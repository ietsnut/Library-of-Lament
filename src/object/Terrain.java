package object;

import org.joml.*;
import org.joml.Math;

import java.awt.image.BufferedImage;

public class Terrain extends Entity {

    public static final float SIZE = 128;
    public static final float MAX_HEIGHT = 8;
    public static final float MAX_PIXEL_COLOUR = 256 * 256 * 256;
    final int VERTEXS;

    public float[][] heights;

    public Terrain(String name, int tiles) {
        super(name, false);
        this.position.x -= (SIZE / 2);
        this.position.z -= (SIZE / 2);
        BufferedImage hMap = Texture.load("terrain/" + name + "_h");
        VERTEXS = hMap.getHeight();
        heights = new float[VERTEXS][VERTEXS];
        for (int i = 0; i < VERTEXS; i++) {
            for (int j = 0; j < VERTEXS; j++) {
                heights[j][i] = getHeight(j, i, hMap);
            }
        }
        //texture(new Texture("terrain", name));
        enqueue();
    }

    @Override
    public void load() {
        int count = VERTEXS * VERTEXS;
        vertices = new float[count * 3];
        normals = new float[count * 3];
        texCoords = new float[count * 2];
        indices = new int[6 * (VERTEXS - 1) * (VERTEXS - 1)];
        int vertexPointer = 0;
        for (int i = 0; i < VERTEXS; i++) {
            for (int j = 0; j < VERTEXS; j++) {
                vertices[vertexPointer * 3] = (float) j / ((float) VERTEXS - 1) * SIZE;
                vertices[vertexPointer * 3 + 1] = heights[j][i];
                vertices[vertexPointer * 3 + 2] = (float) i / ((float) VERTEXS - 1) * SIZE;
                normals[vertexPointer * 3] = 0;
                normals[vertexPointer * 3 + 1] = 1;
                normals[vertexPointer * 3 + 2] = 0;
                texCoords[vertexPointer * 2] = (float) j / ((float) VERTEXS - 1);
                texCoords[vertexPointer * 2 + 1] = (float) i / ((float) VERTEXS - 1);
                vertexPointer++;
            }
        }
        int pointer = 0;
        for (int gz = 0; gz < VERTEXS - 1; gz++) {
            for (int gx = 0; gx < VERTEXS - 1; gx++) {
                int topLeft = (gz * VERTEXS) + gx;
                int topRight = topLeft + 1;
                int bottomLeft = ((gz + 1) * VERTEXS) + gx;
                int bottomRight = bottomLeft + 1;
                indices[pointer++] = topLeft;
                indices[pointer++] = bottomLeft;
                indices[pointer++] = topRight;
                indices[pointer++] = topRight;
                indices[pointer++] = bottomLeft;
                indices[pointer++] = bottomRight;
            }
        }
        model();
    }

    private float getHeight(int x, int z, BufferedImage image) {
        if (x < 0 || x >= image.getHeight() || z < 0 || z >= image.getHeight()) {
            return 0;
        }
        float height = image.getRGB(x, z);
        height += MAX_PIXEL_COLOUR / 2f;
        height /= MAX_PIXEL_COLOUR / 2f;
        height *= MAX_HEIGHT;
        return height;
    }

    public float height(float worldX, float worldZ) {
        float terrainX = worldX - this.position.x;
        float terrainZ = worldZ - this.position.z;
        float gridSquareSize = SIZE / ((float) heights.length - 1);
        int gridX = (int) Math.floor(terrainX / gridSquareSize);
        int gridZ = (int) Math.floor(terrainZ / gridSquareSize);
        if (gridX + 1 >= heights.length || gridZ + 1 >= heights.length || gridX < 0 || gridZ < 0) {
            return 0;
        }
        float xCoord = (terrainX % gridSquareSize) / gridSquareSize;
        float zCoord = (terrainZ % gridSquareSize) / gridSquareSize;
        if (xCoord <= (1 - zCoord)) {
            return barryCentric(new Vector3f(0, heights[gridX][gridZ], 0), new Vector3f(1, heights[gridX + 1][gridZ], 0), new Vector3f(0, heights[gridX][gridZ + 1], 1), new Vector2f(xCoord, zCoord));
        } else {
            return barryCentric(new Vector3f(1, heights[gridX + 1][gridZ], 0), new Vector3f(1, heights[gridX + 1][gridZ + 1], 1), new Vector3f(0, heights[gridX][gridZ + 1], 1), new Vector2f(xCoord, zCoord));
        }
    }

    private float barryCentric(Vector3f p1, Vector3f p2, Vector3f p3, Vector2f pos) {
        float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
        float l1 = ((p2.z - p3.z) * (pos.x - p3.x) + (p3.x - p2.x) * (pos.y - p3.z)) / det;
        float l2 = ((p3.z - p1.z) * (pos.x - p3.x) + (p1.x - p3.x) * (pos.y - p3.z)) / det;
        float l3 = 1.0f - l1 - l2;
        return l1 * p1.y + l2 * p2.y + l3 * p3.y;
    }

    @Override
    public Matrix4f model() {
        return this.model.identity().translate(new Vector3f(position.x, 0, position.z));
    }

}

