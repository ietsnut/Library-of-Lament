package resource;

import engine.Console;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.opencl.CL10.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;

public class Mipmap {

    private static Mipmap instance;
    private long context;
    private long program;
    private long device;
    private boolean initialized = false;

    // Thread-local resources
    private final ThreadLocal<Long> threadKernels = new ThreadLocal<>();
    private final ThreadLocal<Long> threadCommandQueues = new ThreadLocal<>();

    // Track all created resources for cleanup
    private final ConcurrentHashMap<Long, Long> kernels = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Long> commandQueues = new ConcurrentHashMap<>();

    // OpenCL kernel source code
    private static final String KERNEL_SOURCE = """
        __kernel void generate_mipmap(
            __global const uchar* source_pixels,
            __global uchar* target_pixels,
            __global const int* palette,
            const int source_width,
            const int source_height,
            const int target_width,
            const int target_height,
            const int color_map_size
        ) {
            int x = get_global_id(0);
            int y = get_global_id(1);
            
            if (x >= target_width || y >= target_height) {
                return;
            }
            
            float x_ratio = (float)source_width / target_width;
            float y_ratio = (float)source_height / target_height;
            
            float src_x1 = x * x_ratio;
            float src_y1 = y * y_ratio;
            float src_x2 = (x + 1) * x_ratio;
            float src_y2 = (y + 1) * y_ratio;
            
            int start_x = (int)floor(src_x1);
            int start_y = (int)floor(src_y1);
            int end_x = min((int)ceil(src_x2), source_width);
            int end_y = min((int)ceil(src_y2), source_height);
            
            int total_r = 0, total_g = 0, total_b = 0, count = 0;
            
            for (int sy = start_y; sy < end_y; sy++) {
                for (int sx = start_x; sx < end_x; sx++) {
                    int index = source_pixels[sy * source_width + sx];
                    if (index < color_map_size) {
                        int rgb = palette[index];
                        total_r += (rgb >> 16) & 0xFF;
                        total_g += (rgb >> 8) & 0xFF;
                        total_b += rgb & 0xFF;
                        count++;
                    }
                }
            }
            
            uchar best_index = 0;
            
            if (count == 0) {
                int fallback_y = min(start_y, source_height - 1);
                int fallback_x = min(start_x, source_width - 1);
                best_index = source_pixels[fallback_y * source_width + fallback_x];
            } else {
                int avg_r = total_r / count;
                int avg_g = total_g / count;
                int avg_b = total_b / count;
                
                int best_dist = INT_MAX;
                for (int i = 0; i < color_map_size; i++) {
                    int rgb = palette[i];
                    int dr = ((rgb >> 16) & 0xFF) - avg_r;
                    int dg = ((rgb >> 8) & 0xFF) - avg_g;
                    int db = (rgb & 0xFF) - avg_b;
                    int dist = dr * dr + dg * dg + db * db;
                    if (dist < best_dist) {
                        best_dist = dist;
                        best_index = (uchar)i;
                    }
                }
            }
            
            target_pixels[y * target_width + x] = best_index;
        }
        """;

    private Mipmap() {
        initialize();
    }

    public static Mipmap getInstance() {
        if (instance == null) {
            instance = new Mipmap();
        }
        return instance;
    }

    private void initialize() {
        try (MemoryStack stack = stackPush()) {
            // Get platform
            IntBuffer pi = stack.mallocInt(1);
            checkCLError(clGetPlatformIDs(null, pi));
            if (pi.get(0) == 0) {
                Console.error("No OpenCL platforms found");
                return;
            }

            PointerBuffer platforms = stack.mallocPointer(pi.get(0));
            checkCLError(clGetPlatformIDs(platforms, (IntBuffer) null));
            long platform = platforms.get(0);

            // Get device - try GPU first, then CPU
            checkCLError(clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, null, pi));
            long deviceType = CL_DEVICE_TYPE_GPU;
            if (pi.get(0) == 0) {
                Console.warning("No GPU devices found, trying CPU");
                checkCLError(clGetDeviceIDs(platform, CL_DEVICE_TYPE_CPU, null, pi));
                deviceType = CL_DEVICE_TYPE_CPU;
                if (pi.get(0) == 0) {
                    Console.error("No OpenCL devices found");
                    return;
                }
            }

            PointerBuffer devices = stack.mallocPointer(pi.get(0));
            checkCLError(clGetDeviceIDs(platform, deviceType, devices, (IntBuffer) null));
            device = devices.get(0);

            // Create context
            PointerBuffer contextProps = stack.mallocPointer(3);
            contextProps.put(CL_CONTEXT_PLATFORM).put(platform).put(0);
            contextProps.flip();

            context = clCreateContext(contextProps, device, null, NULL, pi);
            checkCLError(pi.get(0));

            // Create program
            PointerBuffer strings = stack.mallocPointer(1);
            PointerBuffer lengths = stack.mallocPointer(1);

            ByteBuffer source = stack.UTF8(KERNEL_SOURCE);
            strings.put(0, source);
            lengths.put(0, source.remaining());

            program = clCreateProgramWithSource(context, strings, lengths, pi);
            checkCLError(pi.get(0));

            // Build program
            int ret = clBuildProgram(program, device, "", null, NULL);
            if (ret != CL_SUCCESS) {
                // Get build log
                PointerBuffer pp = stack.mallocPointer(1);
                clGetProgramBuildInfo(program, device, CL_PROGRAM_BUILD_LOG, (ByteBuffer) null, pp);

                int logSize = (int) pp.get(0);
                ByteBuffer buffer = stack.malloc(logSize);
                clGetProgramBuildInfo(program, device, CL_PROGRAM_BUILD_LOG, buffer, null);

                String buildLog = memUTF8(buffer);
                Console.error("OpenCL build failed:", buildLog);
                return;
            }

            initialized = true;
        } catch (Exception e) {
            Console.error("Failed to initialize OpenCL", e.getMessage());
            cleanup();
        }
    }

    private long getOrCreateKernel() {
        Long threadKernel = threadKernels.get();
        if (threadKernel == null) {
            try (MemoryStack stack = stackPush()) {
                IntBuffer pi = stack.mallocInt(1);
                long newKernel = clCreateKernel(program, "generate_mipmap", pi);
                checkCLError(pi.get(0));

                threadKernels.set(newKernel);
                kernels.put(Thread.currentThread().getId(), newKernel);
                return newKernel;
            }
        }
        return threadKernel;
    }

    private long getOrCreateCommandQueue() {
        Long queue = threadCommandQueues.get();
        if (queue == null) {
            try (MemoryStack stack = stackPush()) {
                IntBuffer pi = stack.mallocInt(1);
                long commandQueue = clCreateCommandQueue(context, device, 0, pi);
                checkCLError(pi.get(0));

                threadCommandQueues.set(commandQueue);
                commandQueues.put(Thread.currentThread().getId(), commandQueue);
                return commandQueue;
            }
        }
        return queue;
    }

    public byte[] generateMipLevel(byte[] sourcePixels, int sourceWidth, int sourceHeight,
                                   int targetWidth, int targetHeight, int[] palette, int colorMapSize) {
        if (!initialized) {
            Console.warning("OpenCL not initialized, falling back to CPU");
            return null;
        }

        try (MemoryStack stack = stackPush()) {
            IntBuffer errcode_ret = stack.mallocInt(1);

            // Get thread-specific resources
            long kernel = getOrCreateKernel();
            long commandQueue = getOrCreateCommandQueue();

            // Create source buffer
            ByteBuffer sourceByteBuffer = BufferUtils.createByteBuffer(sourcePixels.length);
            sourceByteBuffer.put(sourcePixels).flip();
            long sourceBuffer = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                    sourceByteBuffer, errcode_ret);
            checkCLError(errcode_ret.get(0));

            // Create target buffer
            long targetBuffer = clCreateBuffer(context, CL_MEM_WRITE_ONLY,
                    (long)targetWidth * targetHeight, errcode_ret);
            checkCLError(errcode_ret.get(0));

            // Create palette buffer
            IntBuffer paletteBuffer = BufferUtils.createIntBuffer(palette.length);
            paletteBuffer.put(palette).flip();
            long paletteBufferCL = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                    paletteBuffer, errcode_ret);
            checkCLError(errcode_ret.get(0));

            // Set kernel arguments
            clSetKernelArg1p(kernel, 0, sourceBuffer);
            clSetKernelArg1p(kernel, 1, targetBuffer);
            clSetKernelArg1p(kernel, 2, paletteBufferCL);
            clSetKernelArg1i(kernel, 3, sourceWidth);
            clSetKernelArg1i(kernel, 4, sourceHeight);
            clSetKernelArg1i(kernel, 5, targetWidth);
            clSetKernelArg1i(kernel, 6, targetHeight);
            clSetKernelArg1i(kernel, 7, colorMapSize);

            // Execute kernel
            PointerBuffer globalWorkSize = stack.mallocPointer(2);
            globalWorkSize.put(0, targetWidth);
            globalWorkSize.put(1, targetHeight);

            checkCLError(clEnqueueNDRangeKernel(commandQueue, kernel, 2, null,
                    globalWorkSize, null, null, null));

            // Wait for completion
            clFinish(commandQueue);

            // Read result using ByteBuffer
            ByteBuffer resultBuffer = BufferUtils.createByteBuffer(targetWidth * targetHeight);
            checkCLError(clEnqueueReadBuffer(commandQueue, targetBuffer, true, 0L,
                    resultBuffer, null, null));

            // Convert ByteBuffer to byte array
            byte[] result = new byte[targetWidth * targetHeight];
            resultBuffer.get(result);

            // Cleanup buffers
            clReleaseMemObject(sourceBuffer);
            clReleaseMemObject(targetBuffer);
            clReleaseMemObject(paletteBufferCL);

            return result;

        } catch (Exception e) {
            Console.error("OpenCL failed:", e.getMessage());
            return null;
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    private void checkCLError(int error) {
        if (error != CL_SUCCESS) {
            throw new RuntimeException("OpenCL error " + error);
        }
    }

    public void cleanup() {
        // Clean up all thread-specific kernels
        for (Long k : kernels.values()) {
            if (k != NULL) {
                clReleaseKernel(k);
            }
        }
        kernels.clear();
        threadKernels.remove();

        // Clean up all thread-specific command queues
        for (Long queue : commandQueues.values()) {
            if (queue != NULL) {
                clReleaseCommandQueue(queue);
            }
        }
        commandQueues.clear();
        threadCommandQueues.remove();

        // Clean up shared resources
        if (program != NULL) {
            clReleaseProgram(program);
            program = NULL;
        }
        if (context != NULL) {
            clReleaseContext(context);
            context = NULL;
        }
        initialized = false;
    }

    // Call this when shutting down the application
    public static void shutdown() {
        if (instance != null) {
            instance.cleanup();
            instance = null;
        }
    }
}