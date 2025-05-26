package object;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import engine.Console;
import engine.Manager;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL40.*;

public class FBO {

    public static int ID;

    public static final IntBuffer   DRAWBUFFERS = BufferUtils.createIntBuffer(2).put(GL_COLOR_ATTACHMENT0).put(GL_COLOR_ATTACHMENT1).flip();;

    public static final int[]       INDICES     = new int[] {0, 1, 2, 2, 1, 3};
    public static final byte[]      VERTICES    = new byte[] {-1, 1, -1, -1, 1, 1, 1, -1};

    public static final IntBuffer   IBUFFER     = BufferUtils.createIntBuffer(6);
    public static final ByteBuffer  VBUFFER     = BufferUtils.createByteBuffer(8);

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

        int texid = texture();
        glBindFramebuffer(GL_FRAMEBUFFER, ID);
        glBindTexture(GL_TEXTURE_2D, texid);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R32F, Manager.WIDTH, Manager.HEIGHT, 0, GL_RED, GL_FLOAT, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texid, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        texid = texture();
        glBindFramebuffer(GL_FRAMEBUFFER, ID);
        glBindTexture(GL_TEXTURE_2D, texid);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, Manager.WIDTH, Manager.HEIGHT, 0, GL_RGB, GL_FLOAT, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, texid, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        texid = texture();
        glBindFramebuffer(GL_FRAMEBUFFER, ID);
        glBindTexture(GL_TEXTURE_2D, texid);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F, Manager.WIDTH, Manager.HEIGHT, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, texid, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        glBindFramebuffer(GL_FRAMEBUFFER, ID);
        glDrawBuffers(DRAWBUFFERS);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, Manager.WIDTH, Manager.HEIGHT);
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            Console.error("FBO Not complete", Integer.toString(status));
            Manager.stop();
        }

    }

    private static int texture() {
        int id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D, 0);
        return id;
    }

    public static void unload() {
        glDeleteVertexArrays(1);
        glDeleteBuffers(new int[] {1, 2});
        glDeleteTextures(new int[] {1, 2, 3});
        glDeleteFramebuffers(ID);
        glDeleteBuffers(DRAWBUFFERS);
    }

}
