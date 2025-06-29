package resource;

import engine.Console;
import engine.Manager;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL40.*;

public final class Framebuffer implements Resource {

    private record Attachment(int internalFormat, int format, int type, boolean linear) {};

    public final int width, height;
    private final List<Attachment> attachments = new ArrayList<>();
    private final IntBuffer viewport = BufferUtils.createIntBuffer(4);
    boolean depth = false;

    public Framebuffer(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Framebuffer attach(int channels, int bits, boolean signed, boolean floating, boolean linear) {

        int internalFormat = 0;
        int format = 0;
        int type = 0;

        // Determine format (used for glTexImage2D, etc.)
        format = switch (channels) {
            case 1 -> GL_RED;
            case 2 -> GL_RG;
            case 3 -> GL_RGB;
            case 4 -> GL_RGBA;
            default -> throw new IllegalArgumentException("Unsupported channel count: " + channels);
        };

        if (!floating) {
            format = switch (channels) {
                case 1 -> GL_RED_INTEGER;
                case 2 -> GL_RG_INTEGER;
                case 3 -> GL_RGB_INTEGER;
                case 4 -> GL_RGBA_INTEGER;
                default -> format;
            };
        }

        if (floating) {
            type = GL_FLOAT;
            if (channels == 1 && bits == 16) internalFormat = GL_R16F;
            else if (channels == 1 && bits == 32) internalFormat = GL_R32F;
            else if (channels == 2 && bits == 16) internalFormat = GL_RG16F;
            else if (channels == 2 && bits == 32) internalFormat = GL_RG32F;
            else if (channels == 3 && bits == 16) internalFormat = GL_RGB16F;
            else if (channels == 3 && bits == 32) internalFormat = GL_RGB32F;
            else if (channels == 4 && bits == 16) internalFormat = GL_RGBA16F;
            else if (channels == 4 && bits == 32) internalFormat = GL_RGBA32F;
            else throw new IllegalArgumentException("Unsupported float format");
        } else {
            switch (bits) {
                case 8:
                    type = signed ? GL_BYTE : GL_UNSIGNED_BYTE;
                    if (channels == 1) internalFormat = signed ? GL_R8I : GL_R8UI;
                    else if (channels == 2) internalFormat = signed ? GL_RG8I : GL_RG8UI;
                    else if (channels == 3) internalFormat = signed ? GL_RGB8I : GL_RGB8UI;
                    else internalFormat = signed ? GL_RGBA8I : GL_RGBA8UI;
                    break;
                case 16:
                    type = signed ? GL_SHORT : GL_UNSIGNED_SHORT;
                    if (channels == 1) internalFormat = signed ? GL_R16I : GL_R16UI;
                    else if (channels == 2) internalFormat = signed ? GL_RG16I : GL_RG16UI;
                    else if (channels == 3) internalFormat = signed ? GL_RGB16I : GL_RGB16UI;
                    else internalFormat = signed ? GL_RGBA16I : GL_RGBA16UI;
                    break;
                case 32:
                    type = signed ? GL_INT : GL_UNSIGNED_INT;
                    if (channels == 1) internalFormat = signed ? GL_R32I : GL_R32UI;
                    else if (channels == 2) internalFormat = signed ? GL_RG32I : GL_RG32UI;
                    else if (channels == 3) internalFormat = signed ? GL_RGB32I : GL_RGB32UI;
                    else internalFormat = signed ? GL_RGBA32I : GL_RGBA32UI;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported bit depth: " + bits);
            }
        }

        attachments.add(new Attachment(internalFormat, format, type, linear));

        return this;

    }

    public Framebuffer depth(int bits, boolean stencil, boolean floating, boolean linear) {

        if (depth) {
            return this;
        }

        int internalFormat;
        int format;
        int type;

        if (stencil) {
            format = GL_DEPTH_STENCIL;
            type = GL_UNSIGNED_INT_24_8;
            internalFormat = GL_DEPTH24_STENCIL8;
        } else if (floating) {
            format = GL_DEPTH_COMPONENT;
            type = GL_FLOAT;
            internalFormat = GL_DEPTH_COMPONENT32F;
        } else {
            format = GL_DEPTH_COMPONENT;
            type = GL_UNSIGNED_INT;
            internalFormat = switch (bits) {
                case 16 -> GL_DEPTH_COMPONENT16;
                case 24 -> GL_DEPTH_COMPONENT24;
                case 32 -> GL_DEPTH_COMPONENT32;
                default -> throw new IllegalArgumentException("Unsupported depth bit count: " + bits);
            };
        }

        attachments.add(new Attachment(internalFormat, format, type, linear));

        depth = true;

        return this;

    }

    @Override
    public void bind() {
        glGetIntegerv(GL_VIEWPORT, viewport);
        glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);
        glViewport(0, 0, width, height);
    }

    @Override
    public void unbind() {
        glViewport(0, 0, viewport.get(2), viewport.get(3));
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void clear() {
        glClearColor(0, 0, 0, 0);
        glClear(GL_COLOR_BUFFER_BIT | (depth ? GL_DEPTH_BUFFER_BIT : 0));
    }

    public void clear(float r, float g, float b, float a) {
        glClearColor(r, g, b, a);
        glClear(GL_COLOR_BUFFER_BIT | (depth ? GL_DEPTH_BUFFER_BIT : 0));
    }

    public void bindTexture(int index, int textureUnit) {
        glActiveTexture(GL_TEXTURE0 + textureUnit);
        glBindTexture(GL_TEXTURE_2D, textures[index]);
    }

    public void unbindTexture(int textureUnit) {
        glActiveTexture(GL_TEXTURE0 + textureUnit);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    @Override
    public void load() {
        drawBuffers = new int[attachments.size()];
        textures    = new int[attachments.size()];
    }

    @Override
    public void unload() {

    }

    int framebufferId;
    public int[] textures;
    int[] drawBuffers;

    @Override
    public void link() {

        framebufferId = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);

        for (int i = 0; i < attachments.size(); i++) {

            Attachment attachment = attachments.get(i);

            textures[i] = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textures[i]);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            int minFilter = attachment.linear ? GL_LINEAR : GL_NEAREST;
            int magFilter = attachment.linear ? GL_LINEAR : GL_NEAREST;
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter);

            glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    attachment.internalFormat,
                    width,
                    height,
                    0,
                    attachment.format,
                    attachment.type,
                    (ByteBuffer) null
            );

            glBindTexture(GL_TEXTURE_2D, 0);

            // Determine attachment point
            int attachmentPoint;
            if (attachment.format == GL_DEPTH_STENCIL) {
                attachmentPoint = GL_DEPTH_STENCIL_ATTACHMENT;
            } else if (attachment.format == GL_DEPTH_COMPONENT) {
                attachmentPoint = GL_DEPTH_ATTACHMENT;
            } else {
                attachmentPoint = GL_COLOR_ATTACHMENT0 + i;
                drawBuffers[i] = attachmentPoint;
            }

            glFramebufferTexture2D(GL_FRAMEBUFFER, attachmentPoint, GL_TEXTURE_2D, textures[i], 0);
        }

        // Set draw buffers
        if (drawBuffers.length > 0) {
            glDrawBuffers(Arrays.stream(drawBuffers).filter(attachment -> attachment >= GL_COLOR_ATTACHMENT0).toArray());
        }
        // Check completeness
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            Console.error("FBO not complete", Integer.toString(status));
            Manager.stop();
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        attachments.clear();

    }

    @Override
    public void unlink() {
        glDeleteTextures(textures);
        glDeleteFramebuffers(framebufferId);
        glDeleteBuffers(Arrays.stream(drawBuffers).filter(attachment -> attachment >= GL_COLOR_ATTACHMENT0).toArray());
    }

    @Override
    public void buffer() {

    }

    @Override
    public boolean linked() {
        return framebufferId != 0;
    }

    @Override
    public String toString() {
        return "FBO [ " + width + "x" + height + ", " + attachments.size() + " attachments ]";
    }
}