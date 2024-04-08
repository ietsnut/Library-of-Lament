package object;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL14.GL_GENERATE_MIPMAP;

public class Texture extends Number {

    public static final ArrayList<Texture> ALL = new ArrayList<>();
    public final int ID;
    public int frames = 1;
    public BufferedImage image;

    public Texture(String namespace, String name, int frames) {
        this.frames = frames;
        BufferedImage example = load("resource/" + namespace + "/" + name + "0.png");
        this.image = new BufferedImage(example.getWidth() * frames, example.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = this.image.createGraphics();
        for (int i = 0; i < frames; i++) {
            BufferedImage frame = load("resource/" + namespace + "/" + name + i + ".png");
            g.drawImage(frame, example.getWidth() * i, 0, null);
        }
        g.dispose();
        this.ID = glGenTextures();
        load(this.image);
        ALL.add(this);
    }

    public Texture(String namespace, String name) {
        this(load("resource/" + namespace + "/" + name + ".png"));
    }

    public Texture(BufferedImage image) {
        this.ID = glGenTextures();
        load(image);
        ALL.add(this);
    }

    public Texture() {
        this.ID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.ID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL11.GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL11.GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, 0);
        ALL.add(this);
    }

    public Texture repeat() {
        glBindTexture(GL_TEXTURE_2D, this.ID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glBindTexture(GL_TEXTURE_2D, 0);
        return this;
    }

    public Texture bind() {
        glBindTexture(GL_TEXTURE_2D, this.ID);
        return this;
    }

    public Texture unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
        return this;
    }

    private void load(BufferedImage image) {
        int[] pixels = null;
        this.image  = image;
        int width   = image.getWidth();
        int height  = image.getHeight();
        pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);
        int[] data = new int[width * height];
        for (int i = 0; i < width * height; i++) {
            int a = (pixels[i] & 0xff000000)    >> 24;
            int r = (pixels[i] & 0xff0000)      >> 16;
            int g = (pixels[i] & 0xff00)        >> 8;
            int b = (pixels[i] & 0xff);
            data[i] = a << 24 | b << 16 | g << 8 | r;
        }
        glBindTexture(GL_TEXTURE_2D, this.ID);
        GL30.glGenerateMipmap(GL_TEXTURE_2D);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER,   GL_NEAREST_MIPMAP_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER,   GL_NEAREST_MIPMAP_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER,   GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER,   GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,       GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,       GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP,      GL_FALSE);
        IntBuffer buffer = ByteBuffer.allocateDirect(data.length << 2).order(ByteOrder.nativeOrder()).asIntBuffer();
        buffer.put(data).flip();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    private static BufferedImage load(String file) {
        BufferedImage image;
        try {
            image = ImageIO.read(new FileInputStream(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return image;
    }

    @Override
    public int intValue() {
        return ID;
    }

    @Override
    public long longValue() {
        return ID;
    }

    @Override
    public float floatValue() {
        return ID;
    }

    @Override
    public double doubleValue() {
        return ID;
    }

}
