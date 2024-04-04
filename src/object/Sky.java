package object;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import property.Transformation;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Sky {

    //"right", "left", "top", "bottom", "back", "front"

    public List<Layer> layers = new ArrayList<>();
    private static final int sides = 12;

    public Sky(String name, int rings) {
        for (int i = 0; i < rings; i++) {
            layers.add(new Layer(name + "_" + i));
        }
    }

    public class Layer extends Entity {

        float height;

        public Layer(String name) {
            super(name);
            this.transformation = new Transformation() {
                @Override
                public Matrix4f model() {
                    if (layers.indexOf(Layer.this) % 2 == 0) {
                        transformation.rotation.y += (float) (System.currentTimeMillis() / 1e14) / (layers.indexOf(Layer.this) + 1);
                    } else {
                        transformation.rotation.y -= (float) (System.currentTimeMillis() / 1e14) / (layers.indexOf(Layer.this) + 1);
                    }
                    Matrix4f matrix = new Matrix4f();
                    matrix.setIdentity();
                    matrix.translate(transformation.position);
                    matrix.rotate((float) Math.toRadians(transformation.rotation.x), AXIS_X);
                    matrix.rotate((float) Math.toRadians(transformation.rotation.y), AXIS_Y);
                    matrix.rotate((float) Math.toRadians(transformation.rotation.z), AXIS_Z);
                    matrix.scale(new Vector3f(transformation.scale.x, -transformation.scale.y, transformation.scale.z));
                    return matrix;
                }
            };
            texture("resource/sky/" + name + ".png");
        }

        @Override
        protected void load(Object... args) {

            float radius = Terrain.SIZE + (layers.size() * (Terrain.SIZE/12));
            height = (float) (2 * radius * Math.sin(Math.PI / sides));
            transformation.position.y = height / 4;

            double angleStep = 2.0 * Math.PI / sides;

            vertices = new float[sides * 4 * 3];
            indices = new int[sides * 6];
            texCoords = new float[sides * 4 * 2];

            for (int i = 0; i < sides; i++) {
                double currentAngle = i * angleStep;
                double nextAngle = (i + 1) * angleStep;

                float x0 = (float)(radius * Math.cos(currentAngle));
                float z0 = (float)(radius * Math.sin(currentAngle));
                float x1 = (float)(radius * Math.cos(nextAngle));
                float z1 = (float)(radius * Math.sin(nextAngle));

                int baseVertexIndex = i * 4 * 3;
                vertices[baseVertexIndex] = x0;
                vertices[baseVertexIndex + 1] = -height / 2;
                vertices[baseVertexIndex + 2] = z0;
                vertices[baseVertexIndex + 3] = x1;
                vertices[baseVertexIndex + 4] = -height / 2;
                vertices[baseVertexIndex + 5] = z1;
                vertices[baseVertexIndex + 6] = x1;
                vertices[baseVertexIndex + 7] = height / 2;
                vertices[baseVertexIndex + 8] = z1;
                vertices[baseVertexIndex + 9] = x0;
                vertices[baseVertexIndex + 10] = height / 2;
                vertices[baseVertexIndex + 11] = z0;

                int baseIndexIndex = i * 6;
                int vertexOffset = i * 4;
                indices[baseIndexIndex] = vertexOffset;
                indices[baseIndexIndex + 1] = vertexOffset + 1;
                indices[baseIndexIndex + 2] = vertexOffset + 2;
                indices[baseIndexIndex + 3] = vertexOffset;
                indices[baseIndexIndex + 4] = vertexOffset + 2;
                indices[baseIndexIndex + 5] = vertexOffset + 3;

                int baseTexCoordIndex = i * 4 * 2;
                texCoords[baseTexCoordIndex] = 0.0f;
                texCoords[baseTexCoordIndex + 1] = 0.0f;
                texCoords[baseTexCoordIndex + 2] = 1.0f;
                texCoords[baseTexCoordIndex + 3] = 0.0f;
                texCoords[baseTexCoordIndex + 4] = 1.0f;
                texCoords[baseTexCoordIndex + 5] = 1.0f;
                texCoords[baseTexCoordIndex + 6] = 0.0f;
                texCoords[baseTexCoordIndex + 7] = 1.0f;

            }

        }

    }

}
