package property;

public class State extends Number {

    final boolean[] bits;

    public State(int b) {
        this.bits = new boolean[b];
    }

    public State(State... bits) {
        int b = 0;
        for (State state : bits) {
            b += state.bits.length;
        }
        this.bits = new boolean[b];
        int index = 0;
        for (State s : bits) {
            for (int i = 0; i < s.bits.length; i++) {
                this.bits[index++] = s.bits[i];
            }
        }
    }

    public State(boolean... bits) {
        this.bits = bits;
    }

    public State(String state) {
        this.bits = new boolean[state.length()];
        for (int i = 0; i < state.length(); i++) {
            bits[i] = state.charAt(i) != '0';
        }
    }

    public static State of(int value) {
        int b = 0;
        for (int i = value; i > 0; i >>= 1) {
            b++;
        }
        return new State(b);
    }

    public State reset() {
        for (int i = 0; i < bits.length; i++) {
            bits[i] = false;
        }
        return this;
    }

    public int limit() {
        return 1 << bits.length;
    }

    public int get() {
        int state = 0;
        for (int i = 0; i < bits.length; i++) {
            state += bits[i] ? 1 << i : 0;
        }
        return state;
    }

    public State set(int i) {
        for (int j = 0; j < bits.length; j++) {
            bits[j] = (i & 1 << j) != 0;
        }
        return this;
    }

    public State next() {
        for (int i = 0; i < bits.length; i++) {
            if (!bits[i]) {
                bits[i] = true;
                break;
            } else {
                bits[i] = false;
            }
        }
        return this;
    }

    public State previous() {
        for (int i = 0; i < bits.length; i++) {
            if (bits[i]) {
                bits[i] = false;
                break;
            } else {
                bits[i] = true;
            }
        }
        return this;
    }

    public String encode() {
        StringBuilder state = new StringBuilder();
        for (boolean b : bits) {
            state.append(b ? '1' : '0');
        }
        return state.toString();
    }

    public State decode(String state) {
        for (int i = 0; i < state.length(); i++) {
            bits[i] = state.charAt(i) != '0';
        }
        return this;
    }

    @Override
    public String toString() {
        return encode();
    }

    @Override
    public int intValue() {
        return get();
    }

    @Override
    public long longValue() {
        return get();
    }

    @Override
    public float floatValue() {
        return get();
    }

    @Override
    public double doubleValue() {
        return get();
    }

}
