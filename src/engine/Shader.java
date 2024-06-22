package engine;

import java.io.*;
import java.nio.FloatBuffer;
import java.util.*;

import game.Game;
import game.Scene;
import object.Camera;
import property.Entity;
import org.joml.*;
import org.lwjgl.BufferUtils;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL40.*;

public abstract class Shader {

    public static final List<Shader> ALL = new ArrayList<>();
    public static final byte LIGHTS = Byte.MAX_VALUE;

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

    protected void uniform(String location, float data) {
        int uniform = uniforms.computeIfAbsent(location, loc -> glGetUniformLocation(program, loc));
        glUniform1f(uniform, data);
    }

    protected void uniform(String location, int data) {
        int uniform = uniforms.computeIfAbsent(location, loc -> glGetUniformLocation(program, loc));
        glUniform1i(uniform, data);
    }

    protected void uniform(String location, boolean data) {
        int uniform = uniforms.computeIfAbsent(location, loc -> glGetUniformLocation(program, loc));
        glUniform1f(uniform, data ? 1.0f : 0.0f);
    }

    protected void uniform(String location, Vector2f data) {
        int uniform = uniforms.computeIfAbsent(location, loc -> glGetUniformLocation(program, loc));
        glUniform2f(uniform, data.x, data.y);
    }

    protected void uniform(String location, Vector3f data) {
        int uniform = uniforms.computeIfAbsent(location, loc -> glGetUniformLocation(program, loc));
        glUniform3f(uniform, data.x, data.y, data.z);
    }

    protected void uniform(String location, Vector4f data) {
        int uniform = uniforms.computeIfAbsent(location, loc -> glGetUniformLocation(program, loc));
        glUniform4f(uniform, data.x, data.y, data.z, data.w);
    }

    protected void uniform(String location, Matrix4f data) {
        int uniform = uniforms.computeIfAbsent(location, loc -> glGetUniformLocation(program, loc));
        data.get(buffer);
        glUniformMatrix4fv(uniform, false, buffer);
    }

    protected abstract void shader(Scene scene);

    protected final void render(Scene scene) {
        start();
        shader(scene);
        stop();
    }

    protected abstract void render(Entity entity);

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

    private static int loadShader(String file, int type) {
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