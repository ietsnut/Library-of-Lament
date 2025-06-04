package resource;

import engine.Console;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL40;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL40.*;

public class Texture implements Resource {

    private ByteBuffer buffer;
    private byte[] pixels;

    public int id;
    public int width;
    public int height;

    private final String file;

    public Texture(String name) {
        this.file = "/resources/" + name + ".png";
        this.queue();
    }

    public Texture(String type, String name) {
        this.file = "/resources/" + type + "/" + name + ".png";
        this.queue();
    }

    @Override
    public String toString() {
        return file;
    }

    @Override
    public void load() {
        try (InputStream in = Texture.class.getResourceAsStream(file)) {
            if (in == null) {
                Console.warning("Failed to load", file);
                return;
            }

            BufferedImage image = ImageIO.read(in);
            if (image == null) {
                Console.warning("Failed to decode image", file);
                return;
            }

            this.width = image.getWidth();
            this.height = image.getHeight();

            // Convert to ARGB if not already
            BufferedImage argbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            argbImage.getGraphics().drawImage(image, 0, 0, null);

            int[] argbPixels = ((DataBufferInt) argbImage.getRaster().getDataBuffer()).getData();
            this.pixels = new byte[width * height];

            for (int i = 0; i < width * height; i++) {
                int argb = argbPixels[i];
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                // If pixel is transparent (alpha < threshold), set to 0
                if (a < 128) {
                    this.pixels[i] = 0;
                } else {
                    // Convert to grayscale and map to range 1-255
                    int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);
                    // Ensure grayscale value is in range 1-255 (never 0)
                    this.pixels[i] = (byte) Math.max(1, gray);
                }
            }
        } catch (IOException e) {
            Console.error("Failed to load image", file);
        }
    }

    @Override
    public void buffer() {
        buffer = ByteBuffer.allocateDirect(pixels.length).order(ByteOrder.nativeOrder());
        buffer.put(pixels).flip();
    }

    @Override
    public boolean loaded() {
        return buffer != null && buffer.capacity() > 0;
    }

    @Override
    public boolean binded() {
        return id != 0;
    }

    @Override
    public void bind() {
        if (buffer == null) {
            Console.error("Attempted to bind texture before buffering", file);
            return;
        }
        this.id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R8UI, width, height, 0, GL_RED_INTEGER, GL_UNSIGNED_BYTE, buffer);
        if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
            float amount = Math.min(4f, GL40.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));
            GL40.glTexParameterf(GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, amount);
        }
        glBindTexture(GL_TEXTURE_2D, 0);
        buffer.clear();
        buffer = null;
    }

    @Override
    public void unload() {
        pixels = null;
    }

    @Override
    public void unbind() {
        glDeleteTextures(id);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Texture texture && this.file.equals(texture.file);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}