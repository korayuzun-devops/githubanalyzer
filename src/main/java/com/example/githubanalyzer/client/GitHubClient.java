package com.example.githubanalyzer.client;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class GitHubClient {

    @Value("${github.api.token}")
    private String token;

    private static final String API_BASE_URL = "https://api.github.com";
    private static final String APACHE_ORG = "apache";

    /**
     * Get recently updated repositories from Apache organization
     * @param perPage Number of repositories to fetch per page
     * @return JSON response from GitHub API
     */
    public String getApacheRepos(int perPage) throws Exception {
        String url = API_BASE_URL + "/orgs/" + APACHE_ORG + "/repos?sort=updated&direction=desc&per_page=" + perPage;
        return executeGetRequest(url);
    }

    /**
     * Get contributors for a repository
     * @param repoName Repository name
     * @param perPage Number of contributors to fetch per page
     * @return JSON response from GitHub API
     */
    public String getRepoContributors(String repoName, int perPage) throws Exception {
        String url = API_BASE_URL + "/repos/" + APACHE_ORG + "/" + repoName + "/contributors?per_page=" + perPage;
        return executeGetRequest(url);
    }

    /**
     * Get user information
     * @param username GitHub username
     * @return JSON response from GitHub API
     */
    public String getUserInfo(String username) throws Exception {
        String url = API_BASE_URL + "/users/" + username;
        return executeGetRequest(url);
    }

    /**
     * Execute a GET request to the GitHub API
     * @param url API endpoint URL
     * @return JSON response from GitHub API
     */
    private String executeGetRequest(String url) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            request.addHeader("Authorization", "token " + token);
            request.addHeader("Accept", "application/vnd.github.v3+json");

            try (CloseableHttpResponse response = client.execute(request)) {
                return EntityUtils.toString(response.getEntity());
            }
        }
    }

    /**
     * Legacy method for backward compatibility
     */
    public String getJson(String query) throws Exception {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = API_BASE_URL + "/search/repositories?q=" + encodedQuery + "&sort=stars&per_page=5";
        return executeGetRequest(url);
    }
}
