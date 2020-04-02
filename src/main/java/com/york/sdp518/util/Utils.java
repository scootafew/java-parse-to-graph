package com.york.sdp518.util;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Utils {

    private Utils() {

    }

    public static String repoNameFromURI(String uri) {
        return uri.substring(uri.lastIndexOf('/') + 1, uri.lastIndexOf(".git"));
    }

    public static String repoFullNameFromURI(String uri) {
        return StringUtils.substringBetween(uri, "github.com/", ".git");
    }

    public static Collection<Path> getDirectoriesWithPom(Path projectDirectory) {
        return getPoms(Collections.singletonList(projectDirectory.toFile())).stream()
                .map(file -> file.toPath().getParent())
                .collect(Collectors.toSet());
    }

    /**
     * Breadth first search for POM files, returns list of files at equivalent directory depths when found
     * @param files
     * @return
     */
    private static Collection<File> getPoms(List<File> files) {
        if (files.isEmpty()) {
            return Collections.emptySet();
        }
        Collection<File> poms = files.stream()
                .filter(file -> file.getName().equals("pom.xml"))
                .collect(Collectors.toSet());
        if (!poms.isEmpty()) {
            return poms;
        } else {
            List<File> nextDepth = files.stream()
                    .map(File::listFiles)
                    .filter(Objects::nonNull)
                    .flatMap(Arrays::stream)
                    .filter(f -> !f.isHidden())
                    .collect(Collectors.toList());
            return getPoms(nextDepth);
        }
    }

    public static boolean isNonEmptyDirectory(File dir) {
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            return files != null && files.length != 0;
        } else {
            return false;
        }
    }

    public static String getPropertyOrEnv(String key, boolean required) {
        String value;
        value = System.getProperty(key);
        if (value == null) {
            value = System.getenv(key);
        }
        if (required && (value == null)) {
            throw new RuntimeException("Required parameter not found");
        }
        return (value == null) ? "" : value;
    }

    public static List<String> readFileSplitNewLine(File file) throws IOException {
        try (Stream<String> stream = Files.lines( file.toPath(), StandardCharsets.UTF_8)) {
            return stream.collect(Collectors.toList());
        }
    }
}
