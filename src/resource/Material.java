package resource;

import engine.Console;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL40;

import javax.imageio.ImageIO;
import java.awt.*;
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
    private int colorMapSize; // Store the actual size of the color palette

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
        this.queue();
    }

    public Material(String name) {
        this.file = "/resources/" + name + ".png";
        this.queue();
    }

    public Material(String type, String name) {
        this.file = "/resources/" + type + "/" + name + ".png";
        this.queue();
    }

    @Override
    public String toString() {
        return file;
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
                Console.error("Expected indexed image format", file);
                return;
            }

            // Get the actual color map size from the IndexColorModel
            this.colorMapSize = colorModel.getMapSize();

            WritableRaster raster = image.getRaster();
            pixels = ((DataBufferByte) raster.getDataBuffer()).getData();

            // Generate mip levels during load
            generateMipLevels();

        } catch (IOException e) {
            Console.error("Failed to load", file);
        }
    }

    private void generateMipLevels() {
        if (pixels == null) return;

        // Calculate how many mip levels we can/should generate
        int maxPossibleLevels = (int) (Math.log(Math.max(width, height)) / Math.log(2)) + 1;
        mipLevels = Math.min(MAX_MIP_LEVELS, maxPossibleLevels);

        // Initialize arrays
        mipPixels = new byte[mipLevels][];
        mipWidths = new int[mipLevels];
        mipHeights = new int[mipLevels];

        // Generate mip levels
        byte[] currentPixels = pixels;
        int currentWidth = width;
        int currentHeight = height;

        for (int level = 0; level < mipLevels; level++) {
            mipPixels[level] = currentPixels.clone();
            mipWidths[level] = currentWidth;
            mipHeights[level] = currentHeight;

            // Check if we should generate the next level
            if (level < mipLevels - 1 && currentWidth > MIN_MIP_SIZE && currentHeight > MIN_MIP_SIZE) {
                int nextWidth = Math.max(MIN_MIP_SIZE, currentWidth / 2);
                int nextHeight = Math.max(MIN_MIP_SIZE, currentHeight / 2);

                currentPixels = generateMipLevel(currentPixels, currentWidth, currentHeight, nextWidth, nextHeight);
                currentWidth = nextWidth;
                currentHeight = nextHeight;
            } else {
                // Adjust mipLevels to actual generated levels
                mipLevels = level + 1;
                break;
            }
        }

        Console.log("Generated " + mipLevels, file);
    }

    private byte[] generateMipLevel(byte[] sourcePixels, int sourceWidth, int sourceHeight, int targetWidth, int targetHeight) {
        byte[] targetPixels = new byte[targetWidth * targetHeight];

        float xRatio = (float) sourceWidth / targetWidth;
        float yRatio = (float) sourceHeight / targetHeight;

        for (int y = 0; y < targetHeight; y++) {
            for (int x = 0; x < targetWidth; x++) {
                float sourceX1 = x * xRatio;
                float sourceY1 = y * yRatio;
                float sourceX2 = (x + 1) * xRatio;
                float sourceY2 = (y + 1) * yRatio;

                int startX = (int) Math.floor(sourceX1);
                int startY = (int) Math.floor(sourceY1);
                int endX = Math.min((int) Math.ceil(sourceX2), sourceWidth);
                int endY = Math.min((int) Math.ceil(sourceY2), sourceHeight);

                int[] counts = new int[colorMapSize];
                int totalSamples = 0;

                for (int sy = startY; sy < endY; sy++) {
                    for (int sx = startX; sx < endX; sx++) {
                        if (sx >= 0 && sx < sourceWidth && sy >= 0 && sy < sourceHeight) {
                            byte pixelValue = sourcePixels[sy * sourceWidth + sx];
                            int index = pixelValue & 0xFF;
                            if (index < colorMapSize) {
                                counts[index]++;
                                totalSamples++;
                            }
                        }
                    }
                }

                byte mostFrequent = 0;
                int maxCount = 0;
                for (int i = 0; i < colorMapSize; i++) {
                    if (counts[i] > maxCount) {
                        maxCount = counts[i];
                        mostFrequent = (byte) i;
                    }
                }

                if (totalSamples == 0) {
                    int fallbackX = Math.min(startX, sourceWidth - 1);
                    int fallbackY = Math.min(startY, sourceHeight - 1);
                    mostFrequent = sourcePixels[fallbackY * sourceWidth + fallbackX];
                }

                targetPixels[y * targetWidth + x] = mostFrequent;
            }
        }

        return targetPixels;
    }

    @Override
    public void buffer() {
        if (mipPixels == null) {
            Console.warning("No mip levels to buffer for", file);
            return;
        }
        mipBuffers = new ByteBuffer[mipLevels];
        for (int level = 0; level < mipLevels; level++) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(mipPixels[level].length).order(ByteOrder.nativeOrder());
            buffer.put(mipPixels[level]).flip();
            mipBuffers[level] = buffer;
        }
        Console.log("Buffered " + mipLevels, file);
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
            Console.warning("No buffers to bind for", file);
            return;
        }

        this.id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, mipLevels - 1);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, -0.5f);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        for (int level = 0; level < mipLevels; level++) {
            glTexImage2D(GL_TEXTURE_2D, level, GL_R8UI, mipWidths[level], mipHeights[level], 0, GL_RED_INTEGER, GL_UNSIGNED_BYTE, mipBuffers[level]);
        }
        if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
            float amount = Math.max(4f, GL40.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));
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

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}