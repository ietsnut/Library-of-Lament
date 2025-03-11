package game;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import static resource.Resource.THREADS;

public class Serial implements SerialPortDataListener, Runnable {

    public static final byte[] IN  = new byte[] {0b0000, 0b0000, 0b0000, 0b0000};
    public static final byte[] OUT = new byte[] {0b0001, 0b0001, 0b0000, 0b0000};
    public static final int LENGTH = 4; // number of nibbles

    private static SerialPort PORT;

    private static int expectedBytes;
    private static byte[] receivedBuffer;
    private static int receivedCount;

    public static void start(String portDescriptor) {
        Serial serial = new Serial();
        PORT = SerialPort.getCommPort(portDescriptor);
        PORT.setBaudRate(9600);
        PORT.setNumDataBits(8);
        PORT.setParity(SerialPort.NO_PARITY);
        PORT.setNumStopBits(SerialPort.ONE_STOP_BIT);
        PORT.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
        if (!PORT.openPort()) {
            Console.error("Failed to open port", portDescriptor);
            return;
        }
        PORT.addDataListener(serial);
        THREADS.submit(serial);
    }

    private static volatile boolean shiftingIn = false;

    private static void read() {
        if (PORT == null) {
            Console.warning("Port not initialized");
            return;
        }
        if (!PORT.isOpen()) {
            Console.warning("Port not open");
            return;
        }
        PORT.flushIOBuffers();
        int numBytes = (LENGTH + 1) / 2;
        expectedBytes = numBytes;
        receivedBuffer = new byte[numBytes];
        receivedCount = 0;
        byte[] command = new byte[] { 1, (byte) numBytes };
        int bytesWritten = PORT.writeBytes(command, command.length);
        if (bytesWritten != command.length) {
            Console.error("Could not write full syncIn command.");
        }
    }

    private static boolean shiftingOut = false;

    public static void write() {
        if (PORT == null) {
            Console.warning("Port not initialized");
            return;
        }
        if (!PORT.isOpen()) {
            Console.warning("Port not open");
            return;
        }
        if (shiftingOut) {
            return;
        }
        shiftingOut = true;
        PORT.flushIOBuffers();
        int numBytes = (LENGTH + 1) / 2;
        byte[] packedData = new byte[numBytes];
        for (int i = 0; i < numBytes; i++) {
            int indexHigh = 2 * i;
            int highNibble = OUT[indexHigh] & 0x0F;
            int lowNibble = 0;
            if (indexHigh + 1 < LENGTH) {
                lowNibble = OUT[indexHigh + 1] & 0x0F;
            }
            packedData[i] = (byte) ((highNibble << 4) | lowNibble);
        }
        byte[] command = new byte[2 + numBytes];
        command[0] = 2;
        command[1] = (byte) numBytes;
        System.arraycopy(packedData, 0, command, 2, numBytes);
        int bytesWritten = PORT.writeBytes(command, command.length);
        if (bytesWritten != command.length) {
            Console.error("Could not write full syncOut command.");
        }
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
            return;
        }
        int available = PORT.bytesAvailable();
        if (available <= 0) return;

        byte[] newData = new byte[available];
        int numRead = PORT.readBytes(newData, newData.length);
        if (numRead > 0 && receivedBuffer != null) {
            int bytesToCopy = Math.min(numRead, expectedBytes - receivedCount);
            System.arraycopy(newData, 0, receivedBuffer, receivedCount, bytesToCopy);
            receivedCount += bytesToCopy;
            if (receivedCount >= expectedBytes) {
                for (int i = 0; i < expectedBytes; i++) {
                    int highNibble = (receivedBuffer[i] >> 4) & 0x0F;
                    IN[2 * i] = (byte) highNibble;
                    if (2 * i + 1 < LENGTH) {
                        int lowNibble = receivedBuffer[i] & 0x0F;
                        IN[2 * i + 1] = (byte) lowNibble;
                    }
                }
                StringBuilder data = new StringBuilder("IN: ");
                for (byte b : IN) {
                    data.append((b & 0x0F)).append(" ");
                }
                Console.log("Input", data.toString());
                shiftingIn = false;
                shiftingOut = false;
            }
        }
    }

    public static boolean get(int nibbleIndex, int position) {
        return ((IN[nibbleIndex] >> position) & 1) == 1;
    }

    public static void set(int nibbleIndex, int position, int bitValue) {
        OUT[nibbleIndex] = bitValue == 1 ? (byte)(OUT[nibbleIndex] | (1 << position)) : (byte)(OUT[nibbleIndex] & ~(1 << position));
    }

    public static void close() {
        if (PORT != null && PORT.isOpen()) {
            PORT.closePort();
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted() && PORT.isOpen()) {
            if (!shiftingIn) {
                shiftingIn = true;
                read();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
