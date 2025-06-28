package resource;

import engine.Console;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.lwjgl.opencl.CL10.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;

public class Calculation {

    private static Calculation instance;
    private long context;
    private long device;
    private boolean initialized = false;

    // Track programs and their kernels
    private final ConcurrentHashMap<String, Long> programs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> programKernels = new ConcurrentHashMap<>();

    // Thread-local resources
    private final ThreadLocal<ConcurrentHashMap<String, Long>> threadKernels = new ThreadLocal<>();
    private final ThreadLocal<Long> threadCommandQueues = new ThreadLocal<>();

    // Track all created resources for cleanup
    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, Long>> allThreadKernels = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Long> commandQueues = new ConcurrentHashMap<>();

    private Calculation() {
        initialize();
    }

    public static Calculation getInstance() {
        if (instance == null) {
            instance = new Calculation();
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

            initialized = true;
            Console.log("OpenCL started: ",
                    (deviceType == CL_DEVICE_TYPE_GPU ? "GPU" : "CPU"));

        } catch (Exception e) {
            Console.error("Failed to initialize OpenCL", e.getMessage());
            cleanup();
        }
    }

    /**
     * Load and compile a program from a file in the resources/kernels/ directory
     */
    public boolean loadProgram(String programName, String filename) {
        if (!initialized) {
            Console.error("OpenCL not initialized");
            return false;
        }

        try (MemoryStack stack = stackPush()) {
            // Load kernel source from file
            String source = loadKernelSource("/resources/calculation/" + filename);
            if (source == null) {
                return false;
            }

            IntBuffer pi = stack.mallocInt(1);

            // Create program
            PointerBuffer strings = stack.mallocPointer(1);
            PointerBuffer lengths = stack.mallocPointer(1);

            ByteBuffer sourceBuffer = stack.UTF8(source);
            strings.put(0, sourceBuffer);
            lengths.put(0, sourceBuffer.remaining());

            long program = clCreateProgramWithSource(context, strings, lengths, pi);
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
                Console.error("OpenCL build failed", programName, buildLog);
                clReleaseProgram(program);
                return false;
            }

            // Store program
            Long oldProgram = programs.put(programName, program);
            if (oldProgram != null) {
                // Clean up old program
                clReleaseProgram(oldProgram);
                programKernels.remove(programName);
            }

            // Initialize kernel map for this program
            programKernels.put(programName, new ConcurrentHashMap<>());

            Console.log("Loaded OpenCL:", programName);
            return true;

        } catch (Exception e) {
            Console.error("Failed to load program " + programName, e.getMessage());
            return false;
        }
    }

    /**
     * Get or create a kernel for the current thread
     */
    public long getKernel(String programName, String kernelName) {
        if (!initialized) {
            Console.error("OpenCL not initialized");
            return NULL;
        }

        Long program = programs.get(programName);
        if (program == null) {
            Console.error("Program not found:", programName);
            return NULL;
        }

        // Get thread-local kernel map
        ConcurrentHashMap<String, Long> threadKernelMap = threadKernels.get();
        if (threadKernelMap == null) {
            threadKernelMap = new ConcurrentHashMap<>();
            threadKernels.set(threadKernelMap);
            allThreadKernels.put(Thread.currentThread().getId(), threadKernelMap);
        }

        String kernelKey = programName + "::" + kernelName;
        Long kernel = threadKernelMap.get(kernelKey);

        if (kernel == null) {
            try (MemoryStack stack = stackPush()) {
                IntBuffer pi = stack.mallocInt(1);
                long newKernel = clCreateKernel(program, kernelName, pi);
                checkCLError(pi.get(0));

                threadKernelMap.put(kernelKey, newKernel);
                return newKernel;
            } catch (Exception e) {
                Console.error("Failed to create kernel " + kernelName + " from program " + programName, e.getMessage());
                return NULL;
            }
        }

        return kernel;
    }

    /**
     * Get or create command queue for the current thread
     */
    public long getCommandQueue() {
        if (!initialized) {
            Console.error("OpenCL not initialized");
            return NULL;
        }

        Long queue = threadCommandQueues.get();
        if (queue == null) {
            try (MemoryStack stack = stackPush()) {
                IntBuffer pi = stack.mallocInt(1);
                long commandQueue = clCreateCommandQueue(context, device, 0, pi);
                checkCLError(pi.get(0));

                threadCommandQueues.set(commandQueue);
                commandQueues.put(Thread.currentThread().getId(), commandQueue);
                return commandQueue;
            } catch (Exception e) {
                Console.error("Failed to create command queue", e.getMessage());
                return NULL;
            }
        }
        return queue;
    }

    /**
     * Create a buffer
     */
    public long createBuffer(long flags, long size) {
        if (!initialized) {
            Console.error("OpenCL not initialized");
            return NULL;
        }

        try (MemoryStack stack = stackPush()) {
            IntBuffer errcode_ret = stack.mallocInt(1);
            long buffer = clCreateBuffer(context, flags, size, errcode_ret);
            checkCLError(errcode_ret.get(0));
            return buffer;
        } catch (Exception e) {
            Console.error("Failed to create buffer", e.getMessage());
            return NULL;
        }
    }

    /**
     * Create a buffer with initial data
     */
    public long createBuffer(long flags, ByteBuffer data) {
        if (!initialized) {
            Console.error("OpenCL not initialized");
            return NULL;
        }

        try (MemoryStack stack = stackPush()) {
            IntBuffer errcode_ret = stack.mallocInt(1);
            long buffer = clCreateBuffer(context, flags, data, errcode_ret);
            checkCLError(errcode_ret.get(0));
            return buffer;
        } catch (Exception e) {
            Console.error("Failed to create buffer with data", e.getMessage());
            return NULL;
        }
    }

    /**
     * Create a buffer with initial int array data
     */
    public long createBuffer(long flags, IntBuffer data) {
        if (!initialized) {
            Console.error("OpenCL not initialized");
            return NULL;
        }

        try (MemoryStack stack = stackPush()) {
            IntBuffer errcode_ret = stack.mallocInt(1);
            long buffer = clCreateBuffer(context, flags, data, errcode_ret);
            checkCLError(errcode_ret.get(0));
            return buffer;
        } catch (Exception e) {
            Console.error("Failed to create int buffer with data", e.getMessage());
            return NULL;
        }
    }

    /**
     * Release a buffer
     */
    public void releaseBuffer(long buffer) {
        if (buffer != NULL) {
            clReleaseMemObject(buffer);
        }
    }

    /**
     * Set kernel arguments (convenience methods)
     */
    public void setKernelArg(long kernel, int index, long buffer) {
        clSetKernelArg1p(kernel, index, buffer);
    }

    public void setKernelArg(long kernel, int index, int value) {
        clSetKernelArg1i(kernel, index, value);
    }

    public void setKernelArg(long kernel, int index, float value) {
        clSetKernelArg1f(kernel, index, value);
    }

    /**
     * Execute kernel
     */
    public boolean executeKernel(long commandQueue, long kernel, int workDim, long[] globalWorkSize, long[] localWorkSize) {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer globalWS = null;
            PointerBuffer localWS = null;

            if (globalWorkSize != null) {
                globalWS = stack.mallocPointer(workDim);
                for (int i = 0; i < workDim; i++) {
                    globalWS.put(i, globalWorkSize[i]);
                }
            }

            if (localWorkSize != null) {
                localWS = stack.mallocPointer(workDim);
                for (int i = 0; i < workDim; i++) {
                    localWS.put(i, localWorkSize[i]);
                }
            }

            checkCLError(clEnqueueNDRangeKernel(commandQueue, kernel, workDim, null, globalWS, localWS, null, null));
            return true;
        } catch (Exception e) {
            Console.error("Failed to execute kernel", e.getMessage());
            return false;
        }
    }

    /**
     * Read buffer data
     */
    public boolean readBuffer(long commandQueue, long buffer, ByteBuffer data) {
        try {
            checkCLError(clEnqueueReadBuffer(commandQueue, buffer, true, 0L, data, null, null));
            return true;
        } catch (Exception e) {
            Console.error("Failed to read buffer", e.getMessage());
            return false;
        }
    }

    /**
     * Wait for all operations to complete
     */
    public void finish(long commandQueue) {
        clFinish(commandQueue);
    }

    public boolean isInitialized() {
        return initialized;
    }

    private String loadKernelSource(String filename) {
        try (InputStream in = Calculation.class.getResourceAsStream(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

            if (in == null) {
                Console.error("Failed to load kernel file:", filename);
                return null;
            }

            return reader.lines().collect(Collectors.joining("\n"));

        } catch (IOException e) {
            Console.error("Failed to read kernel file:", filename, e.getMessage());
            return null;
        }
    }

    private void checkCLError(int error) {
        if (error != CL_SUCCESS) {
            throw new RuntimeException("OpenCL error " + error);
        }
    }

    public void cleanup() {
        // Clean up all thread-specific kernels
        for (ConcurrentHashMap<String, Long> kernelMap : allThreadKernels.values()) {
            for (Long kernel : kernelMap.values()) {
                if (kernel != NULL) {
                    clReleaseKernel(kernel);
                }
            }
            kernelMap.clear();
        }
        allThreadKernels.clear();
        threadKernels.remove();

        // Clean up all thread-specific command queues
        for (Long queue : commandQueues.values()) {
            if (queue != NULL) {
                clReleaseCommandQueue(queue);
            }
        }
        commandQueues.clear();
        threadCommandQueues.remove();

        // Clean up programs
        for (Long program : programs.values()) {
            if (program != NULL) {
                clReleaseProgram(program);
            }
        }
        programs.clear();
        programKernels.clear();

        // Clean up context
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