package top.jaxlabs.repoReloader.model;

import java.util.List;

public record ReleaseInfo(String tagName, List<ReleaseAsset> assets) {}
