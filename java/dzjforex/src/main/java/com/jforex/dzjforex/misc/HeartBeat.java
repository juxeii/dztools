package com.jforex.dzjforex.misc;

public class HeartBeat<T> {

    private final int index;
    private final T data;

    public HeartBeat(final int index,
                     final T data) {
        this.index = index;
        this.data = data;
    }

    public int index() {
        return index;
    }

    public T data() {
        return data;
    }
}
