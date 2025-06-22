package resource;

import java.nio.ByteBuffer;
import engine.Console;
import engine.Manager;

import static org.lwjgl.opengl.GL40.*;

public class BloomFBO implements Resource {

    public int framebuffer;
    public int colorTexture;
    public int width, height;

    public BloomFBO(int width, int height) {
        this.width = width;
        this.height = height;
        this.queue();
    }

    @Override
    public void load() {

    }

    @Override
    public void unload() {

    }

    @Override
    public void bind() {
        this.framebuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, this.framebuffer);

        // Create RGB color texture for bloom
        colorTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, colorTexture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, width, height, 0, GL_RGB, GL_FLOAT, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorTexture, 0);

        glBindTexture(GL_TEXTURE_2D, 0);

        int[] drawBuffers = {GL_COLOR_ATTACHMENT0};
        glDrawBuffers(drawBuffers);

        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            Console.error("Bloom FBO Not complete", Integer.toString(status));
            Manager.stop();
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public void unbind() {
        if (colorTexture != 0) {
            glDeleteTextures(colorTexture);
        }
        if (framebuffer != 0) {
            glDeleteFramebuffers(framebuffer);
        }
    }

    @Override
    public void buffer() {

    }

    @Override
    public boolean loaded() {
        return true;
    }

    @Override
    public boolean binded() {
        return framebuffer != 0 && colorTexture != 0;
    }

    @Override
    public String toString() {
        return "BloomFBO";
    }
}