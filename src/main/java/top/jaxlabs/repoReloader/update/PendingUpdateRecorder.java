package top.jaxlabs.repoReloader.update;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;

/**
 * Appends a line to {@code pending-updates.txt} in the plugin data folder
 * whenever a new update has been downloaded and is awaiting a server restart.
 */
public final class PendingUpdateRecorder {

    private final Path recordFile;
    private final Logger logger;

    public PendingUpdateRecorder(Path dataFolder, Logger logger) {
        this.recordFile = dataFolder.resolve("pending-updates.txt");
        this.logger = logger;
    }

    public void record(String repositoryKey, String tagName, Path updatePath) {
        String line = repositoryKey + " -> " + tagName + " -> " + updatePath + System.lineSeparator();
        try {
            Files.createDirectories(recordFile.getParent());
            Files.writeString(recordFile, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException exception) {
            logger.warning("Could not write pending-updates.txt: " + exception.getMessage());
        }
    }
}
