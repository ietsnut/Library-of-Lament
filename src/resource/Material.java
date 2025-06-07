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

    private byte[] pixels;

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
            if (!(image.getColorModel() instanceof IndexColorModel)) {
                Console.error("Expected indexed image format", file);
                return;
            }
            WritableRaster raster = image.getRaster();
            pixels = ((DataBufferByte) raster.getDataBuffer()).getData();
        } catch (IOException e) {
            Console.error("Failed to load", file);
        }
    }

    @Override
    public void buffer() {
        if (pixels == null) {
            Console.warning("No pixels to buffer for", file);
            return;
        }
        buffer = ByteBuffer.allocateDirect(pixels.length).order(ByteOrder.nativeOrder());
        buffer.put(pixels).flip();
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