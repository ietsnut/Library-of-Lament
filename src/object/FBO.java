package object;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import game.Game;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import static org.lwjgl.opengl.GL40.*;

public class FBO extends Entity {

    public int width;
    public int height;

    public int frameBuffer;
    public int depthBuffer;

    public IntBuffer drawBuffers;

    public FBO(int width, int height) {
        super();
        this.width = width;
        this.height = height;
        direct();
    }

    @Override
    public void unbind() {
        super.unbind();
        glDeleteFramebuffers(frameBuffer);
        glDeleteFramebuffers(drawBuffers);
        glDeleteRenderbuffers(depthBuffer);
    }

    @Override
    public void preload() {
        frameBuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
        drawBuffers = BufferUtils.createIntBuffer(2).put(GL_COLOR_ATTACHMENT1).put(GL_COLOR_ATTACHMENT0).flip();
        glDrawBuffers(drawBuffers);

        Texture color = new Texture();
        glBindTexture(GL_TEXTURE_2D, color.id);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RG32F, width, height, 0, GL_RG, GL_FLOAT, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, color.id, 0);
        textures.add(color);

        Texture normals = new Texture();
        glBindTexture(GL_TEXTURE_2D, normals.id);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, width, height, 0, GL_RGBA, GL_FLOAT, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, normals.id, 0);
        textures.add(normals);

        depthBuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthBuffer);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glViewport(0, 0, Game.WIDTH, Game.HEIGHT);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer not complete");
        }
    }

    @Override
    public void load() {
        indices     = new int[] { 0, 1, 2, 2, 1, 3 };
        vertices    = new byte[] { -1, 1, -1, -1, 1, 1, 1, -1 };
    }

}
