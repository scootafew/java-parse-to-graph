package com.york.sdp518;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.domain.ProcessingState;
import com.york.sdp518.exception.AlreadyProcessedException;
import com.york.sdp518.exception.JavaParseToGraphException;
import com.york.sdp518.exception.MavenPluginInvocationException;
import com.york.sdp518.processor.SpoonProcessor;
import com.york.sdp518.service.impl.MavenDependencyManagementService;
import com.york.sdp518.service.impl.Neo4jServiceFactory;
import com.york.sdp518.service.impl.ProcessableNeo4jService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(SpringExtension.class)
class ArtifactAnalyserTest {

    private static final String ARTIFACT_FQN = "org.test.mock:mock-core:3.0.0";
    private static final Path MOCK_ARTIFACT_PATH = Paths.get("./src/test/resources/artifacts/maven/mock-core");

    private static final String SPOON_TMP_FILE = "spoon.classpath-app.tmp";

    @MockBean
    MavenDependencyManagementService mavenService;
    @MockBean
    SpoonProcessor spoonProcessor;
    @MockBean
    Neo4jServiceFactory neo4jServiceFactory;

    @Mock
    ProcessableNeo4jService<Artifact> neo4jServiceMock;

    ArtifactAnalyser artifactAnalyser;

    @BeforeEach
    void setUp() throws Exception {
        given(neo4jServiceFactory.getServiceForProcessableEntity(Artifact.class))
                .willReturn(neo4jServiceMock);
        artifactAnalyser = new ArtifactAnalyser(mavenService, spoonProcessor, neo4jServiceFactory);

        // create tmp file to mock successful classpath build
        MOCK_ARTIFACT_PATH.resolve(SPOON_TMP_FILE).toFile().createNewFile();
    }

    @AfterEach
    void tearDown() {
        // cleanup tmp file representing successful classpath build
        MOCK_ARTIFACT_PATH.resolve(SPOON_TMP_FILE).toFile().delete();
    }

    @Test
    void analyseArtifactSuccessfully() throws Exception {
        Artifact artifact = new Artifact(ARTIFACT_FQN);

        // given
        given(mavenService.getSources(artifact)).willReturn(MOCK_ARTIFACT_PATH);
        willDoNothing().given(spoonProcessor).process(any());

        // when
        artifactAnalyser.analyseArtifact(artifact);

        // then
        then(mavenService).should(times(1)).getSources(artifact);

        ArgumentCaptor<Artifact> artifactCaptor = ArgumentCaptor.forClass(Artifact.class);
        then(neo4jServiceMock).should(times(1)).createOrUpdate(artifactCaptor.capture());

        Artifact postProcessingArtifact = artifactCaptor.getValue();
        assertThat(postProcessingArtifact.getProcessingState()).isEqualTo(ProcessingState.COMPLETED);
        assertThat(postProcessingArtifact.getGroupId()).isEqualTo("org.test.mock");
        assertThat(postProcessingArtifact.getArtifactId()).isEqualTo("mock-core");
        assertThat(postProcessingArtifact.getVersion()).isEqualTo("3.0.0");
    }

    @Test
    void analyseArtifactFail() throws Exception {
        Artifact artifact = new Artifact(ARTIFACT_FQN);

        // given
        given(mavenService.getSources(artifact)).willThrow(new MavenPluginInvocationException("Error retrieving sources"));

        // when
        Assertions.assertThrows(JavaParseToGraphException.class, () -> artifactAnalyser.analyseArtifact(artifact));

        // then
        then(spoonProcessor).shouldHaveNoInteractions();
    }

    @Test
    public void alreadyProcessedArtifact() throws Exception {
        // given
        given(neo4jServiceMock.tryToBeginProcessing(any(Artifact.class))).willThrow(new AlreadyProcessedException("Already processed"));

        // when
        Assertions.assertThrows(AlreadyProcessedException.class, () -> artifactAnalyser.analyseArtifact(ARTIFACT_FQN));

        // then
        then(mavenService).shouldHaveNoInteractions();
        then(spoonProcessor).shouldHaveNoInteractions();
    }
}