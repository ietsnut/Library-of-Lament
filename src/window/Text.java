package window;

import engine.Console;
import org.lwjgl.nanovg.NVGColor;
import resource.Mesh;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;

public class Text extends Window {

    public Text(int size) {
        super(size);
        glfwSetWindowPos(handle, 100, 100);
    }

    public long vg;
    private int font;
    private ByteBuffer fontBuffer; // 👈 persist buffer

    @Override
    public void setup() {
        vg = nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES);
        if (vg == 0) {
            Console.error("Failed to create NanoVG context");
            return;
        }

        try (InputStream is = Text.class.getResourceAsStream("/resources/dungeon-mode.ttf")) {
            if (is == null) {
                Console.error("Font not found in resources");
                return;
            }

            byte[] fontData = is.readAllBytes();
            fontBuffer = ByteBuffer.allocateDirect(fontData.length); // 👈 allocate persistent buffer
            fontBuffer.put(fontData).flip();

            font = nvgCreateFontMem(vg, "dungeon-mode", fontBuffer, false);
            if (font == -1) {
                Console.error("Failed to create font from memory");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void draw() {
        if (vg == 0) return;

        nvgBeginFrame(vg, width, height, 1.0f);

        float fontSize = 20.0f;
        nvgFontSize(vg, fontSize);
        nvgFontFaceId(vg, font); // Use your monospaced font here

        try (NVGColor color = NVGColor.calloc()) {
            nvgRGBA((byte)255, (byte)255, (byte)255, (byte)255, color);
            nvgFillColor(vg, color);

            float x = 40;
            float y = 60;
            float lineHeight = fontSize / 1.2f;

            String[] lines = {
                    "┌────────────┐",
                    "│Lv2 Fort    │",
                    "│   Rains    │",
                    "├────────────┤",
                    "│💰25 🕒08       │",
                    "│∞ 99999     │",
                    "├────────────┤",
                    "│ Gob  Lv1   │",
                    "│ ♥3  ⚔2      │",
                    "│------------│",
                    "│ Rast Lv3   │",
                    "│ ♥6  ⚔6      │",
                    "│------------│",
                    "│ Zo   Lv1   │",
                    "│ ♥2  ⚔8      │",
                    "└────────────┘"
            };

            for (String line : lines) {
                nvgText(vg, x, y, line);
                y += lineHeight;
            }
        }

        nvgEndFrame(vg);
    }

    @Override
    public void clear() {
        if (vg != 0) {
            nvgDelete(vg);
            vg = 0;
        }
    }


}
