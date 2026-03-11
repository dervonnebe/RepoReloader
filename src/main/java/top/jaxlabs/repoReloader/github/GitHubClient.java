package top.jaxlabs.repoReloader.github;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import top.jaxlabs.repoReloader.model.ReleaseAsset;
import top.jaxlabs.repoReloader.model.ReleaseInfo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Thin wrapper around the GitHub REST API.
 * Handles authentication, request building, and JSON deserialization into model objects.
 */
public final class GitHubClient {

    private static final String API_BASE = "https://api.github.com";
    private static final String GITHUB_API_VERSION = "2022-11-28";

    private final HttpClient httpClient;
    private final String token;

    public GitHubClient(String token) {
        this.token = token;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    /**
     * Fetches the latest release for the given repository.
     *
     * @throws IOException          on non-200 response or network error
     * @throws InterruptedException if the thread is interrupted
     */
    public ReleaseInfo fetchLatestRelease(String owner, String repo) throws IOException, InterruptedException {
        String url = API_BASE + "/repos/" + owner + "/" + repo + "/releases/latest";
        HttpRequest request = buildRequest(url).GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " fetching latest release for " + owner + "/" + repo);
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        String tagName = jsonString(json, "tag_name");
        if (tagName.isEmpty()) {
            throw new IOException("tag_name missing in release response for " + owner + "/" + repo);
        }

        return new ReleaseInfo(tagName, parseAssets(json.getAsJsonArray("assets")));
    }

    /**
     * Downloads a release asset by its API URL and returns the raw {@link InputStream}.
     * The caller is responsible for closing the stream.
     */
    public InputStream downloadAsset(String assetApiUrl) throws IOException, InterruptedException {
        HttpRequest request = buildRequest(assetApiUrl)
                .header("Accept", "application/octet-stream")
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            throw new IOException("Asset download failed with HTTP " + response.statusCode());
        }
        return response.body();
    }

    private HttpRequest.Builder buildRequest(String url) {
        return HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/vnd.github+json")
                .header("Authorization", "Bearer " + token)
                .header("X-GitHub-Api-Version", GITHUB_API_VERSION)
                .header("User-Agent", "RepoReloader");
    }

    private List<ReleaseAsset> parseAssets(JsonArray array) {
        if (array == null) return List.of();
        List<ReleaseAsset> assets = new ArrayList<>();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) continue;
            JsonObject obj = element.getAsJsonObject();
            String name = jsonString(obj, "name");
            String url = jsonString(obj, "url");
            if (!name.isEmpty() && !url.isEmpty()) {
                assets.add(new ReleaseAsset(name, url));
            }
        }
        return assets;
    }

    private String jsonString(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return "";
        return obj.get(key).getAsString();
    }
}
