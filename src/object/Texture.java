package object;

import org.lwjgl.BufferUtils;
import property.Load;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.*;

import static org.lwjgl.opengl.GL40.*;

public class Texture implements Load {

    private static final IndexColorModel ICM = new IndexColorModel(2, 4,
            new byte[]{(byte) 0, (byte) 0, (byte) 128, (byte) 255},
            new byte[]{(byte) 0, (byte) 0, (byte) 128, (byte) 255},
            new byte[]{(byte) 0, (byte) 0, (byte) 128, (byte) 255},
            new byte[]{(byte) 0, (byte) 255, (byte) 255, (byte) 255});

    public int id;

    public BufferedImage    image;
    public String           file;
    public ByteBuffer       buffer;

    public int width;
    public int height;

    public int tiles;
    public int tile;

    public boolean repeat = false;

    public Texture() {
        this.id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, 0);
        BOUND.add(this);
    }

    public Texture(String namespace, String name) {
        this(namespace, name, 1);
    }

    public Texture(String namespace, String name, int tiles) {
        this.tiles  = tiles;
        this.file   = namespace + "/" + name;
        enqueue();
    }

    public static BufferedImage load(String file) {
        BufferedImage image;
        try {
            image = ImageIO.read(new FileInputStream("resource/" + file + ".png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return image;
    }

    public BufferedImage dither(BufferedImage original) {
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

    public ByteBuffer load(BufferedImage image) {
        byte[] imgData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        ByteBuffer buffer = BufferUtils.createByteBuffer(imgData.length / 4).order(ByteOrder.nativeOrder());
        byte packedData;
        for (int i = 0; i < imgData.length; i += 4) {
            packedData = 0;
            for (int j = 0; j < 4; j++) {
                if (i + j < imgData.length) {
                    int pixelValue = (imgData[i + j] & 0xFF) & 0x03;
                    packedData |= (byte) (pixelValue << (6 - 2 * j));
                }
            }
            buffer.put(packedData);
        }
        buffer.flip();
        return buffer;
    }

    @Override
    public void load() {
        if (tiles == 1) {
            this.image = load(this.file);
        } else {
            BufferedImage sample = load(this.file + "0");
            this.image = new BufferedImage(sample.getWidth() * tiles, sample.getHeight(), BufferedImage.TYPE_BYTE_INDEXED, ICM);
            Graphics2D g = image.createGraphics();
            for (int i = 0; i < tiles; i++) {
                BufferedImage frame = load(file + i);
                g.drawImage(frame, sample.getWidth() * i, 0, null);
            }
            g.dispose();
        }
        this.image = dither(image);
        this.width  = image.getWidth();
        this.height = image.getHeight();
        this.buffer = load(image);
    }

    @Override
    public void bind() {
        this.id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        if (!repeat) {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, GL_REPEAT);
        } else {
            //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        }
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, (width / 4) * tiles, height, 0, GL_RED, GL_UNSIGNED_BYTE, buffer);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    @Override
    public void unload() {
        glDeleteTextures(id);
    }

    @Override
    public String toString() {
        return "<" + this.getClass().getSimpleName() + "> [" + file + "] : " + width + ", " + height;
    }

    public Texture repeat() {
        repeat = true;
        return this;
    }

}
