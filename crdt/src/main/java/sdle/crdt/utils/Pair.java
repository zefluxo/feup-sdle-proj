package main.java.sdle.crdt.utils;


import java.util.Objects;

public class Pair<F, S> {
    private final F first;
    private final S second;

    public Pair() {
        this.first = null;
        this.second = null;
    }

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public F getFirst() { return first; }
    public S getSecond() { return second; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Pair<?, ?> pair = (Pair<?, ?>) obj;

        if (!Objects.equals(first, pair.first)) return false;
        return Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        assert this.first != null;
        assert this.second != null;
        return "[Pair: K = " + this.first.toString() + " / V = " + this.second.toString() + "]";
    }

    public boolean isEmpty() {
        return (this.first == null && this.second == null);
    }

    // Factory method for convenience
    public static <A, B> Pair<A, B> of(A first, B second) {
        return new Pair<>(first, second);
    }
}
