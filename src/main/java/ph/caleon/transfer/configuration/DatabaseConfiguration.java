package ph.caleon.transfer.configuration;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

/**
 * @author arvin.caleon on 2019-08-10
 **/
public final class DatabaseConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfiguration.class);

    private static final String INIT_SQL = "init.sql";

    private final Jdbi jdbi;

    public DatabaseConfiguration(ApplicationProperties properties) {
        jdbi = Jdbi.create(properties.getStringOrDefault("database.url", "jdbc:h2:~/test;SCHEMA=TRANSFER_APP"));
        executeInitFile(properties);
    }

    private void executeInitFile(ApplicationProperties properties) {
        final boolean isToInitDb = properties.getBooleanOrDefault("database.init-db", true);
        if (isToInitDb) {
            LOGGER.info("Executing init sql file...");
            final String file = getResourceFile(INIT_SQL);
            try {
                final List<String> strings = Files.readAllLines(new File(file).toPath());
                try (final Handle handle = jdbi.open();
                     final Script script = handle.createScript(String.join("", strings))) {
                    script.execute();
                }
            } catch (Exception e) {
                LOGGER.error("Error occurred while executing init sql file", e);
            }
        }
    }

    private String getResourceFile(String file) {
        return getClass().getClassLoader().getResource(file).getFile();
    }

    public Jdbi getJdbi() {
        return jdbi;
    }
}
