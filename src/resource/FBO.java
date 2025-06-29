package resource;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import engine.Console;
import engine.Manager;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.opengl.GL44.glClearTexImage;

public class FBO implements Resource {

    public int framebuffer;
    public final int[] textures;
    public final int[] buffers;

    public int width, height;

    public FBO(int textures, int width, int height) {
        this.textures = new int[textures + 1];
        this.buffers = new int[textures];
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
    public void link() {

        this.framebuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, this.framebuffer);

        for (int i = 0; i < textures.length - 1; i++) {
            textures[i] = glGenTextures();
            buffers[i] = GL_COLOR_ATTACHMENT0 + i;
            glBindTexture(GL_TEXTURE_2D, textures[i]);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_R8UI, width, height, 0, GL_RED_INTEGER, GL_UNSIGNED_BYTE, (ByteBuffer) null);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textures[i], 0);
        }

        textures[textures.length - 1] = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textures[textures.length - 1]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F, width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, textures[textures.length - 1], 0);

        glBindTexture(GL_TEXTURE_2D, 0);
        glDrawBuffers(this.buffers);
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            Console.error("FBO Not complete", Integer.toString(status));
            Manager.stop();
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

    }

    @Override
    public void unlink() {
        glDeleteTextures(this.textures);
        glDeleteFramebuffers(this.framebuffer);
        glDeleteBuffers(this.buffers);
    }

    @Override
    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, this.framebuffer);
        glViewport(0, 0, width, height);
    }

    @Override
    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public void buffer() {

    }

    @Override
    public boolean linked() {
        return framebuffer != 0 &&
                java.util.Arrays.stream(textures).allMatch(t -> t != 0) &&
                java.util.Arrays.stream(buffers).allMatch(b -> b != 0);
    }

    @Override
    public String toString() {
        return "FBO";
    }

}
