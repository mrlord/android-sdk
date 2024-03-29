package com.adcash.mobileads.util;

import java.io.*;

public class Files {
    public static File createDirectory(String absolutePath) {
        if (absolutePath == null) {
            return null;
        }

        File directory = new File(absolutePath);

        if (directory.exists() && directory.isDirectory() ||
                directory.mkdirs() && directory.isDirectory()) {
            return directory;
        }

        return null;
    }

    public static int intLength(File file) {
        if (file == null) {
            return 0;
        }

        long length = file.length();

        if (length < Integer.MAX_VALUE) {
            return (int) length;
        } else {
            return Integer.MAX_VALUE;
        }
    }
}
