package engine;

import java.io.*;
import java.nio.FloatBuffer;
import java.util.*;

import game.Game;
import game.Scene;
import object.Camera;
import object.Entity;
import org.joml.*;
import org.lwjgl.BufferUtils;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL40.*;

public abstract class Shader {

    public static final List<Shader> ALL = new ArrayList<>();
    public static final int LIGHTS = Byte.MAX_VALUE;

    private final HashMap<String, Integer> uniforms = new HashMap<>();
    final String[] attributes;

    protected final int program, vertex, fragment;

    public Shader(String type, String... attributes) {
        vertex      = loadShader("resource/shader/" + type + "Vertex.glsl",     GL_VERTEX_SHADER);
        fragment    = loadShader("resource/shader/" + type + "Fragment.glsl",   GL_FRAGMENT_SHADER);
        program     = glCreateProgram();
        glAttachShader(program, vertex);
        glAttachShader(program, fragment);
        this.attributes = attributes;
        for (int i = 0; i < attributes.length; i++) {
            glBindAttribLocation(program, i, attributes[i]);
        }
        glLinkProgram(program);
        glValidateProgram(program);
        ALL.add(this);

    }

    final static FloatBuffer buffer = BufferUtils.createFloatBuffer(16);

    protected void uniform(String location, Object data) {
        int uniform = uniforms.computeIfAbsent(location, loc -> glGetUniformLocation(program, loc));
        switch (data) {
            case Float      f -> glUniform1f(uniform, f);
            case Integer    i -> glUniform1i(uniform, i);
            case Boolean    b -> glUniform1f(uniform, b ? 1.0f : 0.0f);
            case Vector2f   v -> glUniform2f(uniform, v.x, v.y);
            case Vector3f   v -> glUniform3f(uniform, v.x, v.y, v.z);
            case Vector4f   v -> glUniform4f(uniform, v.x, v.y, v.z, v.w);
            case Matrix4f   m -> {
                m.get(buffer);
                glUniformMatrix4fv(uniform, false, buffer);
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
        glBindVertexArray(entity.vao);
        for (int i = 0; i < attributes.length; i++) {
            glEnableVertexAttribArray(i);
        }
        for (int i = 0; i < entity.textures.size(); i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, entity.textures.get(i).id);
            uniform("texture" + (i + 1), i);
        }
        glDrawElements(entity instanceof Entity.Collider ? GL_LINES : GL_TRIANGLES, entity.indices.length, GL_UNSIGNED_INT, 0);
        for (int i = 0; i < entity.textures.size(); i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        for (int i = 0; i < attributes.length; i++) {
            glDisableVertexAttribArray(i);
        }
        glBindVertexArray(0);
    }

    public void start() {
        glUseProgram(program);
    }

    public void stop(){
        glUseProgram(0);
    }

    public static void unload() {
        for (Shader shader : Shader.ALL) {
            shader.stop();
            glDetachShader(shader.program, shader.vertex);
            glDetachShader(shader.program, shader.fragment);
            glDeleteShader(shader.vertex);
            glDeleteShader(shader.fragment);
            glDeleteProgram(shader.program);
        }
    }

    private static int loadShader(String file, int type){
        StringBuilder shaderSource = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while((line = reader.readLine())!=null){
                if (line.startsWith("#version")) {
                    shaderSource.append("#version ").append(glfwGetWindowAttrib(Game.window, GLFW_CONTEXT_VERSION_MAJOR)).append(glfwGetWindowAttrib(Game.window, GLFW_CONTEXT_VERSION_MINOR)).append("0 core").append("//\n");
                    shaderSource.append("#define LIGHTS " + LIGHTS).append("//\n");
                    shaderSource.append("#define GRAYSCALE vec3(0.299, 0.587, 0.114)").append("//\n");
                    shaderSource.append("#define WIDTH " + Game.WIDTH).append("//\n");
                    shaderSource.append("#define HEIGHT " + Game.HEIGHT).append("//\n");
                    shaderSource.append("#define NEAR " + Camera.NEAR).append("//\n");
                    shaderSource.append("#define FAR " + Camera.FAR).append("//\n");
                } else {
                    shaderSource.append(line).append("//\n");
                }

            }
            reader.close();
        } catch (IOException e){
            System.exit(-1);
            throw new RuntimeException(e);
        }
        int shaderID = glCreateShader(type);
        glShaderSource(shaderID, shaderSource);
        glCompileShader(shaderID);
        if(glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE){
            System.out.println(glGetShaderInfoLog(shaderID, 500));
            System.err.println("Could not compile shader!");
            System.exit(-1);
        }
        return shaderID;
    }

}