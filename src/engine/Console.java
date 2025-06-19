package engine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Console {

    public static final String ANSI_RESET  = "\u001B[0m";
    public static final String ANSI_BLACK  = "\u001B[30m";
    public static final String ANSI_RED    = "\u001B[31m";
    public static final String ANSI_GREEN  = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE   = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN   = "\u001B[36m";
    public static final String ANSI_WHITE  = "\u001B[37m";

    private static final int CELL_WIDTH = 24;
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private static ArrayList<String> messages = new ArrayList<>();

    private static String getCurrentTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMAT);
    }

    private static String getCallerSimpleName() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stack) {
            String className = element.getClassName();
            if (className.equals(Thread.class.getName()) || className.equals(Console.class.getName())) {
                continue;
            }
            int lastDot = className.lastIndexOf('.');
            return lastDot != -1 ? className.substring(lastDot + 1) : className;
        }
        return "?";
    }

    private static void printFormatted(String color, String... messages) {
        StringBuilder format = new StringBuilder(color);
        Object[] args = new Object[messages.length];

        for (int i = 0; i < messages.length; i++) {
            String message = messages[i];
            int messageLength = message.length();

            // Calculate how many cells this message spans
            int cellsNeeded = (messageLength + CELL_WIDTH - 1) / CELL_WIDTH; // Ceiling division
            if (cellsNeeded == 0) cellsNeeded = 1; // At least one cell

            int totalWidth = cellsNeeded * CELL_WIDTH;
            format.append("%-").append(totalWidth).append("s");
            args[i] = message;
        }

        format.append(ANSI_RESET).append("\n");
        System.out.printf(format.toString(), args);
    }

    private static void printWithTimestampCallerAndThread(String color, String... messages) {
        String timestamp = getCurrentTimestamp();
        String caller = getCallerSimpleName();
        String threadId = "Thread #" + Thread.currentThread().getId();
        String[] args = new String[messages.length + 3];
        args[0] = timestamp;
        args[1] = threadId;
        args[2] = caller;
        System.arraycopy(messages, 0, args, 3, messages.length);
        printFormatted(color, args);
    }

    public static void debug(Object... messages) {
        printWithTimestampCallerAndThread(ANSI_GREEN, Arrays.stream(messages).map(String::valueOf).toArray(String[]::new));
    }

    public static void notify(Object... messages) {
        printWithTimestampCallerAndThread(ANSI_PURPLE, Arrays.stream(messages).map(String::valueOf).toArray(String[]::new));
    }

    public static void log(Object... messages) {
        printWithTimestampCallerAndThread(ANSI_WHITE, Arrays.stream(messages).map(String::valueOf).toArray(String[]::new));
    }

    public static void warning(Object... messages) {
        printWithTimestampCallerAndThread(ANSI_YELLOW, Arrays.stream(messages).map(String::valueOf).toArray(String[]::new));
    }

    public static void error(Object... messages) {
        printWithTimestampCallerAndThread(ANSI_RED, Arrays.stream(messages).map(String::valueOf).toArray(String[]::new));
    }

    public static void error(Throwable t, Object... messages) {
        String[] logMessages = new String[messages.length + 1];
        logMessages[0] = t.toString().replace("java.lang.", "");
        for (int i = 0; i < messages.length; i++) {
            logMessages[i + 1] = String.valueOf(messages[i]);
        }
        error((Object[]) logMessages);

        for (StackTraceElement element : t.getStackTrace()) {
            System.out.println(ANSI_RED + element + ANSI_RESET);
        }

        Throwable cause = t.getCause();
        int indentLevel = 0;
        while (cause != null) {
            // Use 8 spaces per level for clear visual separation
            String indent = String.join("", Collections.nCopies(4 + (indentLevel * 8), " "));
            System.out.println(ANSI_RED + indent + "└─▶ " + cause + ANSI_RESET);

            StackTraceElement[] causeStack = cause.getStackTrace();
            int maxElements = Math.min(causeStack.length, 4);
            for (int i = 0; i < maxElements; i++) {
                // Stack traces get same base indent + 4 more spaces for alignment
                System.out.println(ANSI_RED + indent + "    " + causeStack[i] + ANSI_RESET);
            }

            cause = cause.getCause();
            indentLevel++;
        }
    }

    public static void ln() {
        System.out.println();
    }
}