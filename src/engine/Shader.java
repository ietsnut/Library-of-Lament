package engine;

import java.io.*;
import java.nio.FloatBuffer;
import java.util.*;

import game.Game;
import game.Scene;
import object.Entity;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.*;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public abstract class Shader {

    public static final List<Shader> ALL = new ArrayList<>();
    public static final int LIGHTS = 64;

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

    final static FloatBuffer buffer = BufferUtils.createFloatBuffer(16);

    protected void uniform(String location, Serializable data) {
        int uniform = uniforms.computeIfAbsent(location, loc -> GL20.glGetUniformLocation(program, loc));
        switch (data) {
            case Float      f -> GL20.glUniform1f(uniform, f);
            case Integer    i -> GL20.glUniform1i(uniform, i);
            case Boolean    b -> GL20.glUniform1f(uniform, b ? 1.0f : 0.0f);
            case Vector2f   v -> GL20.glUniform2f(uniform, v.x, v.y);
            case Vector3f   v -> GL20.glUniform3f(uniform, v.x, v.y, v.z);
            case Vector4f   v -> GL20.glUniform4f(uniform, v.x, v.y, v.z, v.w);
            case Matrix4f   m -> {
                m.store(buffer);
                buffer.flip();
                GL20.glUniformMatrix4(uniform, false, buffer);
            }
            default -> throw new IllegalArgumentException("Unsupported uniform type: " + data.getClass());
        }
    }

    protected abstract void shader(Scene scene);

    protected final void render(Scene scene) {
        start();
        shader(scene);
        stop();
    }

    protected void render(Entity entity) {
        GL30.glBindVertexArray(entity.vaoID);
        for (int i = 0; i < attributes.length; i++) {
            GL20.glEnableVertexAttribArray(i);
        }
        for (int i = 0; i < entity.textures.size(); i++) {
            glActiveTexture(GL13.GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, entity.textures.get(i).id);
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

    protected void render(Entity.Collider collider) {
        GL30.glBindVertexArray(collider.vaoID);
        for (int i = 0; i < attributes.length; i++) {
            GL20.glEnableVertexAttribArray(i);
        }
        GL11.glDrawElements(GL11.GL_LINES, collider.indices.length, GL11.GL_UNSIGNED_INT, 0);
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
                shaderSource.append(line).append("//\n");
                if (line.startsWith("#version")) {
                    shaderSource.append("#define LIGHTS " + LIGHTS).append("//\n");
                    shaderSource.append("#define GRAYSCALE vec3(0.299, 0.587, 0.114)").append("//\n");
                    shaderSource.append("#define WIDTH " + Game.WIDTH).append("//\n");
                    shaderSource.append("#define HEIGHT " + Game.HEIGHT).append("//\n");
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