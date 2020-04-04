package com.york.sdp518.service.impl;

import com.york.sdp518.exception.MavenMetadataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class MavenMetadataService {

    private static final Logger logger = LoggerFactory.getLogger(MavenMetadataService.class);

    private static final String MAVEN_CENTRAL_URL = "https://repo.maven.apache.org/maven2";
    private static final String MAVEN_CENTRAL_URL_2 = "https://repo1.maven.org/maven2";

    private static final String MAVEN_METADATA_XML = "maven-metadata.xml";
    private static final String PATH_TO_LATEST_VERSION = "/metadata/versioning/latest"; //NOSONAR Hardcoded path

    public MavenMetadataService() {
        // NOSONAR
    }

    public String getLatestVersion(String groupId, String artifactId) throws MavenMetadataException {
        String url = buildURL(groupId, artifactId);
        try {
            Document xmlDocument = getXmlFromUrl(url);
            return readLatestVersionFromXml(xmlDocument);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            String errorMessage = String.format("Could not retrieve %s from %s", MAVEN_METADATA_XML, MAVEN_CENTRAL_URL);
            throw new MavenMetadataException(errorMessage, e);
        } catch (XPathExpressionException e) {
            throw new MavenMetadataException("Could not read latest version from " + MAVEN_METADATA_XML, e);
        }
    }

    // https://stackoverflow.com/questions/2461864/can-i-check-if-a-file-exists-at-a-url
    // TODO Should retry before failing
    public boolean isPublishedArtifact(String groupId, String artifactId) throws MavenMetadataException {
        try {
            final URL url = new URL(buildURL(groupId, artifactId));
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();

            switch (huc.getResponseCode()) {
                case HttpURLConnection.HTTP_OK:
                    return true;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    return false;
                default:
                    throw new MavenMetadataException("Unexpected status code from Maven Central");
            }
        } catch (IOException e) {
            logger.warn("Error contacting Maven Central");
            throw new MavenMetadataException("Error contacting Maven Central", e);
        }
    }

    private String buildURL(String groupId, String artifactId) {
        String groupIdFragment = groupId.replace('.', '/');
        return String.join("/", MAVEN_CENTRAL_URL, groupIdFragment, artifactId, MAVEN_METADATA_XML);
    }

    // Credit https://stackoverflow.com/questions/26654210/read-tag-value-of-remote-xml-file-using-java
    private Document getXmlFromUrl(String url) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

        // Disable XML External Entity (XXE) processing
        builderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        return builder.parse(url);
    }

    private String readLatestVersionFromXml(Document document) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        return xPath.compile(PATH_TO_LATEST_VERSION).evaluate(document);
    }
}
