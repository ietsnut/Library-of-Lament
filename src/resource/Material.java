package resource;

import engine.Console;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL40.*;

public class Material implements Resource {

    private static final IndexColorModel ICM = new IndexColorModel(2, 4,
            new byte[]{(byte) 0, (byte) 0, (byte) 128, (byte) 255},
            new byte[]{(byte) 0, (byte) 0, (byte) 128, (byte) 255},
            new byte[]{(byte) 0, (byte) 0, (byte) 128, (byte) 255},
            new byte[]{(byte) 0, (byte) 255, (byte) 255, (byte) 255});

    private ByteBuffer buffer;

    public int texture;
    public byte[] image = new byte[0];

    public int width;
    public int height;

    private final String file;

    public static final int[] PALETTE_OLD = {
            0xFF000000, // vec3(0.0, 0.0, 0.0)
            0xFF3A3635, // vec3(0.227, 0.212, 0.208)
            0xFF474745, // vec3(0.278, 0.278, 0.263)
            0xFF575D5B, // vec3(0.341, 0.365, 0.357)
            0xFF7E8686, // vec3(0.494, 0.525, 0.525)
            0xFFAAACA4, // vec3(0.667, 0.675, 0.643)
            0xFFE6E7DE  // vec3(0.902, 0.906, 0.871)
    };

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

    public Material(String type, String name) {
        this.file = "/resources/" + type + "/" + name + ".png";
        this.queue();
    }

    public Material(String type, int state) {
        this(type, Integer.toString(state));
    }

    @Override
    public String toString() {
        return file;
    }

    public BufferedImage load(String file) {
        BufferedImage image;
        try (InputStream in = getClass().getResourceAsStream(file)) {
            assert in != null;
            try (BufferedInputStream bis = new BufferedInputStream(in)) {
                image = ImageIO.read(bis);
            }
        } catch (IOException e) {
            return null;
        }
        return image;
    }

    //TODO: encode and decode directly without going over pixels

    public static BufferedImage dither(BufferedImage original) {
        BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_INDEXED, ICM);
        for (int y = 0; y < original.getHeight(); y++) {
            for (int x = 0; x < original.getWidth(); x++) {
                int argb = original.getRGB(x, y);
                int a = (argb >> 24) & 0xff;
                int r = (argb >> 16) & 0xff;
                int g = (argb >> 8) & 0xff;
                int b = argb & 0xff;
                int index;
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                if (a < 128) {
                    index = 0;
                } else if (gray < 64.75) {
                    index = 1; // black
                } else if (gray < 191.25) {
                    index = 2; // gray
                } else {
                    index = 3; // white
                }
                image.getRaster().setSample(x, y, 0, index);
            }
        }
        return image;
    }

    @Override
    public void load() {
        BufferedImage image;
        image       = load(file);
        if (this.image == null) {
            Console.error("Failed to load", file);
            return;
        }
        image       = dither(image);
        this.width  = image.getWidth();
        this.height = image.getHeight();
        this.image  = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
    }

    @Override
    public void buffer() {
        int totalPixels = width * height;
        int totalBytes  = (totalPixels + 3) / 4;
        this.buffer = BufferUtils.createByteBuffer(totalBytes).order(ByteOrder.nativeOrder());
        byte packedData;
        for (int i = 0; i < image.length; i += 4) {
            packedData = 0;
            for (int j = 0; j < 4; j++) {
                if (i + j < image.length) {
                    int pixelValue = (image[i + j] & 0xFF) & 0x03;
                    packedData |= (byte) (pixelValue << (6 - 2 * j));
                }
            }
            buffer.put(packedData);
        }
        buffer.flip();
    }

    @Override
    public boolean loaded() {
        return this.image.length > 0;
    }

    @Override
    public boolean binded() {
        return this.texture != 0;
    }

    @Override
    public void bind() {
        this.texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, (width / 4), height, 0, GL_RED, GL_UNSIGNED_BYTE, buffer);
        glBindTexture(GL_TEXTURE_2D, 0);
        this.buffer.clear();
        this.buffer = null;
    }

    @Override
    public void unload() {
        this.image = null;
    }

    @Override
    public void unbind() {
        glDeleteTextures(texture);
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
