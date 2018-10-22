package com.alexsxode.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Reimplementation of useful methods from next versions of java
 */
public final class Future {
    public static int transferTo(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int read = 0, count = 0;
        while ((read = in.read(buffer, 0, 4096)) >= 0) {
            out.write(buffer, 0, read);
            count += read;
        }
        return count;
    }
}
