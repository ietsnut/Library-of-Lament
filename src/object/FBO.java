package object;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import game.Manager;
import org.lwjgl.BufferUtils;
import resource.Mesh;

import static org.lwjgl.opengl.GL40.*;

public class FBO {

    public static int ID;

    public static final IntBuffer   DRAWBUFFERS = BufferUtils.createIntBuffer(2).put(GL_COLOR_ATTACHMENT0).put(GL_COLOR_ATTACHMENT1).flip();;

    public static final int[]       INDICES     = new int[] {0, 1, 2, 2, 1, 3};
    public static final byte[]      VERTICES    = new byte[] {-1, 1, -1, -1, 1, 1, 1, -1};

    public static final IntBuffer   IBUFFER     = BufferUtils.createIntBuffer(INDICES.length);
    public static final ByteBuffer  VBUFFER     = BufferUtils.createByteBuffer(VERTICES.length);

    public static void load() {

        glGenVertexArrays();
        glBindVertexArray(1);

        IBUFFER.put(INDICES);
        IBUFFER.flip();
        VBUFFER.put(VERTICES);
        VBUFFER.flip();

        glGenBuffers();

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 1);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, IBUFFER, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER, 2);
        glBufferData(GL_ARRAY_BUFFER, VBUFFER, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_BYTE, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glBindVertexArray(0);

        ID = glGenFramebuffers();

        frame(1);
        glBindFramebuffer(GL_FRAMEBUFFER, ID);
        glBindTexture(GL_TEXTURE_2D, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R32F, Manager.WIDTH, Manager.HEIGHT, 0, GL_RED, GL_FLOAT, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, 1, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        frame(2);
        glBindFramebuffer(GL_FRAMEBUFFER, ID);
        glBindTexture(GL_TEXTURE_2D, 2);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, Manager.WIDTH, Manager.HEIGHT, 0, GL_RGB, GL_FLOAT, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, 2, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        frame(3);
        glBindFramebuffer(GL_FRAMEBUFFER, ID);
        glBindTexture(GL_TEXTURE_2D,3);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F, Manager.WIDTH, Manager.HEIGHT, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, 3, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        glBindFramebuffer(GL_FRAMEBUFFER, ID);
        glDrawBuffers(DRAWBUFFERS);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, Manager.WIDTH, Manager.HEIGHT);
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("Framebuffer error: " + status);
            throw new RuntimeException("Framebuffer not complete");
        }
        System.out.println(status);

    }

    private static void frame(int id) {
        glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public static void unload() {
        glDeleteVertexArrays(1);
        glDeleteBuffers(new int[] {1, 2});
        glDeleteTextures(new int[] {1, 2, 3});
        glDeleteFramebuffers(ID);
        glDeleteBuffers(DRAWBUFFERS);
    }

}
