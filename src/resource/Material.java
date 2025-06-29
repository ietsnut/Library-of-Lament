package resource;

import engine.Console;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL40;
import org.lwjgl.system.MemoryStack;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.stream.Collectors;

import static org.lwjgl.opencl.CL10.*;
import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;

public class Material implements Resource {

    private static long clContext;
    private static long clDevice;
    private static long clProgram;
    private static long clKernel;
    private static long clCommandQueue;
    private static boolean clInitialized = false;

    private ByteBuffer[] mipBuffers;
    private int[] mipWidths;
    private int[] mipHeights;
    private byte[][] mipPixels;
    private int mipLevels;
    private int colorMapSize;

    private IndexColorModel colorModel;

    public int id;
    public int width;
    public int height;
    public int states;

    private final String type;
    private final String name;
    private byte[] pixels;

    private static final int MAX_MIP_LEVELS = 8;
    private static final int MIN_MIP_SIZE = 8;

    public static final int[] MIYAZAKI_16 = {
            0xFF232228, 0xFF284261, 0xFF5F5854, 0xFF878573,
            0xFFB8B095, 0xFFC3D5C7, 0xFFEBECDC, 0xFF2485A6,
            0xFF54BAD2, 0xFF754D45, 0xFFC65046, 0xFFE6928A,
            0xFF1E7453, 0xFF55A058, 0xFFA1BF41, 0xFFE3C054
    };

    public static final int LINE = MIYAZAKI_16[0];

    static {
        initializeOpenCL();
    }

    public Material() {
        this(null, null, 1);
    }

    public Material(String name) {
        this(name, null, 1);
    }

    public Material(String name, String type) {
        this(name, type, 1);
    }

    public Material(String name, String type, int states) {
        this.type = type;
        this.name = name;
        this.states = states;
        this.queue();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void load() {
        if (states > 1) {
            loadAtlas();
        } else {
            loadSingle();
        }
    }

    private void loadSingle() {
        String file;
        if (type == null) {
            file = "/resources/" + name + ".png";
        } else {
            file = "/resources/" + type + "/" + name + ".png";
        }
        try (InputStream in = Material.class.getResourceAsStream(file)) {
            if (in == null) {
                Console.warning("Failed to load", file);
                return;
            }
            BufferedImage image = ImageIO.read(in);
            if (image == null) {
                Console.warning("Failed to load", file);
                return;
            }
            this.width = image.getWidth();
            this.height = image.getHeight();
            if (!(image.getColorModel() instanceof IndexColorModel colorModel)) {
                Console.error("Unexpected format", file);
                return;
            }
            this.colorModel = colorModel;
            this.colorMapSize = colorModel.getMapSize();
            WritableRaster raster = image.getRaster();
            pixels = ((DataBufferByte) raster.getDataBuffer()).getData();
            generateMipLevels();
        } catch (IOException e) {
            Console.error("Failed to load", file);
        }
    }

    private void loadAtlas() {
        try {
            BufferedImage[] stateImages = new BufferedImage[states];
            int stateWidth = 0, stateHeight = 0;
            IndexColorModel sharedColorModel = null;

            // Load all state textures
            for (int i = 0; i < states; i++) {
                String stateFile = "/resources/" + type + "/" + name + i + ".png";
                try (InputStream in = Material.class.getResourceAsStream(stateFile)) {
                    if (in == null) {
                        Console.warning("Failed to load state", stateFile);
                        return;
                    }
                    BufferedImage image = ImageIO.read(in);
                    if (image == null) {
                        Console.warning("Failed to load state", stateFile);
                        return;
                    }

                    if (i == 0) {
                        stateWidth = image.getWidth();
                        stateHeight = image.getHeight();
                        if (!(image.getColorModel() instanceof IndexColorModel)) {
                            Console.error("Unexpected format", stateFile);
                            return;
                        }
                        sharedColorModel = (IndexColorModel) image.getColorModel();
                    } else {
                        // Verify all states have the same dimensions and color model
                        if (image.getWidth() != stateWidth || image.getHeight() != stateHeight) {
                            Console.error("State texture size mismatch", stateFile);
                            return;
                        }
                    }

                    stateImages[i] = image;
                }
            }

            // Create atlas (horizontal arrangement)
            this.width = stateWidth * states;
            this.height = stateHeight;
            this.colorModel = sharedColorModel;
            this.colorMapSize = sharedColorModel.getMapSize();

            // Combine all state textures into one atlas
            pixels = new byte[width * height];

            for (int state = 0; state < states; state++) {
                WritableRaster raster = stateImages[state].getRaster();
                byte[] statePixels = ((DataBufferByte) raster.getDataBuffer()).getData();

                // Copy state texture to atlas
                for (int y = 0; y < stateHeight; y++) {
                    for (int x = 0; x < stateWidth; x++) {
                        int srcIndex = y * stateWidth + x;
                        int dstIndex = y * width + (state * stateWidth + x);
                        pixels[dstIndex] = statePixels[srcIndex];
                    }
                }
            }

            generateMipLevels();

        } catch (IOException e) {
            Console.error("Failed to load atlas", name);
        }
    }

    private void generateMipLevels() {
        if (pixels == null)
            return;

        int maxPossibleLevels = (int) (Math.log(Math.max(width, height)) / Math.log(2)) + 1;
        mipLevels = Math.min(MAX_MIP_LEVELS, maxPossibleLevels);

        mipPixels = new byte[mipLevels][];
        mipWidths = new int[mipLevels];
        mipHeights = new int[mipLevels];

        // Create palette array for OpenCL
        int[] palette = new int[colorMapSize];
        for (int i = 0; i < colorMapSize; i++) {
            palette[i] = colorModel.getRGB(i);
        }

        byte[] currentPixels = pixels;
        int currentWidth = width;
        int currentHeight = height;

        boolean usingOpenCL = clInitialized;

        for (int level = 0; level < mipLevels; level++) {
            mipPixels[level] = currentPixels.clone();
            mipWidths[level] = currentWidth;
            mipHeights[level] = currentHeight;

            if (level < mipLevels - 1 && currentWidth > MIN_MIP_SIZE && currentHeight > MIN_MIP_SIZE) {
                int nextWidth = Math.max(MIN_MIP_SIZE, currentWidth / 2);
                int nextHeight = Math.max(MIN_MIP_SIZE, currentHeight / 2);

                // Try OpenCL first, fallback to CPU if it fails
                byte[] nextLevelPixels = null;
                if (usingOpenCL) {
                    nextLevelPixels = generateMipLevelOpenCL(
                            currentPixels, currentWidth, currentHeight,
                            nextWidth, nextHeight, palette, colorMapSize);

                    // If OpenCL fails, switch to CPU for remaining levels
                    if (nextLevelPixels == null) {
                        usingOpenCL = false;
                        Console.warning("OpenCL failed for " + toString() + ", switching to CPU");
                    }
                }

                if (nextLevelPixels == null) {
                    // Fallback to CPU implementation
                    nextLevelPixels = generateMipLevelCPU(currentPixels, currentWidth, currentHeight,
                            nextWidth, nextHeight, palette);
                }

                currentPixels = nextLevelPixels;
                currentWidth = nextWidth;
                currentHeight = nextHeight;
            } else {
                mipLevels = level + 1;
                break;
            }
        }
    }

    private static void initializeOpenCL() {
        try (MemoryStack stack = stackPush()) {
            // Get platform
            IntBuffer pi = stack.mallocInt(1);
            checkCLError(clGetPlatformIDs(null, pi));
            if (pi.get(0) == 0) {
                Console.error("No OpenCL platforms found");
                return;
            }

            PointerBuffer platforms = stack.mallocPointer(pi.get(0));
            checkCLError(clGetPlatformIDs(platforms, (IntBuffer) null));
            long platform = platforms.get(0);

            // Get device - try GPU first, then CPU
            checkCLError(clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, null, pi));
            long deviceType = CL_DEVICE_TYPE_GPU;
            if (pi.get(0) == 0) {
                Console.warning("No GPU devices found, trying CPU");
                checkCLError(clGetDeviceIDs(platform, CL_DEVICE_TYPE_CPU, null, pi));
                deviceType = CL_DEVICE_TYPE_CPU;
                if (pi.get(0) == 0) {
                    Console.error("No OpenCL devices found");
                    return;
                }
            }

            PointerBuffer devices = stack.mallocPointer(pi.get(0));
            checkCLError(clGetDeviceIDs(platform, deviceType, devices, (IntBuffer) null));
            clDevice = devices.get(0);

            // Create context
            PointerBuffer contextProps = stack.mallocPointer(3);
            contextProps.put(CL_CONTEXT_PLATFORM).put(platform).put(0);
            contextProps.flip();

            clContext = clCreateContext(contextProps, clDevice, null, NULL, pi);
            checkCLError(pi.get(0));

            // Create command queue
            clCommandQueue = clCreateCommandQueue(clContext, clDevice, 0, pi);
            checkCLError(pi.get(0));

            // Load and compile the mipmap kernel
            if (!loadMipmapKernel()) {
                shutdown();
                return;
            }

            clInitialized = true;
            Console.log("OpenCL:",
                    (deviceType == CL_DEVICE_TYPE_GPU ? "GPU" : "CPU"));

        } catch (Exception e) {
            Console.error("Failed to initialize OpenCL", e.getMessage());
            shutdown();
        }
    }

    private static boolean loadMipmapKernel() {
        try (MemoryStack stack = stackPush()) {
            // Load kernel source from file
            String source = loadKernelSource();
            if (source == null) {
                return false;
            }

            IntBuffer pi = stack.mallocInt(1);

            // Create program
            PointerBuffer strings = stack.mallocPointer(1);
            PointerBuffer lengths = stack.mallocPointer(1);

            ByteBuffer sourceBuffer = stack.UTF8(source);
            strings.put(0, sourceBuffer);
            lengths.put(0, sourceBuffer.remaining());

            clProgram = clCreateProgramWithSource(clContext, strings, lengths, pi);
            checkCLError(pi.get(0));

            // Build program
            int ret = clBuildProgram(clProgram, clDevice, "", null, NULL);
            if (ret != CL_SUCCESS) {
                // Get build log
                PointerBuffer pp = stack.mallocPointer(1);
                clGetProgramBuildInfo(clProgram, clDevice, CL_PROGRAM_BUILD_LOG, (ByteBuffer) null, pp);

                int logSize = (int) pp.get(0);
                ByteBuffer buffer = stack.malloc(logSize);
                clGetProgramBuildInfo(clProgram, clDevice, CL_PROGRAM_BUILD_LOG, buffer, null);

                String buildLog = memUTF8(buffer);
                Console.error("OpenCL build failed", buildLog);
                clReleaseProgram(clProgram);
                clProgram = 0;
                return false;
            }

            // Create kernel
            clKernel = clCreateKernel(clProgram, "generate_mipmap", pi);
            checkCLError(pi.get(0));

            return true;

        } catch (Exception e) {
            Console.error("Failed to load mipmap kernel", e.getMessage());
            return false;
        }
    }

    private static String loadKernelSource() {
        try (InputStream in = Material.class.getResourceAsStream("/resources/calculation/mipmap.cl")) {
            assert in != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                return reader.lines().collect(Collectors.joining("\n"));

            }
        } catch (IOException e) {
            Console.error("Failed to read kernel file:", "/resources/calculation/mipmap.cl", e.getMessage());
            return null;
        }
    }

    private byte[] generateMipLevelOpenCL(byte[] sourcePixels, int sourceWidth, int sourceHeight,
                                          int targetWidth, int targetHeight, int[] palette, int colorMapSize) {
        if (!clInitialized) {
            return null;
        }

        // Synchronize OpenCL operations to prevent mixing between textures
        // This is necessary because the kernel arguments are set on a shared static kernel
        // Without synchronization, concurrent mipmap generations could overwrite each other's arguments
        synchronized (Material.class) {
            try (MemoryStack stack = stackPush()) {
                IntBuffer errcode_ret = stack.mallocInt(1);

                // Create source buffer
                ByteBuffer sourceByteBuffer = BufferUtils.createByteBuffer(sourcePixels.length);
                sourceByteBuffer.put(sourcePixels).flip();
                long sourceBuffer = clCreateBuffer(clContext, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                        sourceByteBuffer, errcode_ret);
                checkCLError(errcode_ret.get(0));

                // Create target buffer
                long targetBuffer = clCreateBuffer(clContext, CL_MEM_WRITE_ONLY,
                        (long)targetWidth * targetHeight, errcode_ret);
                checkCLError(errcode_ret.get(0));

                // Create palette buffer
                IntBuffer paletteBuffer = BufferUtils.createIntBuffer(palette.length);
                paletteBuffer.put(palette).flip();
                long paletteBufferCL = clCreateBuffer(clContext, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                        paletteBuffer, errcode_ret);
                checkCLError(errcode_ret.get(0));

                // Set kernel arguments
                clSetKernelArg1p(clKernel, 0, sourceBuffer);
                clSetKernelArg1p(clKernel, 1, targetBuffer);
                clSetKernelArg1p(clKernel, 2, paletteBufferCL);
                clSetKernelArg1i(clKernel, 3, sourceWidth);
                clSetKernelArg1i(clKernel, 4, sourceHeight);
                clSetKernelArg1i(clKernel, 5, targetWidth);
                clSetKernelArg1i(clKernel, 6, targetHeight);
                clSetKernelArg1i(clKernel, 7, colorMapSize);

                // Execute kernel
                PointerBuffer globalWS = stack.mallocPointer(2);
                globalWS.put(0, targetWidth);
                globalWS.put(1, targetHeight);

                checkCLError(clEnqueueNDRangeKernel(clCommandQueue, clKernel, 2, null, globalWS, null, null, null));

                // Wait for completion
                clFinish(clCommandQueue);

                // Read result
                ByteBuffer resultBuffer = BufferUtils.createByteBuffer(targetWidth * targetHeight);
                checkCLError(clEnqueueReadBuffer(clCommandQueue, targetBuffer, true, 0L, resultBuffer, null, null));

                // Convert ByteBuffer to byte array
                byte[] result = new byte[targetWidth * targetHeight];
                resultBuffer.get(result);

                // Cleanup buffers
                clReleaseMemObject(sourceBuffer);
                clReleaseMemObject(targetBuffer);
                clReleaseMemObject(paletteBufferCL);

                return result;

            } catch (Exception e) {
                Console.error("OpenCL mipmap generation failed:", e.getMessage());
                return null;
            }
        } // end synchronized block
    }

    // Fallback CPU implementation
    private byte[] generateMipLevelCPU(byte[] sourcePixels, int sourceWidth, int sourceHeight,
                                       int targetWidth, int targetHeight, int[] palette) {
        byte[] targetPixels = new byte[targetWidth * targetHeight];

        float xRatio = (float) sourceWidth / targetWidth;
        float yRatio = (float) sourceHeight / targetHeight;

        for (int y = 0; y < targetHeight; y++) {
            for (int x = 0; x < targetWidth; x++) {
                float srcX1 = x * xRatio;
                float srcY1 = y * yRatio;
                float srcX2 = (x + 1) * xRatio;
                float srcY2 = (y + 1) * yRatio;

                int startX = (int) Math.floor(srcX1);
                int startY = (int) Math.floor(srcY1);
                int endX = Math.min((int) Math.ceil(srcX2), sourceWidth);
                int endY = Math.min((int) Math.ceil(srcY2), sourceHeight);

                int totalR = 0, totalG = 0, totalB = 0, count = 0;

                for (int sy = startY; sy < endY; sy++) {
                    for (int sx = startX; sx < endX; sx++) {
                        int index = sourcePixels[sy * sourceWidth + sx] & 0xFF;
                        if (index < colorMapSize) {
                            int rgb = palette[index];
                            totalR += (rgb >> 16) & 0xFF;
                            totalG += (rgb >> 8) & 0xFF;
                            totalB += rgb & 0xFF;
                            count++;
                        }
                    }
                }

                if (count == 0) {
                    targetPixels[y * targetWidth + x] = sourcePixels[Math.min(startY, sourceHeight - 1) * sourceWidth
                            + Math.min(startX, sourceWidth - 1)];
                    continue;
                }

                int avgR = totalR / count;
                int avgG = totalG / count;
                int avgB = totalB / count;

                int bestIndex = 0;
                int bestDist = Integer.MAX_VALUE;
                for (int i = 0; i < colorMapSize; i++) {
                    int rgb = palette[i];
                    int dr = ((rgb >> 16) & 0xFF) - avgR;
                    int dg = ((rgb >> 8) & 0xFF) - avgG;
                    int db = (rgb & 0xFF) - avgB;
                    int dist = dr * dr + dg * dg + db * db;
                    if (dist < bestDist) {
                        bestDist = dist;
                        bestIndex = i;
                    }
                }

                targetPixels[y * targetWidth + x] = (byte) bestIndex;
            }
        }

        return targetPixels;
    }

    @Override
    public void buffer() {
        if (mipPixels == null) {
            Console.warning("No mipmaps", name);
            return;
        }
        mipBuffers = new ByteBuffer[mipLevels];
        for (int level = 0; level < mipLevels; level++) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(mipPixels[level].length).order(ByteOrder.nativeOrder());
            buffer.put(mipPixels[level]).flip();
            mipBuffers[level] = buffer;
        }
    }

    @Override
    public boolean linked() {
        return this.id != 0;
    }

    @Override
    public void link() {
        if (mipBuffers == null) {
            Console.warning("No buffers", name);
            return;
        }

        this.id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, mipLevels - 1);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, -0.5f);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        for (int level = 0; level < mipLevels; level++) {
            glTexImage2D(GL_TEXTURE_2D, level, GL_R8UI, mipWidths[level], mipHeights[level], 0, GL_RED_INTEGER,
                    GL_UNSIGNED_BYTE, mipBuffers[level]);
        }
        if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
            float amount = Math.min(4f, GL40.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));
            glTexParameterf(GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, amount);
        } else {
            Console.notify("No anisotropic filtering.");
        }
        glBindTexture(GL_TEXTURE_2D, 0);
        for (int i = 0; i < mipBuffers.length; i++) {
            if (mipBuffers[i] != null) {
                mipBuffers[i].clear();
                mipBuffers[i] = null;
            }
        }
        mipBuffers = null;
        mipWidths = null;
        mipHeights = null;
    }

    @Override
    public void unload() {
        pixels = null;
        mipPixels = null;
    }

    @Override
    public void unlink() {
        if (id != 0) {
            glDeleteTextures(id);
            id = 0;
        }
    }

    @Override
    public void bind() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, id);
    }

    @Override
    public void unbind() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Material material && this.name.equals(material.name);
    }

    private static void checkCLError(int error) {
        if (error != CL_SUCCESS) {
            throw new RuntimeException("OpenCL error " + error);
        }
    }

    // Call this when shutting down the application
    public static synchronized void shutdown() {
        if (clKernel != 0) {
            clReleaseKernel(clKernel);
            clKernel = 0;
        }
        if (clProgram != 0) {
            clReleaseProgram(clProgram);
            clProgram = 0;
        }
        if (clCommandQueue != 0) {
            clReleaseCommandQueue(clCommandQueue);
            clCommandQueue = 0;
        }
        if (clContext != 0) {
            clReleaseContext(clContext);
            clContext = 0;
        }
        clInitialized = false;
    }
}