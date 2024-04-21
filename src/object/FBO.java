package object;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;

public class FBO extends Entity {

    public final int width;
    public final int height;

    public int frameBuffer;
    public int depthBuffer;

    public IntBuffer drawBuffers;

    public FBO(int width, int height) {
        super("fbo", false);
        this.width = width;
        this.height = height;
        frameBuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
        drawBuffers = BufferUtils.createIntBuffer(2).put(GL_COLOR_ATTACHMENT0).put(GL_COLOR_ATTACHMENT1).flip();
        glDrawBuffers(drawBuffers);

        Texture color = texture(new Texture()).textures.getLast();
        glBindTexture(GL11.GL_TEXTURE_2D, color.id);
        glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL_R32F, width, height, 0, GL_RED, GL_FLOAT, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D,color.id, 0);

        Texture normals = texture(new Texture()).textures.getLast();
        glBindTexture(GL_TEXTURE_2D, normals.id);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, width, height, 0, GL_RGBA, GL_FLOAT, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, normals.id, 0);

        depthBuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthBuffer);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer not complete");
        }
        load();
        post();
        bind();
        BOUND.add(this);
    }

    @Override
    public void load() {
        indices     = new int[] { 0, 1, 2, 2, 1, 3 };
        vertices    = new float[] { -1, 1, -1, -1, 1, 1, 1, -1 };
    }

}
