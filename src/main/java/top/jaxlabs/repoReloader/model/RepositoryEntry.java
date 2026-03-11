package top.jaxlabs.repoReloader.model;

public record RepositoryEntry(
        String owner,
        String repo,
        String localFilename,
        int checkIntervalMinutes
) {
    public String key() {
        return owner + "/" + repo;
    }
}
