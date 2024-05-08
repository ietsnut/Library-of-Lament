package object;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import game.Game;
import org.lwjgl.BufferUtils;
import property.Material;
import property.Mesh;
import property.Resource;

import static org.lwjgl.opengl.GL40.*;

public class FBO extends Entity {

    public int id;
    public IntBuffer drawBuffers;

    public FBO() {
        super();
        meshes.add(new Mesh() {
            @Override
            public void load() {
                indices = new int[]{0, 1, 2, 2, 1, 3};
                vertices = new byte[]{-1, 1, -1, -1, 1, 1, 1, -1};
            }
        });
        meshes.getFirst().direct();

        id = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, id);
        drawBuffers = BufferUtils.createIntBuffer(2).put(GL_COLOR_ATTACHMENT0).put(GL_COLOR_ATTACHMENT1).flip();
        glDrawBuffers(drawBuffers);

        Material color = new Material();
        glBindTexture(GL_TEXTURE_2D, color.texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R32F, Game.WIDTH, Game.HEIGHT, 0, GL_RED, GL_FLOAT, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, color.texture, 0);
        materials.add(color);

        Material normals = new Material();
        glBindTexture(GL_TEXTURE_2D, normals.texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, Game.WIDTH, Game.HEIGHT, 0, GL_RGB, GL_FLOAT, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, normals.texture, 0);
        materials.add(normals);

        Material depth = new Material();
        glBindTexture(GL_TEXTURE_2D, depth.texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F, Game.WIDTH, Game.HEIGHT, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depth.texture, 0);
        materials.add(depth);

        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, Game.WIDTH, Game.HEIGHT);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer not complete");
        }
    }

    @Override
    public void update() {

    }

}
