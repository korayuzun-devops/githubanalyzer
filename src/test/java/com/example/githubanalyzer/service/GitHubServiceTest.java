package com.example.githubanalyzer.service;

import com.example.githubanalyzer.client.GitHubClient;
import com.example.githubanalyzer.entity.ContributorEntity;
import com.example.githubanalyzer.entity.RepoEntity;
import com.example.githubanalyzer.repository.ContributorRepository;
import com.example.githubanalyzer.repository.RepoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GitHubServiceTest {

    @Mock
    private GitHubClient gitHubClient;

    @Mock
    private RepoRepository repoRepository;

    @Mock
    private ContributorRepository contributorRepository;

    @InjectMocks
    private GitHubService gitHubService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFetchAndSaveTopApacheRepos() throws Exception {
        // Mock the GitHub API responses
        String reposResponse = getTestReposJson();
        String contributorsResponse = getTestContributorsJson();
        String userInfoResponse = getTestUserInfoJson();

        // Configure mocks
        when(gitHubClient.getApacheRepos(anyInt())).thenReturn(reposResponse);
        when(gitHubClient.getRepoContributors(anyString(), anyInt())).thenReturn(contributorsResponse);
        when(gitHubClient.getUserInfo(anyString())).thenReturn(userInfoResponse);
        
        // Mock repository save methods
        when(repoRepository.save(any(RepoEntity.class))).thenAnswer(invocation -> {
            RepoEntity entity = invocation.getArgument(0);
            entity.setId(1L); // Simulate auto-generated ID
            return entity;
        });
        
        when(contributorRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<ContributorEntity> entities = invocation.getArgument(0);
            // Simulate auto-generated IDs
            for (int i = 0; i < entities.size(); i++) {
                entities.get(i).setId((long) (i + 1));
            }
            return entities;
        });

        // Execute the method under test
        List<RepoEntity> result = gitHubService.fetchAndSaveTopApacheRepos();

        // Verify the results
        assertNotNull(result);
        assertEquals(2, result.size()); // We have 2 repos in our test data
        
        // Verify the first repository
        RepoEntity repo1 = result.get(0);
        assertEquals("apache/commons-lang", repo1.getFullName());
        assertEquals(4500, repo1.getStars());
        assertEquals("Java", repo1.getLanguage());
        
        // Verify interactions with mocks
        verify(gitHubClient).getApacheRepos(100);
        verify(gitHubClient, times(2)).getRepoContributors(anyString(), eq(10));
        verify(gitHubClient, times(2)).getUserInfo(anyString());
        verify(repoRepository, times(2)).save(any(RepoEntity.class));
        verify(contributorRepository, times(2)).saveAll(anyList());
    }

    @Test
    void testGetAllRepos() {
        // Mock repository response
        RepoEntity repo1 = new RepoEntity();
        repo1.setId(1L);
        repo1.setFullName("apache/commons-lang");
        
        RepoEntity repo2 = new RepoEntity();
        repo2.setId(2L);
        repo2.setFullName("apache/commons-io");
        
        when(repoRepository.findAll()).thenReturn(Arrays.asList(repo1, repo2));
        
        // Execute the method under test
        List<RepoEntity> result = gitHubService.getAllRepos();
        
        // Verify the results
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("apache/commons-lang", result.get(0).getFullName());
        assertEquals("apache/commons-io", result.get(1).getFullName());
        
        // Verify interactions with mocks
        verify(repoRepository).findAll();
    }

    @Test
    void testGetAllContributors() {
        // Mock repository response
        ContributorEntity contributor1 = new ContributorEntity();
        contributor1.setId(1L);
        contributor1.setLogin("user1");
        
        ContributorEntity contributor2 = new ContributorEntity();
        contributor2.setId(2L);
        contributor2.setLogin("user2");
        
        when(contributorRepository.findAll()).thenReturn(Arrays.asList(contributor1, contributor2));
        
        // Execute the method under test
        List<ContributorEntity> result = gitHubService.getAllContributors();
        
        // Verify the results
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getLogin());
        assertEquals("user2", result.get(1).getLogin());
        
        // Verify interactions with mocks
        verify(contributorRepository).findAll();
    }

    // Helper methods to provide test JSON data
    
    private String getTestReposJson() {
        return "[\n" +
               "  {\n" +
               "    \"id\": 1,\n" +
               "    \"name\": \"commons-lang\",\n" +
               "    \"full_name\": \"apache/commons-lang\",\n" +
               "    \"description\": \"Apache Commons Lang\",\n" +
               "    \"html_url\": \"https://github.com/apache/commons-lang\",\n" +
               "    \"stargazers_count\": 4500,\n" +
               "    \"watchers_count\": 4500,\n" +
               "    \"language\": \"Java\",\n" +
               "    \"forks_count\": 2000,\n" +
               "    \"open_issues_count\": 120,\n" +
               "    \"open_issues\": 120,\n" +
               "    \"license\": {\n" +
               "      \"key\": \"apache-2.0\",\n" +
               "      \"name\": \"Apache License 2.0\"\n" +
               "    },\n" +
               "    \"contributors_url\": \"https://api.github.com/repos/apache/commons-lang/contributors\"\n" +
               "  },\n" +
               "  {\n" +
               "    \"id\": 2,\n" +
               "    \"name\": \"commons-io\",\n" +
               "    \"full_name\": \"apache/commons-io\",\n" +
               "    \"description\": \"Apache Commons IO\",\n" +
               "    \"html_url\": \"https://github.com/apache/commons-io\",\n" +
               "    \"stargazers_count\": 3500,\n" +
               "    \"watchers_count\": 3500,\n" +
               "    \"language\": \"Java\",\n" +
               "    \"forks_count\": 1500,\n" +
               "    \"open_issues_count\": 80,\n" +
               "    \"open_issues\": 80,\n" +
               "    \"license\": {\n" +
               "      \"key\": \"apache-2.0\",\n" +
               "      \"name\": \"Apache License 2.0\"\n" +
               "    },\n" +
               "    \"contributors_url\": \"https://api.github.com/repos/apache/commons-io/contributors\"\n" +
               "  }\n" +
               "]";
    }
    
    private String getTestContributorsJson() {
        return "[\n" +
               "  {\n" +
               "    \"login\": \"garydgregory\",\n" +
               "    \"id\": 105904,\n" +
               "    \"contributions\": 845\n" +
               "  }\n" +
               "]";
    }
    
    private String getTestUserInfoJson() {
        return "{\n" +
               "  \"login\": \"garydgregory\",\n" +
               "  \"id\": 105904,\n" +
               "  \"name\": \"Gary Gregory\",\n" +
               "  \"company\": \"Rocket Software\",\n" +
               "  \"location\": \"Denver, CO, USA\"\n" +
               "}";
    }
}