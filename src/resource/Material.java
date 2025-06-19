package resource;

import engine.Console;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL40;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.lwjgl.opengl.GL40.*;

public class Material implements Resource {

    private ByteBuffer[] mipBuffers;
    private int[] mipWidths;
    private int[] mipHeights;
    private byte[][] mipPixels;
    private int mipLevels;
    private int colorMapSize;

    private IndexColorModel colorModel;
    private final Mipmap mipmapper;

    public int id;
    public int width;
    public int height;

    private final String file;
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

    public Material() {
        this.file = null;
        this.mipmapper = Mipmap.getInstance();
        this.queue();
    }

    public Material(String name) {
        this.file = "/resources/" + name + ".png";
        this.mipmapper = Mipmap.getInstance();
        this.queue();
    }

    public Material(String type, String name) {
        this.file = "/resources/" + type + "/" + name + ".png";
        this.mipmapper = Mipmap.getInstance();
        this.queue();
    }

    @Override
    public String toString() {
        return file.replace("/resources/", "");
    }

    @Override
    public void load() {
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

    private void generateMipLevels() {
        if (pixels == null)
            return;

        long startTime = System.nanoTime();

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

        for (int level = 0; level < mipLevels; level++) {
            mipPixels[level] = currentPixels.clone();
            mipWidths[level] = currentWidth;
            mipHeights[level] = currentHeight;

            if (level < mipLevels - 1 && currentWidth > MIN_MIP_SIZE && currentHeight > MIN_MIP_SIZE) {
                int nextWidth = Math.max(MIN_MIP_SIZE, currentWidth / 2);
                int nextHeight = Math.max(MIN_MIP_SIZE, currentHeight / 2);

                // Try OpenCL first, fallback to CPU if it fails
                byte[] nextLevelPixels = null;
                if (mipmapper.isInitialized()) {
                    nextLevelPixels = mipmapper.generateMipLevel(
                            currentPixels, currentWidth, currentHeight,
                            nextWidth, nextHeight, palette, colorMapSize);
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

        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / 1_000_000.0; // Convert to milliseconds
        Console.notify("Generated " + mipLevels + " mips", toString(), " in " +
                String.format("%.2f", duration) + "ms using " +
                (mipmapper.isInitialized() ? "OpenCL" : "CPU"));
    }

    // Fallback CPU implementation (original method)
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
            Console.warning("No mipmaps", file);
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
    public boolean loaded() {
        return mipPixels != null;
    }

    @Override
    public boolean binded() {
        return this.id != 0;
    }

    @Override
    public void bind() {
        if (mipBuffers == null) {
            Console.warning("No buffers", file);
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
    public void unbind() {
        if (id != 0) {
            glDeleteTextures(id);
            id = 0;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Material material && this.file.equals(material.file);
    }
}