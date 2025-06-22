package resource;

import engine.Console;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opencl.CL10.*;

public class Mipmap {

    private static Mipmap instance;
    private final Calculation calculation;
    private boolean programLoaded = false;

    private static final String PROGRAM_NAME = "mipmap";
    private static final String KERNEL_NAME = "generate_mipmap";

    private Mipmap() {
        calculation = Calculation.getInstance();
        if (calculation.isInitialized()) {
            programLoaded = calculation.loadProgram(PROGRAM_NAME, "mipmap.cl");
        }
    }

    public static Mipmap getInstance() {
        if (instance == null) {
            instance = new Mipmap();
        }
        return instance;
    }

    public byte[] generateMipLevel(byte[] sourcePixels, int sourceWidth, int sourceHeight,
                                   int targetWidth, int targetHeight, int[] palette, int colorMapSize) {
        if (!calculation.isInitialized() || !programLoaded) {
            Console.warning("OpenCL not initialized or program not loaded, falling back to CPU");
            return null;
        }

        try {
            // Get thread-specific resources
            long kernel = calculation.getKernel(PROGRAM_NAME, KERNEL_NAME);
            long commandQueue = calculation.getCommandQueue();

            if (kernel == 0 || commandQueue == 0) {
                Console.warning("Failed to get OpenCL resources, falling back to CPU");
                return null;
            }

            // Create source buffer
            ByteBuffer sourceByteBuffer = BufferUtils.createByteBuffer(sourcePixels.length);
            sourceByteBuffer.put(sourcePixels).flip();
            long sourceBuffer = calculation.createBuffer(CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, sourceByteBuffer);

            if (sourceBuffer == 0) {
                Console.warning("Failed to create source buffer, falling back to CPU");
                return null;
            }

            // Create target buffer
            long targetBuffer = calculation.createBuffer(CL_MEM_WRITE_ONLY, (long)targetWidth * targetHeight);

            if (targetBuffer == 0) {
                calculation.releaseBuffer(sourceBuffer);
                Console.warning("Failed to create target buffer, falling back to CPU");
                return null;
            }

            // Create palette buffer
            IntBuffer paletteBuffer = BufferUtils.createIntBuffer(palette.length);
            paletteBuffer.put(palette).flip();
            long paletteBufferCL = calculation.createBuffer(CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, paletteBuffer);

            if (paletteBufferCL == 0) {
                calculation.releaseBuffer(sourceBuffer);
                calculation.releaseBuffer(targetBuffer);
                Console.warning("Failed to create palette buffer, falling back to CPU");
                return null;
            }

            // Set kernel arguments
            calculation.setKernelArg(kernel, 0, sourceBuffer);
            calculation.setKernelArg(kernel, 1, targetBuffer);
            calculation.setKernelArg(kernel, 2, paletteBufferCL);
            calculation.setKernelArg(kernel, 3, sourceWidth);
            calculation.setKernelArg(kernel, 4, sourceHeight);
            calculation.setKernelArg(kernel, 5, targetWidth);
            calculation.setKernelArg(kernel, 6, targetHeight);
            calculation.setKernelArg(kernel, 7, colorMapSize);

            // Execute kernel
            long[] globalWorkSize = {targetWidth, targetHeight};
            boolean success = calculation.executeKernel(commandQueue, kernel, 2, globalWorkSize, null);

            if (!success) {
                calculation.releaseBuffer(sourceBuffer);
                calculation.releaseBuffer(targetBuffer);
                calculation.releaseBuffer(paletteBufferCL);
                Console.warning("Failed to execute kernel, falling back to CPU");
                return null;
            }

            // Wait for completion
            calculation.finish(commandQueue);

            // Read result
            ByteBuffer resultBuffer = BufferUtils.createByteBuffer(targetWidth * targetHeight);
            success = calculation.readBuffer(commandQueue, targetBuffer, resultBuffer);

            if (!success) {
                calculation.releaseBuffer(sourceBuffer);
                calculation.releaseBuffer(targetBuffer);
                calculation.releaseBuffer(paletteBufferCL);
                Console.warning("Failed to read result buffer, falling back to CPU");
                return null;
            }

            // Convert ByteBuffer to byte array
            byte[] result = new byte[targetWidth * targetHeight];
            resultBuffer.get(result);

            // Cleanup buffers
            calculation.releaseBuffer(sourceBuffer);
            calculation.releaseBuffer(targetBuffer);
            calculation.releaseBuffer(paletteBufferCL);

            return result;

        } catch (Exception e) {
            Console.error("OpenCL mipmap generation failed:", e.getMessage());
            return null;
        }
    }

    public boolean isInitialized() {
        return calculation.isInitialized() && programLoaded;
    }

    // Call this when shutting down the application
    public static void shutdown() {
        if (instance != null) {
            instance = null;
        }
    }
}