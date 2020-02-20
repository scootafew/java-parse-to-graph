package com.york.sdp518.service.impl;

import com.york.sdp518.service.MetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.util.Optional;

public class MavenMetadataService implements MetadataService {

    private static final Logger logger = LoggerFactory.getLogger(MavenMetadataService.class);

    private static final String MAVEN_CENTRAL_URL = "https://repo.maven.apache.org/maven2";
    private static final String MAVEN_CENTRAL_URL_2 = "https://repo1.maven.org/maven2";

    private static final String MAVEN_METADATA_XML = "maven-metadata.xml";

    public MavenMetadataService() {

    }

    public Optional<String> getLatestVersion(String groupId, String artifactId) {
        String url = buildURL(groupId, artifactId);
        try {
            return Optional.of(readLatestVersion(url));
        } catch (Exception e) {
            // TODO Might want to just let exception propagate here as will want to exit
            logger.error("Could not get version from maven-metadata");
        }
        return Optional.empty();
    }

    private String buildURL(String groupId, String artifactId) {
        String groupIdFragment = groupId.replace('.', '/');
        return String.join("/", MAVEN_CENTRAL_URL, groupIdFragment, artifactId, MAVEN_METADATA_XML);
    }

    // Credit https://stackoverflow.com/questions/26654210/read-tag-value-of-remote-xml-file-using-java
    private String readLatestVersion(String url) throws Exception {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document document = builder.parse(url);

        XPath xPath = XPathFactory.newInstance().newXPath();
        String path = "/metadata/versioning/latest";
        String latestVersion = xPath.compile(path).evaluate(document);

        logger.info("Found latest version {}", latestVersion);

        return latestVersion;
    }
}
