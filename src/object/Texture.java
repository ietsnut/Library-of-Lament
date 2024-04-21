package object;

import org.lwjgl.BufferUtils;
import property.Load;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL30.*;

public class Texture implements Load {

    private static final IndexColorModel ICM = new IndexColorModel(2, 4,
            new byte[]{(byte) 0, (byte) 0, (byte) 128, (byte) 255},
            new byte[]{(byte) 0, (byte) 0, (byte) 128, (byte) 255},
            new byte[]{(byte) 0, (byte) 0, (byte) 128, (byte) 255},
            new byte[]{(byte) 0, (byte) 255, (byte) 255, (byte) 255});

    public int id;

    public String           file;
    public ByteBuffer       bytes;

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
        BufferedImage image;
        if (tiles == 1) {
            image = load(this.file);
        } else {
            BufferedImage sample = load(this.file + "0");
            image = new BufferedImage(sample.getWidth() * tiles, sample.getHeight(), BufferedImage.TYPE_BYTE_INDEXED, ICM);
            Graphics2D g = image.createGraphics();
            for (int i = 0; i < tiles; i++) {
                BufferedImage frame = load(file + i);
                g.drawImage(frame, sample.getWidth() * i, 0, null);
            }
            g.dispose();
        }
        image = dither(image);
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.bytes = load(image);
    }

    @Override
    public void bind() {
        this.id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        if (repeat) {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, GL_REPEAT);
            glBindTexture(GL_TEXTURE_2D, 0);
        } else {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        }
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, (width / 4) * tiles, height, 0, GL_RED, GL_UNSIGNED_BYTE, bytes);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    @Override
    public String toString() {
        return "<" + this.getClass().getSimpleName() + "> [" + file + "] : " + width + ", " + height;
    }

    public Texture repeat() {
        repeat = true;
        return this;
    }

    class Dither {

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

        public static BufferedImage floydSteinbergDithering(BufferedImage img) {

            C3[] palette = new C3[]{

                    new C3(0, 0, 0), // black
                    new C3(0, 0, 255), // green
                    new C3(0, 255, 0), // blue
                    new C3(0, 255, 255), // cyan
                    new C3(255, 0, 0), // red
                    new C3(255, 0, 255), // purple
                    new C3(255, 255, 0), // yellow
                    new C3(255, 255, 255)  // white

            };

            int w = img.getWidth();
            int h = img.getHeight();

            C3[][] d = new C3[h][w];

            for (int y = 0; y < h; y++) {

                for (int x = 0; x < w; x++) {

                    d[y][x] = new C3(img.getRGB(x, y));

                }

            }

            for (int y = 0; y < img.getHeight(); y++) {

                for (int x = 0; x < img.getWidth(); x++) {

                    C3 oldColor = d[y][x];
                    C3 newColor = findClosestPaletteColor(oldColor, palette);
                    img.setRGB(x, y, newColor.toColor().getRGB());

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

            return img;
        }

    }
}
