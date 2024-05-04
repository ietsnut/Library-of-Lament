package property;

public class State extends Number {

    private final boolean[] bits;

    /**
     * @param limit possible states
     */
    public State(int limit) {
        int b = 0;
        for (int i = limit; i > 0; i >>= 1) {
            b++;
        }
        this.bits = new boolean[b];
    }

    /**
     * @param bits number of bits in the state
     */
    public static State of(int bits) {
        return new State(1 << bits - 1);
    }

    public State(State... states) {
        int b = 0;
        for (State state : states) {
            b += state.bits.length;
        }
        this.bits = new boolean[b];
        int index = 0;
        for (State s : states) {
            for (int i = 0; i < s.bits.length; i++) {
                this.bits[index++] = s.bits[i];
            }
        }
    }

    public State(String state) {
        this.bits = new boolean[state.length()];
        for (int i = 0; i < state.length(); i++) {
            bits[i] = state.charAt(i) != '0';
        }
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

    public boolean equals(int i) {
        return get() == i;
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
