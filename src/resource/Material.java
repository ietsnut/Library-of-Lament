package resource;

import engine.Console;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL40.*;

public class Material implements Resource {

    private ByteBuffer buffer;

    public int id;

    public int width;
    public int height;

    private final String file;

    int[] pixels;
    private byte[] grayscalePixels;
    private boolean isIndexed = false;

    public static final int[] PALETTE = {
            0xFF000000, // #000000
            0xFF1F1C23, // #1F1C23
            0xFF322429, // #322429
            0xFF643A37, // #643A37
            0xFF4D5176, // #4D5176
            0xFFA29F7C, // #A29F7C
            0xFFC0D1CB  // #C0D1CB
    };

    public static final int LINE = 0xFFA29F7C;

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

            // Check if the image is indexed (has a color palette)
            if (image.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
                this.isIndexed = true;
                // Handle indexed image as before
                if (image.getType() == BufferedImage.TYPE_INT_RGB || image.getType() == BufferedImage.TYPE_INT_ARGB) {
                    pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
                } else {
                    pixels = image.getRGB(0, 0, width, height, null, 0, width);
                }
            } else {
                this.isIndexed = false;
                // Handle as grayscale image (similar to Texture class)

                // Convert to ARGB if not already
                BufferedImage argbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                argbImage.getGraphics().drawImage(image, 0, 0, null);

                int[] argbPixels = ((DataBufferInt) argbImage.getRaster().getDataBuffer()).getData();
                this.grayscalePixels = new byte[width * height];

                for (int i = 0; i < width * height; i++) {
                    int argb = argbPixels[i];
                    int a = (argb >> 24) & 0xFF;
                    int r = (argb >> 16) & 0xFF;
                    int g = (argb >> 8) & 0xFF;
                    int b = argb & 0xFF;

                    // If pixel is transparent (alpha < threshold), set to 0
                    if (a < 128) {
                        this.grayscalePixels[i] = 0;
                    } else {
                        // Convert to grayscale and map to range 1-255
                        int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);
                        // Ensure grayscale value is in range 1-255 (never 0)
                        this.grayscalePixels[i] = (byte) Math.max(1, gray);
                    }
                }
            }
        } catch (IOException e) {
            Console.error("Failed to load", file);
        }
    }

    private int dither(int argb) {
        int a = (argb >> 24) & 0xff;
        if (a < 128) return 0;
        int r = (argb >> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = argb & 0xff;
        int gray = (r * 77 + g * 150 + b * 29) >> 8;
        if (gray < 65) return 1;
        else if (gray < 191) return 2;
        else return 3;
    }

    @Override
    public void buffer() {
        if (isIndexed) {
            // Handle indexed images as before
            if (pixels == null) {
                Console.warning("No pixels to buffer for", file);
                return;
            }
            buffer = BufferUtils.createByteBuffer(width * height);
            for (int pixel : pixels) {
                int paletteIndex = dither(pixel);
                buffer.put((byte) paletteIndex);
            }
            buffer.flip();
        } else {
            // Handle grayscale images
            if (grayscalePixels == null) {
                Console.warning("No grayscale pixels to buffer for", file);
                return;
            }
            buffer = ByteBuffer.allocateDirect(grayscalePixels.length).order(ByteOrder.nativeOrder());
            buffer.put(grayscalePixels).flip();
        }
    }

    @Override
    public boolean loaded() {
        return this.buffer != null && this.buffer.capacity() > 0;
    }

    @Override
    public boolean binded() {
        return this.id != 0;
    }

    @Override
    public void bind() {
        this.id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R8UI, width, height, 0, GL_RED_INTEGER, GL_UNSIGNED_BYTE, buffer);
        glBindTexture(GL_TEXTURE_2D, 0);
        this.buffer.clear();
        this.buffer = null;
    }

    @Override
    public void unload() {
        pixels = null;
        grayscalePixels = null;
    }

    @Override
    public void unbind() {
        glDeleteTextures(id);
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