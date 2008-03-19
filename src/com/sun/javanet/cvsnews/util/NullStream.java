package com.sun.javanet.cvsnews.util;

import java.io.OutputStream;
import java.io.IOException;

/**
 * {@link OutputStream} that discards data.
 *
 * @author Kohsuke Kawaguchi
 */
public class NullStream extends OutputStream {
    public void write(int b) throws IOException {
    }
    public void write(byte b[]) throws IOException {
    }
    public void write(byte b[], int off, int len) throws IOException {
    }
}
