package library;

import javax.imageio.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Perception extends BufferedImage {

    private int id;

    public Perception(String name) {
        super(1, 1, BufferedImage.TYPE_BYTE_BINARY);
        try (InputStream inputStream = getClass().getResourceAsStream("/resources/" + name)) {
            if (inputStream != null) {
                BufferedImage original = ImageIO.read(inputStream);
                if (original != null) {
                    setData(original.getData());
                    setRGB(0, 0, original.getRGB(0, 0));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Perception texture() {
        int[] pixels = null;
        pixels = new int[getWidth() * getHeight()];
        getRGB(0, 0, getWidth(), getHeight(), pixels, 0, getWidth());
        int[] data = new int[getWidth() *  getHeight()];
        for (int i = 0; i < getWidth() *  getHeight(); i++) {
            int a = (pixels[i] & 0xff000000) >> 24;
            int r = (pixels[i] & 0xff0000) >> 16;
            int g = (pixels[i] & 0xff00) >> 8;
            int b = (pixels[i] & 0xff);
            data[i] = a << 24 | b << 16 | g << 8 | r;
        }
        int id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        IntBuffer buffer = ByteBuffer.allocateDirect(data.length << 2).order(ByteOrder.nativeOrder()).asIntBuffer();
        buffer.put(data).flip();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, getWidth(), getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        glBindTexture(GL_TEXTURE_2D, 0);
        this.id = id;
        return this;
    }

    public Perception bind() {
        glBindTexture(GL_TEXTURE_2D, id);
        return this;
    }

    public Perception unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
        return this;
    }

    static class C3 {
        int r, g, b;

        public C3(int c) {
            Color color = new Color(c);
            r = color.getRed();
            g = color.getGreen();
            b = color.getBlue();
        }

        public C3(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public C3 add(C3 o) {
            return new C3(r + o.r, g + o.g, b + o.b);
        }

        public int clamp(int c) {
            return Math.max(0, Math.min(255, c));
        }

        public int diff(C3 o) {
            int Rdiff = o.r - r;
            int Gdiff = o.g - g;
            int Bdiff = o.b - b;
            int distanceSquared = Rdiff * Rdiff + Gdiff * Gdiff + Bdiff * Bdiff;
            return distanceSquared;
        }

        public C3 mul(double d) {
            return new C3((int) (d * r), (int) (d * g), (int) (d * b));
        }

        public C3 sub(C3 o) {
            return new C3(r - o.r, g - o.g, b - o.b);
        }

        public Color toColor() {
            return new Color(clamp(r), clamp(g), clamp(b));
        }

        public int toRGB() {
            return toColor().getRGB();
        }
    }

    private static C3 findClosestPaletteColor(C3 c, C3[] palette) {
        C3 closest = palette[0];

        for (C3 n : palette) {
            if (n.diff(c) < closest.diff(c)) {
                closest = n;
            }
        }

        return closest;
    }

    public Perception dither() {
        C3[] palette = new C3[] {
                new C3(  0,   0,   0), // black
                new C3(  0,   0, 255), // green
                new C3(  0, 255,   0), // blue
                new C3(  0, 255, 255), // cyan
                new C3(255,   0,   0), // red
                new C3(255,   0, 255), // purple
                new C3(255, 255,   0), // yellow
                new C3(255, 255, 255)  // white
        };
        int w = getWidth();
        int h = getHeight();
        C3[][] d = new C3[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                d[y][x] = new C3(getRGB(x, y));
            }
        }
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                C3 oldColor = d[y][x];
                C3 newColor = findClosestPaletteColor(oldColor, palette);
                setRGB(x, y, newColor.toColor().getRGB());
                C3 err = oldColor.sub(newColor);
                if (x + 1 < w) {
                    d[y][x + 1] = d[y][x + 1].add(err.mul(7. / 16));
                }
                if (x - 1 >= 0 && y + 1 < h) {
                    d[y + 1][x - 1] = d[y + 1][x - 1].add(err.mul(3. / 16));
                }
                if (y + 1 < h) {
                    d[y + 1][x] = d[y + 1][x].add(err.mul(5. / 16));
                }
                if (x + 1 < w && y + 1 < h) {
                    d[y + 1][x + 1] = d[y + 1][x + 1].add(err.mul(1. / 16));
                }
            }
        }

        return this;
    }
}
