package object;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import game.Game;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import static org.lwjgl.opengl.GL40.*;

public class FBO extends Entity {

    public int frameBuffer;

    public IntBuffer drawBuffers;

    public FBO() {
        super();

        frameBuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
        drawBuffers = BufferUtils.createIntBuffer(2).put(GL_COLOR_ATTACHMENT0).put(GL_COLOR_ATTACHMENT1).flip();
        glDrawBuffers(drawBuffers);

        Texture color = new Texture();
        glBindTexture(GL_TEXTURE_2D, color.id);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R32F, Game.WIDTH, Game.HEIGHT, 0, GL_RED, GL_FLOAT, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, color.id, 0);
        textures.add(color);

        Texture normals = new Texture();
        glBindTexture(GL_TEXTURE_2D, normals.id);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, Game.WIDTH, Game.HEIGHT, 0, GL_RGB, GL_FLOAT, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, normals.id, 0);
        textures.add(normals);

        Texture depth = new Texture();
        glBindTexture(GL_TEXTURE_2D, depth.id);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F, Game.WIDTH, Game.HEIGHT, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depth.id, 0);
        textures.add(depth);

        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, Game.WIDTH, Game.HEIGHT);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer not complete");
        }

        direct();
    }

    @Override
    protected Matrix4f model() {
        return null;
    }

    @Override
    public void load() {
        indices     = new int[] { 0, 1, 2, 2, 1, 3 };
        vertices    = new byte[] { -1, 1, -1, -1, 1, 1, 1, -1 };
    }

    @Override
    public void unbind() {
        super.unbind();
        glDeleteFramebuffers(frameBuffer);
        glDeleteBuffers(drawBuffers);
    }

}
