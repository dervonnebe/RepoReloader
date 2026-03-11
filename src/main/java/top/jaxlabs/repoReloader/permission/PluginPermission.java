package top.jaxlabs.repoReloader.permission;

public enum PluginPermission {

    ADMIN_UPDATES("reporeloader.admin.updates"),
    ADMIN_FORCE("reporeloader.admin.force");

    private final String node;

    PluginPermission(String node) {
        this.node = node;
    }

    public String node() {
        return node;
    }
}
