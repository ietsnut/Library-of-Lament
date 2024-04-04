package engine;

import java.io.*;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.*;

import game.Scene;
import object.AABB;
import object.Entity;
import object.Light;
import object.Texture;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.*;
import property.Transformation;
import tool.Noise;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public abstract class Shader {

    public static final List<Shader> ALL = new ArrayList<>();

    private final HashMap<String, Integer> uniforms = new HashMap<>();
    private final String[] attributes;

    private final int program, vertex, fragment;

    public Shader(String type, String... attributes) {
        vertex      = loadShader("resource/shader/" + type + "Vertex.glsl",     GL20.GL_VERTEX_SHADER);
        fragment    = loadShader("resource/shader/" + type + "Fragment.glsl",   GL20.GL_FRAGMENT_SHADER);
        program     = GL20.glCreateProgram();
        GL20.glAttachShader(program, vertex);
        GL20.glAttachShader(program, fragment);
        this.attributes = attributes;
        for (int i = 0; i < attributes.length; i++) {
            GL20.glBindAttribLocation(program, i, attributes[i]);
        }
        GL20.glLinkProgram(program);
        GL20.glValidateProgram(program);
        ALL.add(this);
    }

    protected void uniform(String location, Serializable data) {
        int uniform = uniforms.computeIfAbsent(location, loc -> GL20.glGetUniformLocation(program, loc));
        switch (data) {
            case Float      f -> GL20.glUniform1f(uniform, f);
            case Integer    i -> GL20.glUniform1i(uniform, i);
            case Boolean    b -> GL20.glUniform1f(uniform, b ? 1.0f : 0.0f);
            case Vector2f   v -> GL20.glUniform2f(uniform, v.x, v.y);
            case Vector3f   v -> GL20.glUniform3f(uniform, v.x, v.y, v.z);
            case Vector4f   v -> GL20.glUniform4f(uniform, v.x, v.y, v.z, v.w);
            case Matrix4f   m -> GL20.glUniformMatrix4(uniform, false, matrix(m));
            default -> throw new IllegalArgumentException("Unsupported uniform type: " + data.getClass());
        }
    }

    final static FloatBuffer buffer = BufferUtils.createFloatBuffer(16);

    private FloatBuffer matrix(Matrix4f matrix) {
        matrix.store(buffer);
        buffer.flip();
        return buffer;
    }

    protected abstract void shader(Scene scene);

    protected final void render(Scene scene) {
        start();
        shader(scene);
        stop();
    }

    protected final void render(Entity entity) {
        GL30.glBindVertexArray(entity.vaoID);
        for (int i = 0; i < attributes.length; i++) {
            GL20.glEnableVertexAttribArray(i);
        }
        for (int i = 0; i < entity.textures.size(); i++) {
            glActiveTexture(GL13.GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, entity.textures.get(i).ID);
        }
        GL11.glDrawElements(GL11.GL_TRIANGLES, entity.indices.length, GL11.GL_UNSIGNED_INT, 0);
        for (int i = 0; i < entity.textures.size(); i++) {
            glActiveTexture(GL13.GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        for (int i = 0; i < attributes.length; i++) {
            GL20.glDisableVertexAttribArray(i);
        }
        GL30.glBindVertexArray(0);
    }

    protected final void render(AABB aabb) {
        GL30.glBindVertexArray(aabb.vaoID);
        for (int i = 0; i < attributes.length; i++) {
            GL20.glEnableVertexAttribArray(i);
        }
        GL11.glDrawElements(GL11.GL_LINES, aabb.indices.length, GL11.GL_UNSIGNED_INT, 0);
        for (int i = 0; i < attributes.length; i++) {
            GL20.glDisableVertexAttribArray(i);
        }
        GL30.glBindVertexArray(0);
    }

    public void start() {
        GL20.glUseProgram(program);
    }

    public void stop(){
        GL20.glUseProgram(0);
    }

    public static void clean() {
        for (Shader shader : Shader.ALL) {
            shader.stop();
            GL20.glDetachShader(shader.program, shader.vertex);
            GL20.glDetachShader(shader.program, shader.fragment);
            GL20.glDeleteShader(shader.vertex);
            GL20.glDeleteShader(shader.fragment);
            GL20.glDeleteProgram(shader.program);
        }
    }

    private static int loadShader(String file, int type){
        StringBuilder shaderSource = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while((line = reader.readLine())!=null){
                if (line.startsWith("#define LIGHTS 2")) {
                    line = "#define LIGHTS " + Light.ALL.size();
                }
                shaderSource.append(line).append("//\n");
                if (line.startsWith("#version")) {
                    shaderSource.append("#define GRAYSCALE vec3(0.299, 0.587, 0.114)").append("//\n");
                }
            }
            reader.close();
        } catch (IOException e){
            System.exit(-1);
            throw new RuntimeException(e);
        }
        int shaderID = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderID, shaderSource);
        GL20.glCompileShader(shaderID);
        if(GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE){
            System.out.println(GL20.glGetShaderInfoLog(shaderID, 500));
            System.err.println("Could not compile shader!");
            System.exit(-1);
        }
        return shaderID;
    }

}