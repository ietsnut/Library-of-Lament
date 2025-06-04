package resource;

import java.nio.ByteBuffer;

import engine.Console;
import engine.Manager;

import static org.lwjgl.opengl.GL40.*;

public class FBO implements Resource {

    public int framebuffer;
    public final int[] textures;
    public final int[] buffers;

    public boolean bound = false;

    public FBO(int textures) {
        this.textures = new int[textures];
        this.buffers = new int[textures];
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

        for (int i = 0; i < textures.length; i++) {
            textures[i] = glGenTextures();
            buffers[i] = GL_COLOR_ATTACHMENT0 + i;
        }

        glBindTexture(GL_TEXTURE_2D, textures[0]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R8UI, Manager.main.width, Manager.main.height, 0, GL_RED_INTEGER, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textures[0], 0);

        glBindTexture(GL_TEXTURE_2D, textures[1]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, Manager.main.width, Manager.main.height, 0, GL_RGB, GL_FLOAT, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, textures[1], 0);

        glBindTexture(GL_TEXTURE_2D, textures[2]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F, Manager.main.width, Manager.main.height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, textures[2], 0);

        glBindTexture(GL_TEXTURE_2D, 0);
        glDrawBuffers(this.buffers);
        glViewport(0, 0, Manager.main.width, Manager.main.height);
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            Console.error("FBO Not complete", Integer.toString(status));
            Manager.stop();
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public void unbind() {
        glDeleteTextures(this.textures);
        glDeleteFramebuffers(this.framebuffer);
        glDeleteBuffers(this.buffers);
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
        return true;
    }

    @Override
    public String toString() {
        return "FBO";
    }

}
