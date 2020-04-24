package com.york.sdp518;

import com.york.sdp518.domain.Artifact;
import com.york.sdp518.domain.ProcessingState;
import com.york.sdp518.domain.Repository;
import com.york.sdp518.exception.AlreadyProcessedException;
import com.york.sdp518.exception.JavaParseToGraphException;
import com.york.sdp518.exception.MavenMetadataException;
import com.york.sdp518.exception.VCSClientException;
import com.york.sdp518.processor.SpoonProcessor;
import com.york.sdp518.service.VCSClient;
import com.york.sdp518.service.impl.MavenDependencyManagementService;
import com.york.sdp518.service.impl.Neo4jServiceFactory;
import com.york.sdp518.service.impl.ProcessableNeo4jService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.util.stream.Collectors;

import static org.mockito.BDDMockito.*;

import static org.assertj.core.api.Assertions.*;

//https://medium.com/neo4j/testing-your-neo4j-based-java-application-34bef487cc3c
@ExtendWith(SpringExtension.class)
//@ContextConfiguration(classes = {TestContextConfiguration.class})
//@ActiveProfiles("test")
public class RepositoryAnalyserTest {

//    @Container
//    private static final Neo4jContainer<?> DATABASE_SERVER = new Neo4jContainer<>();



    private static final String REPOSITORY_URL = "https://github.com/test/mock-repository.git";
    private static final String REPOSITORY_NAME = "mock-repository";
    private static final File MOCK_REPOSITORY_PATH = new File("./src/test/resources/clones/github/mock-repository");

    private static final String MOCK_GROUP_ID = "org.test.mock";

    @MockBean
    VCSClient vcsClient;
    @MockBean
    MavenDependencyManagementService mavenService;
    @MockBean
    ArtifactAnalyser artifactAnalyser;
    @MockBean
    SpoonProcessor spoonProcessor;
    @MockBean
    Neo4jServiceFactory neo4jServiceFactory;

    @Mock
    ProcessableNeo4jService<Repository> neo4jServiceMock;

    RepositoryAnalyser repositoryAnalyser;

    @BeforeEach
    void setUp() {
        given(neo4jServiceFactory.getServiceForProcessableEntity(Repository.class))
                .willReturn(neo4jServiceMock);
        this.repositoryAnalyser = new RepositoryAnalyser(vcsClient, mavenService, artifactAnalyser, spoonProcessor, neo4jServiceFactory);
    }

    @Test
    public void analyseRepositoryAsLibrary() throws Exception {
        // given
        given(vcsClient.clone(REPOSITORY_URL)).willReturn(MOCK_REPOSITORY_PATH);
        given(mavenService.isPublishedArtifact(MOCK_GROUP_ID, "mock-parent")).willReturn(true);
        given(mavenService.getLatestVersion(MOCK_GROUP_ID, "mock-core")).willReturn("3.0.0");
        given(mavenService.getLatestVersion(MOCK_GROUP_ID, "mock-utils"))
                .willThrow(new MavenMetadataException("Could not read metadata"));
        given(neo4jServiceMock.tryToBeginProcessing(any(Repository.class))).willAnswer(invocationOnMock -> {
            Repository repo = invocationOnMock.getArgument(0, Repository.class);
            repo.setProcessingState(ProcessingState.IN_PROGRESS);
            return repo;
        });
        willAnswer(invocationOnMock -> {
            Artifact artifact = invocationOnMock.getArgument(0, Artifact.class);
            artifact.setProcessingState(ProcessingState.COMPLETED);
            return null; // void method
        }).given(artifactAnalyser).analyseArtifact(any(Artifact.class));

        // when
        repositoryAnalyser.analyseRepository(REPOSITORY_URL);

        // then
        then(vcsClient).should(times(1)).clone(REPOSITORY_URL);

        InOrder inOrder = Mockito.inOrder(mavenService);
        then(mavenService).should(inOrder).isPublishedArtifact(MOCK_GROUP_ID, "mock-parent");
        then(mavenService).should(inOrder, times(2)).getLatestVersion(eq(MOCK_GROUP_ID), anyString());

        then(artifactAnalyser).should(times(1)).analyseArtifact(any(Artifact.class));

        then(spoonProcessor).shouldHaveNoInteractions();

        ArgumentCaptor<Repository> repoCaptor = ArgumentCaptor.forClass(Repository.class);
        then(neo4jServiceMock).should(times(1)).createOrUpdate(repoCaptor.capture());

        Repository postProcessingRepo = repoCaptor.getValue();
        assertRepo(repoCaptor.getValue(), ProcessingState.COMPLETED, 1);

        Artifact processedArtifact = postProcessingRepo.getArtifacts().iterator().next();
        assertThat(processedArtifact.getArtifactId()).isEqualTo("mock-core");
        assertThat(processedArtifact.getProcessingState()).isEqualTo(ProcessingState.COMPLETED);
    }

    @Test
    public void analyseRepositoryAsRepository() throws Exception {
        // given
        given(vcsClient.clone(REPOSITORY_URL)).willReturn(MOCK_REPOSITORY_PATH);
        given(mavenService.isPublishedArtifact(MOCK_GROUP_ID, "mock-parent")).willReturn(false);
        given(neo4jServiceMock.tryToBeginProcessing(any(Repository.class))).willAnswer(invocationOnMock -> {
            Repository repo = invocationOnMock.getArgument(0, Repository.class);
            repo.setProcessingState(ProcessingState.IN_PROGRESS);
            return repo;
        });
        willDoNothing().given(spoonProcessor).process(any());

        // when
        repositoryAnalyser.analyseRepository(REPOSITORY_URL);

        // then
        then(vcsClient).should(times(1)).clone(REPOSITORY_URL);

        then(mavenService).should(times(1)).isPublishedArtifact(MOCK_GROUP_ID, "mock-parent");
        then(mavenService).should(never()).getLatestVersion(anyString(), anyString());

        then(artifactAnalyser).shouldHaveNoInteractions();

        ArgumentCaptor<Repository> repoCaptor = ArgumentCaptor.forClass(Repository.class);
        then(neo4jServiceMock).should(times(1)).createOrUpdate(repoCaptor.capture());

        Repository postProcessingRepo = repoCaptor.getValue();
        assertRepo(repoCaptor.getValue(), ProcessingState.COMPLETED, 2);
        assertThat(postProcessingRepo.getArtifacts().stream().map(Artifact::getArtifactId).collect(Collectors.toList()))
                .containsExactlyInAnyOrder("mock-core", "mock-utils");
        assertThat(postProcessingRepo.getArtifacts().stream().allMatch(a -> a.getProcessingState().equals(ProcessingState.COMPLETED)))
                .isTrue();
    }

    @Test
    public void analyseRepositoryAsRepositoryFails() throws Exception {
        // given
        given(neo4jServiceMock.tryToBeginProcessing(any(Repository.class))).willAnswer(invocationOnMock -> {
            Repository repo = invocationOnMock.getArgument(0, Repository.class);
            repo.setProcessingState(ProcessingState.IN_PROGRESS);
            return repo;
        });
        given(vcsClient.clone(REPOSITORY_URL)).willThrow(new VCSClientException("Could not clone"));

        // when
        Assertions.assertThrows(JavaParseToGraphException.class, () -> repositoryAnalyser.analyseRepository(REPOSITORY_URL));

        // then
        then(mavenService).shouldHaveNoInteractions();
        then(artifactAnalyser).shouldHaveNoInteractions();
        then(spoonProcessor).shouldHaveNoInteractions();

        ArgumentCaptor<Repository> repoCaptor = ArgumentCaptor.forClass(Repository.class);
        then(neo4jServiceMock).should(times(1)).createOrUpdate(repoCaptor.capture());

        assertRepo(repoCaptor.getValue(), ProcessingState.FAILED, 0);
    }

    @Test
    public void alreadyProcessedRepository() throws Exception {
        // given
        given(neo4jServiceMock.tryToBeginProcessing(any(Repository.class))).willThrow(new AlreadyProcessedException("Already processed"));

        // when
        Assertions.assertThrows(AlreadyProcessedException.class, () -> repositoryAnalyser.analyseRepository(REPOSITORY_URL));

        // then
        then(mavenService).shouldHaveNoInteractions();
        then(vcsClient).shouldHaveNoInteractions();
        then(artifactAnalyser).shouldHaveNoInteractions();
        then(spoonProcessor).shouldHaveNoInteractions();
    }

    private void assertRepo(Repository repository, ProcessingState processingState, int numberOfArtifacts) {
        assertThat(repository.getUrl())
                .isEqualTo(REPOSITORY_URL);
        assertThat(repository.getName())
                .isEqualTo(REPOSITORY_NAME);
        assertThat(repository.getProcessingState())
                .isEqualTo(processingState);
        assertThat(repository.getArtifacts().size())
                .isEqualTo(numberOfArtifacts);
    }
}