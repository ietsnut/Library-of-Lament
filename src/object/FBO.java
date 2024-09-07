package object;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import game.Manager;
import org.lwjgl.BufferUtils;
import resource.Material;
import resource.Mesh;

import static org.lwjgl.opengl.GL40.*;

public class FBO {

    public static int ID;
    public static final IntBuffer   DRAWBUFFERS = BufferUtils.createIntBuffer(2).put(GL_COLOR_ATTACHMENT0).put(GL_COLOR_ATTACHMENT1).flip();;
    public static final Material[]  MATERIALS   = new Material[3];
    public static final Mesh        MESH        = new Mesh() {
        @Override
        public void load() {
            indices = new int[]{0, 1, 2, 2, 1, 3};
            vertices = new byte[]{-1, 1, -1, -1, 1, 1, 1, -1};
        }
    };

    static {
        ID = glGenFramebuffers();
        Material color = new Material();
        glBindFramebuffer(GL_FRAMEBUFFER, FBO.ID);
        glBindTexture(GL_TEXTURE_2D, color.texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R32F, Manager.WIDTH, Manager.HEIGHT, 0, GL_RED, GL_FLOAT, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, color.texture, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        MATERIALS[0] = color;
        Material normals = new Material();
        glBindFramebuffer(GL_FRAMEBUFFER, FBO.ID);
        glBindTexture(GL_TEXTURE_2D, normals.texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, Manager.WIDTH, Manager.HEIGHT, 0, GL_RGB, GL_FLOAT, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, normals.texture, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        MATERIALS[1] = normals;
        Material depth = new Material();
        glBindFramebuffer(GL_FRAMEBUFFER, FBO.ID);
        glBindTexture(GL_TEXTURE_2D, depth.texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F, Manager.WIDTH, Manager.HEIGHT, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depth.texture, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        MATERIALS[2] = depth;
        glBindFramebuffer(GL_FRAMEBUFFER, ID);
        glDrawBuffers(DRAWBUFFERS);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, Manager.WIDTH, Manager.HEIGHT);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer not complete");
        }
    }

}
