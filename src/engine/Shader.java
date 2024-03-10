package engine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import object.Camera;
import object.Light;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public abstract class Shader {

    public static final ArrayList<Shader> ALL = new ArrayList<>();

    private final int programID;
    private final int vertexShaderID;
    private final int fragmentShaderID;

    private static final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    private int location_projectionMatrix;
    private int location_viewMatrix;
    private int location_modelMatrix;

    public Shader(String type) {
        vertexShaderID = loadShader("resource/shader/" + type + "Vertex.glsl", GL20.GL_VERTEX_SHADER);
        fragmentShaderID = loadShader("resource/shader/" + type + "Fragment.glsl", GL20.GL_FRAGMENT_SHADER);
        programID = GL20.glCreateProgram();
        GL20.glAttachShader(programID, vertexShaderID);
        GL20.glAttachShader(programID, fragmentShaderID);
        bindAttributes();
        GL20.glLinkProgram(programID);
        GL20.glValidateProgram(programID);
        getAllUniformLocations();
        start();
        loadMatrix(location_projectionMatrix, Renderer.getProjectionMatrix());
        stop();
        ALL.add(this);
    }

    protected void getAllUniformLocations() {
        location_projectionMatrix = getUniformLocation("projectionMatrix");
        location_viewMatrix = getUniformLocation("viewMatrix");
        location_modelMatrix = getUniformLocation("modelMatrix");
    }

    protected int getUniformLocation(String uniformName){
        return GL20.glGetUniformLocation(programID,uniformName);
    }

    public void start(){
        GL20.glUseProgram(programID);
    }

    public void stop(){
        GL20.glUseProgram(0);
    }

    public static void clean() {
        for (Shader shader : Shader.ALL) {
            shader.stop();
            GL20.glDetachShader(shader.programID, shader.vertexShaderID);
            GL20.glDetachShader(shader.programID, shader.fragmentShaderID);
            GL20.glDeleteShader(shader.vertexShaderID);
            GL20.glDeleteShader(shader.fragmentShaderID);
            GL20.glDeleteProgram(shader.programID);
        }
    }

    protected abstract void bindAttributes();

    protected void bindAttribute(int attribute, String variableName){
        GL20.glBindAttribLocation(programID, attribute, variableName);
    }

    protected void loadFloat(int location, float value){
        GL20.glUniform1f(location, value);
    }

    protected void loadInt(int location, int value) {
        GL20.glUniform1i(location, value);
    }

    protected void loadVector(int location, Vector3f vector){
        GL20.glUniform3f(location,vector.x,vector.y,vector.z);
    }

    protected void loadBoolean(int location, boolean value){
        float toLoad = 0;
        if(value){
            toLoad = 1;
        }
        GL20.glUniform1f(location, toLoad);
    }

    protected void loadMatrix(int location, Matrix4f matrix){
        matrix.store(matrixBuffer);
        matrixBuffer.flip();
        GL20.glUniformMatrix4(location, false, matrixBuffer);
    }

    private static int loadShader(String file, int type){
        StringBuilder shaderSource = new StringBuilder();
        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while((line = reader.readLine())!=null){
                if (line.startsWith("#define LIGHTS")) {
                    line = "#define LIGHTS " + Light.ALL.size();
                }
                shaderSource.append(line).append("//\n");
            }
            reader.close();
        }catch(IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
        int shaderID = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderID, shaderSource);
        GL20.glCompileShader(shaderID);
        if(GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS )== GL11.GL_FALSE){
            System.out.println(GL20.glGetShaderInfoLog(shaderID, 500));
            System.err.println("Could not compile shader!");
            System.exit(-1);
        }
        return shaderID;
    }

    protected void loadViewMatrix(Matrix4f matrix) {
        loadMatrix(location_viewMatrix, matrix);
    }

    protected void loadModelMatrix(Matrix4f matrix) {
        loadMatrix(location_modelMatrix, matrix);
    }

    protected void loadProjectionMatrix(Matrix4f matrix) { loadMatrix(location_projectionMatrix, matrix); }

}