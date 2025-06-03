package engine;

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

    private static final int CELL_WIDTH = 25;

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
        return "Unknown";
    }

    private static void printFormatted(String color, String... messages) {
        System.out.printf(color
                + String.join("", Collections.nCopies(messages.length, "%-" + CELL_WIDTH + "s"))
                + ANSI_RESET + "\n", (Object[]) messages);
    }

    private static void printWithCallerAndThread(String color, String... messages) {
        String caller = getCallerSimpleName();
        String threadId = "Thread #" + Thread.currentThread().getId();
        String[] args = new String[messages.length + 2];
        args[0] = threadId;
        args[1] = caller;
        System.arraycopy(messages, 0, args, 2, messages.length);
        printFormatted(color, args);
    }

    public static void debug(Object message) {
        printWithCallerAndThread(ANSI_GREEN, String.valueOf(message));
    }

    public static void debug(boolean message) {
        printWithCallerAndThread(ANSI_GREEN, Boolean.toString(message));
    }

    public static void debug(int message) {
        printWithCallerAndThread(ANSI_GREEN, Integer.toString(message));
    }

    public static void debug(double message) {
        printWithCallerAndThread(ANSI_GREEN, Double.toString(message));
    }

    public static void debug(float message) {
        printWithCallerAndThread(ANSI_GREEN, Float.toString(message));
    }

    public static void debug(String... messages) {
        printWithCallerAndThread(ANSI_GREEN, messages);
    }

    public static void debug(Object... objects) {
        String[] messages = new String[objects.length];
        for (int i = 0; i < objects.length; i++) {
            messages[i] = String.valueOf(objects[i]);
        }
        printWithCallerAndThread(ANSI_GREEN, messages);
    }

    public static void log(String... messages) {
        printWithCallerAndThread(ANSI_WHITE, messages);
    }

    public static void error(String... messages) {
        printWithCallerAndThread(ANSI_RED, messages);
        System.exit(1);
    }

    public static void warning(String... messages) {
        printWithCallerAndThread(ANSI_YELLOW, messages);
    }

    public static void ln() {
        System.out.println();
    }

}