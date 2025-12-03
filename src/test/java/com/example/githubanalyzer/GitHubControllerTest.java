package com.example.githubanalyzer;

import com.example.githubanalyzer.controller.GitHubController;
import com.example.githubanalyzer.entity.ContributorEntity;
import com.example.githubanalyzer.entity.RepoEntity;
import com.example.githubanalyzer.service.GitHubService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GitHubController.class)
public class GitHubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GitHubService gitHubService;

    @Test
    public void testFetchAndSaveEndpoint() throws Exception {
        // Prepare mock data
        List<RepoEntity> mockRepos = new ArrayList<>();

        RepoEntity repo1 = new RepoEntity();
        repo1.setId(1L);
        repo1.setFullName("apache/commons-lang");
        repo1.setStars(4500);
        repo1.setLanguage("Java");

        RepoEntity repo2 = new RepoEntity();
        repo2.setId(2L);
        repo2.setFullName("apache/commons-io");
        repo2.setStars(3500);
        repo2.setLanguage("Java");

        mockRepos.add(repo1);
        mockRepos.add(repo2);

        // Configure mock service
        when(gitHubService.fetchAndSaveTopApacheRepos()).thenReturn(mockRepos);

        // Test the endpoint
        mockMvc.perform(post("/api/fetch-and-save"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$[0].fullName").value("apache/commons-lang"))
               .andExpect(jsonPath("$[0].stars").value(4500))
               .andExpect(jsonPath("$[0].language").value("Java"))
               .andExpect(jsonPath("$[1].fullName").value("apache/commons-io"))
               .andExpect(jsonPath("$[1].stars").value(3500))
               .andExpect(jsonPath("$[1].language").value("Java"));
    }

    @Test
    public void testGetAllReposEndpoint() throws Exception {
        // Prepare mock data
        List<RepoEntity> mockRepos = new ArrayList<>();

        RepoEntity repo1 = new RepoEntity();
        repo1.setId(1L);
        repo1.setFullName("apache/commons-lang");

        RepoEntity repo2 = new RepoEntity();
        repo2.setId(2L);
        repo2.setFullName("apache/commons-io");

        mockRepos.add(repo1);
        mockRepos.add(repo2);

        // Configure mock service
        when(gitHubService.getAllRepos()).thenReturn(mockRepos);

        // Test the endpoint
        mockMvc.perform(get("/api/repos"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$[0].fullName").value("apache/commons-lang"))
               .andExpect(jsonPath("$[1].fullName").value("apache/commons-io"));
    }

    @Test
    public void testGetAllContributorsEndpoint() throws Exception {
        // Prepare mock data
        List<ContributorEntity> mockContributors = new ArrayList<>();

        ContributorEntity contributor1 = new ContributorEntity();
        contributor1.setId(1L);
        contributor1.setLogin("user1");
        contributor1.setLocation("Location 1");
        contributor1.setCompany("Company 1");
        contributor1.setContributions(100);

        ContributorEntity contributor2 = new ContributorEntity();
        contributor2.setId(2L);
        contributor2.setLogin("user2");
        contributor2.setLocation("Location 2");
        contributor2.setCompany("Company 2");
        contributor2.setContributions(50);

        mockContributors.add(contributor1);
        mockContributors.add(contributor2);

        // Configure mock service
        when(gitHubService.getAllContributors()).thenReturn(mockContributors);

        // Test the endpoint
        mockMvc.perform(get("/api/contributors"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$[0].login").value("user1"))
               .andExpect(jsonPath("$[0].location").value("Location 1"))
               .andExpect(jsonPath("$[0].company").value("Company 1"))
               .andExpect(jsonPath("$[0].contributions").value(100))
               .andExpect(jsonPath("$[1].login").value("user2"))
               .andExpect(jsonPath("$[1].location").value("Location 2"))
               .andExpect(jsonPath("$[1].company").value("Company 2"))
               .andExpect(jsonPath("$[1].contributions").value(50));
    }

    @Test
    public void testGetFormattedInfoEndpoint() throws Exception {
        // Prepare mock data
        String mockFormattedInfo = "Formatted repository and contributor information";

        // Configure mock service
        when(gitHubService.getFormattedRepoAndContributorInfo()).thenReturn(mockFormattedInfo);

        // Test the endpoint
        mockMvc.perform(get("/api/formatted-info"))
               .andExpect(status().isOk())
               .andExpect(content().string(mockFormattedInfo));
    }

    // Legacy endpoint tests

    @Test
    public void testTopContributorsEndpoint() throws Exception {
        // Prepare mock data
        Map<String, Integer> mockContributors = new HashMap<>();
        mockContributors.put("user1", 100);
        mockContributors.put("user2", 50);

        // Configure mock service
        when(gitHubService.getTopContributors()).thenReturn(mockContributors);

        // Test the endpoint
        mockMvc.perform(get("/api/top-contributors"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.user1").value(100))
               .andExpect(jsonPath("$.user2").value(50));
    }

    @Test
    public void testTopReposEndpoint() throws Exception {
        // Prepare mock data
        List<Map<String, Object>> mockRepos = new ArrayList<>();
        Map<String, Object> repo1 = new HashMap<>();
        repo1.put("name", "repo1");
        repo1.put("stars", 1000);
        repo1.put("description", "Description 1");

        Map<String, Object> repo2 = new HashMap<>();
        repo2.put("name", "repo2");
        repo2.put("stars", 500);
        repo2.put("description", "Description 2");

        mockRepos.add(repo1);
        mockRepos.add(repo2);

        // Configure mock service
        when(gitHubService.fetchTopApacheRepos()).thenReturn(mockRepos);

        // Test the endpoint
        mockMvc.perform(get("/api/top-repos"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$[0].name").value("repo1"))
               .andExpect(jsonPath("$[0].stars").value(1000))
               .andExpect(jsonPath("$[0].description").value("Description 1"))
               .andExpect(jsonPath("$[1].name").value("repo2"))
               .andExpect(jsonPath("$[1].stars").value(500))
               .andExpect(jsonPath("$[1].description").value("Description 2"));
    }


}
