package shader;

import java.io.*;
import java.nio.FloatBuffer;
import java.util.*;

import engine.Console;
import engine.Manager;
import object.Camera;
import org.joml.*;
import resource.Material;
import window.Window;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL40.*;

public abstract class Shader<T> {

    public static final List<Shader<?>> ALL = new ArrayList<>();

    private final HashMap<String, Integer> uniforms = new HashMap<>();
    final String[] attributes;

    protected final int program, vertex, fragment;

    final Window window;

    public Shader(Window window, String type, String... attributes) {
        this.window = window;
        vertex      = loadShader("/resources/shader/" + type + "Vertex.glsl",     GL_VERTEX_SHADER);
        fragment    = loadShader("/resources/shader/" + type + "Fragment.glsl",   GL_FRAGMENT_SHADER);
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

    protected void uniform(String location, FloatBuffer buffer) {
        int uniform = uniforms.computeIfAbsent(location, loc -> glGetUniformLocation(program, loc));
        glUniformMatrix4fv(uniform, false, buffer);
    }

    public void uniform(String location, Serializable data) {
        int uniform = uniforms.computeIfAbsent(location, loc -> glGetUniformLocation(program, loc));
        switch (data) {
            case Float f:
                glUniform1f(uniform, f);
                break;
            case Integer i:
                glUniform1i(uniform, i);
                break;
            case Boolean b:
                glUniform1f(uniform, b ? 1.0f : 0.0f);
                break;
            case Vector2f v:
                glUniform2f(uniform, v.x, v.y);
                break;
            case Vector3f v:
                glUniform3f(uniform, v.x, v.y, v.z);
                break;
            case Vector4f v:
                glUniform4f(uniform, v.x, v.y, v.z, v.w);
                break;
            default:
                Console.error("Unsupported type", data.getClass().getSimpleName());
        }
    }

    protected abstract void shader(T object);

    public final void render(T object) {
        start();
        shader(object);
        stop();
    }

    public void start() {
        glUseProgram(program);
    }

    public void stop(){
        glUseProgram(0);
    }

    public static void clear() {
        for (Shader<?> shader : Shader.ALL) {
            shader.stop();
            glDetachShader(shader.program, shader.vertex);
            glDetachShader(shader.program, shader.fragment);
            glDeleteShader(shader.vertex);
            glDeleteShader(shader.fragment);
            glDeleteProgram(shader.program);
        }
    }

    private int loadShader(String file, int type) {
        StringBuilder shaderSource = new StringBuilder();
        try (InputStream in = getClass().getResourceAsStream(file)) {
            assert in != null;
            try (InputStreamReader bis = new InputStreamReader(in)) {
                BufferedReader reader = new BufferedReader(bis);
                String line;
                while((line = reader.readLine())!=null){
                    if (line.startsWith("#version")) {
                        shaderSource.append("#version ").append(glfwGetWindowAttrib(Manager.main.handle, GLFW_CONTEXT_VERSION_MAJOR)).append(glfwGetWindowAttrib(Manager.main.handle, GLFW_CONTEXT_VERSION_MINOR)).append("0 core").append("//\n");
                        shaderSource.append("#define WIDTH ").append(window.width).append("//\n");
                        shaderSource.append("#define HEIGHT ").append(window.height).append("//\n");
                        shaderSource.append("#define GRAYSCALE vec3(0.299, 0.587, 0.114)").append("//\n");
                        shaderSource.append("#define NEAR " + Camera.NEAR).append("//\n");
                        shaderSource.append("#define FAR " + Camera.FAR).append("//\n");
                        shaderSource.append("const vec4 PALETTE[").append(Material.MIYAZAKI_16.length).append("] = vec4[](\n");
                        for (int i = 0; i < Material.MIYAZAKI_16.length; i++) {
                            int color = Material.MIYAZAKI_16[i];
                            shaderSource.append(String.format(Locale.US,"    vec4(%.3f, %.3f, %.3f, 1.0)%s\n", ((color >> 16) & 0xFF) / 255.0f, ((color >> 8) & 0xFF) / 255.0f, ((color) & 0xFF) / 255.0f, (i < Material.MIYAZAKI_16.length - 1) ? "," : ""));
                        }
                        shaderSource.append(");\n");
                        shaderSource.append("const vec4 LINE = ").append(String.format(Locale.US, "vec4(%.3f, %.3f, %.3f, 1.0);\n", ((Material.LINE >> 16) & 0xFF) / 255.0f, ((Material.LINE >> 8) & 0xFF) / 255.0f, ((Material.LINE) & 0xFF) / 255.0f)).append("//\n");
                    } else {
                        shaderSource.append(line).append("//\n");
                    }
                }
                reader.close();
            }
        } catch (IOException e) {
            Console.error(e, "Failed to load", file);
        }
        int shaderID = glCreateShader(type);
        glShaderSource(shaderID, shaderSource);
        glCompileShader(shaderID);
        if(glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE){
            Console.error("Compilation failed.", this.getClass().getSimpleName(), glGetShaderInfoLog(shaderID, 500));
            System.exit(-1);
        }
        return shaderID;
    }

}