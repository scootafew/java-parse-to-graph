package com.york.sdp518;

import java.io.File;

public class Utils {

    public static String repoNameFromURI(String uri) {
        return uri.substring(uri.lastIndexOf("/"), uri.lastIndexOf(".git"));
    }

    public static boolean isNonEmptyDirectory(File dir) {
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            return files != null && files.length != 0;
        } else {
            return false;
        }
    }
}
