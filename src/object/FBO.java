package object;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import engine.Console;
import engine.Manager;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL40.*;

public class FBO {

    public static void load() {

        glBindFramebuffer(GL_FRAMEBUFFER, glGenFramebuffers());

        glBindTexture(GL_TEXTURE_2D, glGenTextures());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R8UI, Manager.windows[0].width, Manager.windows[0].height, 0, GL_RED_INTEGER, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, 1, 0);

        glBindTexture(GL_TEXTURE_2D, glGenTextures());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, Manager.windows[0].width, Manager.windows[0].height, 0, GL_RGB, GL_FLOAT, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, 2, 0);

        glBindTexture(GL_TEXTURE_2D, glGenTextures());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F, Manager.windows[0].width, Manager.windows[0].height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, 3, 0);

        glBindTexture(GL_TEXTURE_2D, 0);
        glDrawBuffers(new int[] {GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1});
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, Manager.windows[0].width, Manager.windows[0].height);
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            Console.error("FBO Not complete", Integer.toString(status));
            Manager.stop();
        }

    }

    public static void unload() {
        glDeleteVertexArrays(1);
        glDeleteBuffers(new int[] {1, 2});
        glDeleteTextures(new int[] {1, 2, 3});
        glDeleteFramebuffers(1);
        glDeleteBuffers(new int[] {GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1});
    }

}
