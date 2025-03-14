package resource;

import engine.Console;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.lwjgl.opengl.GL40.*;

public class Material implements Resource {

    public static final int[] PALETTE = {
            0x00000000, // Transparent
            0xFFA5B4D3, // #A5B4D3
            0xFFCF4E38, // #CF4E38
            0xFFC7BE7D, // #C7BE7D
            0XFFFFA572, // #FFA572
            0xFF325D8A, // #325D8A
            0xFF415E54, // #415E54
            0xFFD28D87  // #D28D87
    };

    public static final int LINE = 0xFF43392A; //#43392A

    private ByteBuffer buffer;

    public int texture;
    public BufferedImage image;

    public int width;
    public int height;

    private final String file;

    public Material(String name) {
        this.file = "/resources/automata/" + name + ".png";
        this.queue();
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

    private static int findClosestPaletteColor(int argb) {
        if ((argb >>> 24) == 0) {
            return 0;
        }
        int minDistance = Integer.MAX_VALUE;
        int closestIndex = 0;
        for (int i = 0; i < PALETTE.length; i++) {
            int color = PALETTE[i];
            int dr = ((color >> 16) & 0xFF) - ((argb >> 16) & 0xFF);
            int dg = ((color >> 8) & 0xFF) - ((argb >> 8) & 0xFF);
            int db = (color & 0xFF) - (argb & 0xFF);
            int distance = dr * dr + dg * dg + db * db;
            if (distance < minDistance) {
                minDistance = distance;
                closestIndex = i;
            }
        }
        return closestIndex;
    }

    @Override
    public void load() {
        this.image = load(file);
        if (this.image == null) {
            Console.error("Failed to load", file);
            return;
        }
        this.width  = this.image.getWidth();
        this.height = this.image.getHeight();
    }

    @Override
    public void buffer() {
        int totalPixels = width * height;
        int totalBytes = (totalPixels + 1) / 2;
        this.buffer = BufferUtils.createByteBuffer(totalBytes).order(ByteOrder.nativeOrder());
        for (int i = 0; i < totalPixels; i += 2) {
            int x1 = i % width;
            int y1 = i / width;
            int argb1 = this.image.getRGB(x1, y1);
            int index1 = findClosestPaletteColor(argb1) & 0x07;
            int index2 = 0;
            if (i + 1 < totalPixels) {
                int x2 = (i + 1) % width;
                int y2 = (i + 1) / width;
                int argb2 = this.image.getRGB(x2, y2);
                index2 = findClosestPaletteColor(argb2) & 0x07;
            }
            byte packedData = (byte) ((index1 << 4) | index2);
            this.buffer.put(packedData);
        }
        this.buffer.flip();
    }

    @Override
    public boolean loaded() {
        return this.image != null;
    }

    @Override
    public boolean binded() {
        return this.texture != 0;
    }

    @Override
    public void bind() {
        this.texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, (width / 2), height, 0, GL_RED, GL_UNSIGNED_BYTE, buffer);
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
