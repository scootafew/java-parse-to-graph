package com.york.sdp518.util;


/**
 * Source: https://maven.apache.org/pom.html
 */
public enum Packaging {
    POM("pom"),
    JAR("jar"),
    MAVEN_PLUGIN("maven_plugin"),
    EJB("ejb"),
    WAR("war"),
    EAR("ear"),
    RAR("rar"),
    BUNDLE("bundle"),
    UNKNOWN("unknown");

    private final String packaging;

    Packaging(String packaging){
        this.packaging = packaging;
    }

    public String getPackaging() {
        return packaging;
    }

    /**
     * converts the string representation of the enum to the enum value
     * @param text string representation of the enum
     * @return the enum value of the provided text
     */
    public static Packaging fromString(String text) {
        for (Packaging packaging : Packaging.values()) {
            if (packaging.getPackaging().equals(text)) return packaging;
        }
        return Packaging.UNKNOWN;
    }

    @Override
    public String toString() {
        return packaging;
    }


}
