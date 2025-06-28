package property;

import engine.Console;
import resource.Mesh;
import resource.Calculation;
import object.Camera;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opencl.CL10.*;

public class Terrain extends Entity {

    private final Mesh terrain;

    // OpenCL resources
    private final Calculation calculation;
    private boolean openCLInitialized = false;
    private long vertexBuffer = 0;
    private long indexBuffer = 0;
    private long resultBuffer = 0;
    private long movementResultBuffer = 0;  // Separate buffer for movement results
    private static final String PROGRAM_NAME = "terrain";
    private static final String HEIGHT_KERNEL = "calculate_height";
    private static final String HEIGHT_MOVEMENT_KERNEL = "calculate_height_movement";

    // Cache for terrain data
    private int lastVertexCount = -1;
    private int lastIndexCount = -1;

    public Terrain(String name) {
        super(name);
        terrain = meshes[state];
        calculation = Calculation.getInstance();
        new Thread(() -> {
            meshes[state].load();
            if (calculation.isInitialized()) {
                openCLInitialized = calculation.loadProgram(PROGRAM_NAME, "terrain.cl");
                if (openCLInitialized) {
                    initializeBuffers();
                }
            }
        }).start();
        this.update();
    }

    private void initializeBuffers() {
        if (!openCLInitialized) return;

        try {
            // Create persistent buffers for terrain data
            // Convert byte vertices to float for accurate calculations
            FloatBuffer vertexData = BufferUtils.createFloatBuffer(terrain.vertices.length);
            for (int i = 0; i < terrain.vertices.length; i++) {
                vertexData.put((float) terrain.vertices[i]);
            }
            vertexData.flip();

            // Convert FloatBuffer to ByteBuffer for the createBuffer method
            ByteBuffer vertexByteBuffer = BufferUtils.createByteBuffer(vertexData.capacity() * Float.BYTES);
            vertexByteBuffer.asFloatBuffer().put(vertexData);
            vertexData.rewind();

            vertexBuffer = calculation.createBuffer(CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, vertexByteBuffer);

            IntBuffer indexData = BufferUtils.createIntBuffer(terrain.indices.length);
            indexData.put(terrain.indices).flip();
            indexBuffer = calculation.createBuffer(CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, indexData);

            // Result buffer for height calculations (max 1 result)
            resultBuffer = calculation.createBuffer(CL_MEM_WRITE_ONLY, Float.BYTES * 4);

            // Movement result buffer - stores final position (x,y,z) and success flag
            movementResultBuffer = calculation.createBuffer(CL_MEM_WRITE_ONLY, Float.BYTES * 4);

            lastVertexCount = terrain.vertices.length;
            lastIndexCount = terrain.indices.length;

        } catch (Exception e) {
            Console.error("Failed to initialize OpenCL buffers for terrain:", e.getMessage());
            openCLInitialized = false;
            cleanup();
        }
    }

    private void updateBuffersIfNeeded() {
        if (!openCLInitialized) return;

        // Check if terrain data has changed
        if (terrain.vertices.length != lastVertexCount || terrain.indices.length != lastIndexCount) {
            cleanup();
            initializeBuffers();
        }
    }

    private final int[] idx = new int[3];
    private final float[] x = new float[3];
    private final float[] y = new float[3];
    private final float[] z = new float[3];
    private final float[] bary = new float[3];

    private final Vector3f edgeVector = new Vector3f();
    private final Vector3f movementDirection = new Vector3f();
    private final Vector3f projectedMovement = new Vector3f();
    private final Vector3f newPosition = new Vector3f();
    private final Vector3f tempPos = new Vector3f();
    private final Vector3f v0 = new Vector3f();
    private final Vector3f v1 = new Vector3f();
    private final Vector3f faceNormal = new Vector3f();

    public float height(float xQuery, float zQuery) {
        // Try OpenCL first
        if (openCLInitialized && vertexBuffer != 0 && indexBuffer != 0) {
            Float result = heightOpenCL(xQuery, zQuery);
            if (result != null) {
                return result;
            }
        }

        // Fallback to CPU implementation
        for (int t = 0; t < terrain.indices.length; t += 3) {
            for (int i = 0; i < 3; i++) {
                idx[i] = terrain.indices[t + i] * 3;
                x[i] = terrain.vertices[idx[i]];
                y[i] = terrain.vertices[idx[i] + 1];
                z[i] = terrain.vertices[idx[i] + 2];
            }
            bary(x[0], z[0], x[1], z[1], x[2], z[2], xQuery, zQuery, bary);
            if (inside(bary)) {
                return bary[0] * y[0] + bary[1] * y[1] + bary[2] * y[2] + 1.7f;
            }
        }
        return 0;
    }

    private Float heightOpenCL(float xQuery, float zQuery) {
        try {
            updateBuffersIfNeeded();

            long kernel = calculation.getKernel(PROGRAM_NAME, HEIGHT_KERNEL);
            long commandQueue = calculation.getCommandQueue();

            if (kernel == 0 || commandQueue == 0) {
                return null;
            }

            // Set kernel arguments
            calculation.setKernelArg(kernel, 0, vertexBuffer);
            calculation.setKernelArg(kernel, 1, indexBuffer);
            calculation.setKernelArg(kernel, 2, resultBuffer);
            calculation.setKernelArg(kernel, 3, xQuery);
            calculation.setKernelArg(kernel, 4, zQuery);
            calculation.setKernelArg(kernel, 5, terrain.indices.length / 3);

            // Execute kernel with 1 work item (since we're finding a single height)
            long[] globalWorkSize = {1};
            boolean success = calculation.executeKernel(commandQueue, kernel, 1, globalWorkSize, null);

            if (!success) {
                return null;
            }

            // Wait for completion
            calculation.finish(commandQueue);

            // Read result
            FloatBuffer resultData = BufferUtils.createFloatBuffer(4);
            ByteBuffer byteBuffer = BufferUtils.createByteBuffer(4 * Float.BYTES);
            success = calculation.readBuffer(commandQueue, resultBuffer, byteBuffer);

            if (!success) {
                return null;
            }

            byteBuffer.rewind();
            resultData.put(byteBuffer.asFloatBuffer());
            resultData.rewind();

            float foundHeight = resultData.get(0);
            float found = resultData.get(1);

            if (found > 0.5f) {
                return foundHeight;
            }

            return 0f;

        } catch (Exception e) {
            Console.error("OpenCL height calculation failed:", e.getMessage());
            return null;
        }
    }

    public Vector3f height(Vector3f origin, Vector3f movement) {
        // Try OpenCL first
        if (openCLInitialized && vertexBuffer != 0 && indexBuffer != 0) {
            Vector3f result = heightMovementOpenCL(origin, movement);
            if (result != null) {
                return result;
            }
        }

        // Fallback to CPU implementation
        tempPos.set(origin).add(movement);
        for (int t = 0; t < terrain.indices.length; t += 3) {
            for (int i = 0; i < 3; i++) {
                idx[i] = terrain.indices[t + i] * 3;
                x[i] = terrain.vertices[idx[i]];
                y[i] = terrain.vertices[idx[i] + 1];
                z[i] = terrain.vertices[idx[i] + 2];
            }

            normal(x, y, z, faceNormal);
            if (faceNormal.y < Camera.SLOPE) continue;

            bary(x[0], z[0], x[1], z[1], x[2], z[2], tempPos.x, tempPos.z, bary);
            if (inside(bary)) {
                float yResult = bary[0] * y[0] + bary[1] * y[1] + bary[2] * y[2] + 1.7f;
                if (Math.abs(yResult - tempPos.y) < 0.5f) {
                    return tempPos.setComponent(1, yResult);
                }
            }
        }

        for (int t = 0; t < terrain.indices.length; t += 3) {
            for (int i = 0; i < 3; i++) {
                idx[i] = terrain.indices[t + i] * 3;
                x[i] = terrain.vertices[idx[i]];
                y[i] = terrain.vertices[idx[i] + 1];
                z[i] = terrain.vertices[idx[i] + 2];
            }

            normal(x, y, z, faceNormal);
            if (faceNormal.y < Camera.SLOPE) continue;

            bary(x[0], z[0], x[1], z[1], x[2], z[2], origin.x, origin.z, bary);
            if (inside(bary)) {
                float yResult = bary[0] * y[0] + bary[1] * y[1] + bary[2] * y[2] + 1.7f;
                if (Math.abs(yResult - origin.y) < 0.1f) {
                    movementDirection.set(movement).normalize();

                    if (bary[0] < 0.05f) {
                        edgeVector.set(x[1] - x[2], 0, z[1] - z[2]);
                    } else if (bary[1] < 0.05f) {
                        edgeVector.set(x[2] - x[0], 0, z[2] - z[0]);
                    } else if (bary[2] < 0.05f) {
                        edgeVector.set(x[1] - x[0], 0, z[1] - z[0]);
                    }
                    edgeVector.normalize();

                    float dot = movementDirection.dot(edgeVector);
                    projectedMovement.set(edgeVector).mul(dot * movement.length());
                    newPosition.set(origin).add(projectedMovement);
                    newPosition.setComponent(1, yResult);
                    return newPosition;
                }
            }
        }

        return origin;
    }

    private Vector3f heightMovementOpenCL(Vector3f origin, Vector3f movement) {
        try {
            updateBuffersIfNeeded();

            long kernel = calculation.getKernel(PROGRAM_NAME, HEIGHT_MOVEMENT_KERNEL);
            long commandQueue = calculation.getCommandQueue();

            if (kernel == 0 || commandQueue == 0) {
                return null;
            }

            // Set kernel arguments
            calculation.setKernelArg(kernel, 0, vertexBuffer);
            calculation.setKernelArg(kernel, 1, indexBuffer);
            calculation.setKernelArg(kernel, 2, movementResultBuffer);
            calculation.setKernelArg(kernel, 3, origin.x);
            calculation.setKernelArg(kernel, 4, origin.y);
            calculation.setKernelArg(kernel, 5, origin.z);
            calculation.setKernelArg(kernel, 6, movement.x);
            calculation.setKernelArg(kernel, 7, movement.y);
            calculation.setKernelArg(kernel, 8, movement.z);
            calculation.setKernelArg(kernel, 9, Camera.SLOPE);
            calculation.setKernelArg(kernel, 10, terrain.indices.length / 3);

            // Execute kernel with 1 work item
            long[] globalWorkSize = {1};
            boolean success = calculation.executeKernel(commandQueue, kernel, 1, globalWorkSize, null);

            if (!success) {
                return null;
            }

            // Wait for completion
            calculation.finish(commandQueue);

            // Read result
            ByteBuffer byteBuffer = BufferUtils.createByteBuffer(4 * Float.BYTES);
            success = calculation.readBuffer(commandQueue, movementResultBuffer, byteBuffer);

            if (!success) {
                return null;
            }

            byteBuffer.rewind();
            FloatBuffer resultData = byteBuffer.asFloatBuffer();

            float resultX = resultData.get(0);
            float resultY = resultData.get(1);
            float resultZ = resultData.get(2);
            float found = resultData.get(3);

            if (found > 0.5f) {
                return new Vector3f(resultX, resultY, resultZ);
            }

            return origin;

        } catch (Exception e) {
            Console.error("OpenCL height movement calculation failed:", e.getMessage());
            return null;
        }
    }

    private void normal(float[] x, float[] y, float[] z, Vector3f out) {
        v0.set(x[1] - x[0], y[1] - y[0], z[1] - z[0]);
        v1.set(x[2] - x[0], y[2] - y[0], z[2] - z[0]);
        v0.cross(v1, out).normalize();
    }

    private boolean inside(float[] bary) {
        return 0 <= bary[0] && bary[0] <= 1 &&
                0 <= bary[1] && bary[1] <= 1 &&
                0 <= bary[2] && bary[2] <= 1;
    }

    private void bary(float x1, float z1, float x2, float z2, float x3, float z3, float x, float z, float[] out) {
        float det = (z2 - z3) * (x1 - x3) + (x3 - x2) * (z1 - z3);
        float l1 = ((z2 - z3) * (x - x3) + (x3 - x2) * (z - z3)) / det;
        float l2 = ((z3 - z1) * (x - x3) + (x1 - x3) * (z - z3)) / det;
        float l3 = 1.0f - l1 - l2;
        out[0] = l1;
        out[1] = l2;
        out[2] = l3;
    }

    public void cleanup() {
        if (vertexBuffer != 0) {
            calculation.releaseBuffer(vertexBuffer);
            vertexBuffer = 0;
        }
        if (indexBuffer != 0) {
            calculation.releaseBuffer(indexBuffer);
            indexBuffer = 0;
        }
        if (resultBuffer != 0) {
            calculation.releaseBuffer(resultBuffer);
            resultBuffer = 0;
        }
        if (movementResultBuffer != 0) {
            calculation.releaseBuffer(movementResultBuffer);
            movementResultBuffer = 0;
        }
    }
}