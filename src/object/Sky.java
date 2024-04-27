package object;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

public class Sky {

    public List<Layer> layers = new ArrayList<>();
    private static final int sides = 12;

    public Sky(String name, int rings) {
        for (int i = 0; i < rings; i++) {
            layers.add(new Layer(name + i));
        }
    }

    public class Layer extends Entity {

        float height;
        float radius;

        public Layer(String name) {
            super(name, false);
            radius = Terrain.SIZE + (layers.size() * (Terrain.SIZE/12));
            height = (float) (2 * radius * Math.sin(Math.PI / sides));
            position.y = height / 4;
            texture(new Texture("sky", name));
            flip(Axis.Y);
            enqueue();
        }

        @Override
        public Matrix4f model() {
            if (layers.indexOf(Layer.this) % 2 == 0) {
                rotate(Axis.Y, (float) (System.currentTimeMillis() / 1e14) / (layers.indexOf(Layer.this) + 1));
            } else {
                rotate(Axis.Y, -(float) (System.currentTimeMillis() / 1e14) / (layers.indexOf(Layer.this) + 1));
            }
            return super.model();
        }

        @Override
        public void load() {

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
