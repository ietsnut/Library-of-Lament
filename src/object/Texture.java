package object;

import org.lwjgl.opengl.GL11;
import tool.Dither;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.GL_GENERATE_MIPMAP;

public class Texture  {

    public static final ArrayList<Texture> ALL = new ArrayList<>();
    public final int ID;
    public ByteBuffer buffer;
    public BufferedImage image;

    public Texture(String name) {
        this(load(name));
    }

    public Texture(BufferedImage image) {
        int[] pixels = null;
        this.image = image;
        int width = image.getWidth();
        int height = image.getHeight();
        pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);
        buffer = ByteBuffer.allocateDirect(4 * image.getWidth() * image.getHeight());
        int[] data = new int[width * height];
        for (int i = 0; i < width * height; i++) {
            buffer.put((byte) ((pixels[i] >> 16) & 0xFF)); // Red component
            buffer.put((byte) ((pixels[i] >> 8) & 0xFF));  // Green component
            buffer.put((byte) (pixels[i] & 0xFF));         // Blue component
            buffer.put((byte) ((pixels[i] >> 24) & 0xFF)); // Alpha component
            int a = (pixels[i] & 0xff000000) >> 24;
            int r = (pixels[i] & 0xff0000) >> 16;
            int g = (pixels[i] & 0xff00) >> 8;
            int b = (pixels[i] & 0xff);
            data[i] = a << 24 | b << 16 | g << 8 | r;
        }
        buffer.flip();
        this.ID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.ID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, GL_FALSE);
        IntBuffer buffer = ByteBuffer.allocateDirect(data.length << 2).order(ByteOrder.nativeOrder()).asIntBuffer();
        buffer.put(data).flip();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        glBindTexture(GL_TEXTURE_2D, 0);
        ALL.add(this);
    }

    public Texture() {
        this.ID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.ID);
        glBindTexture(GL_TEXTURE_2D, 0);
        ALL.add(this);
    }

    private static BufferedImage load(String name) {
        BufferedImage image;
        try {
            image = ImageIO.read(new FileInputStream(name));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return image;
    }

}
