package com.vspiewak.jdk_features.jdk7;

public class MyResource implements AutoCloseable {

    public boolean closed = false;

    @Override
    public void close() throws Exception {
        closed = true;
    }

}
