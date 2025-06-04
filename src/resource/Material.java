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
            if (image.getType() == BufferedImage.TYPE_INT_RGB || image.getType() == BufferedImage.TYPE_INT_ARGB) {
                pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
            } else {
                pixels = image.getRGB(0, 0, width, height, null, 0, width);
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
        int totalPixels = width * height;
        int totalBytes = (totalPixels + 3) / 4;
        this.buffer = BufferUtils.createByteBuffer(totalBytes).order(ByteOrder.nativeOrder());
        for (int i = 0; i < totalPixels; i += 4) {
            byte packedData = 0;
            for (int j = 0; j < 4 && (i + j) < totalPixels; j++) {
                int argb = pixels[i + j];
                int index = dither(argb);
                packedData |= (byte) (index << (6 - 2 * j));
            }
            buffer.put(packedData);
        }
        buffer.flip();
    }

    @Override
    public boolean loaded() {
        return this.buffer.capacity() > 0;
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
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R8UI, (width / 4), height, 0, GL_RED_INTEGER, GL_UNSIGNED_BYTE, buffer);
        glBindTexture(GL_TEXTURE_2D, 0);
        this.buffer.clear();
        this.buffer = null;
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
        return obj instanceof Material material && this.file.equals(material.file);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
