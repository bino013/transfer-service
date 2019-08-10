package ph.caleon.transfer.configuration;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author arvin.caleon on 2019-08-03
 **/
public final class ApplicationProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationProperties.class);

    private static final String PROPERTIES_FILE = "application.properties";

    private static final String LOGBACK = "logback.xml";

    private final Config config;

    public ApplicationProperties() {
        initializeLogbackLogging();
        LOGGER.info("Reading application properties file...");
        this.config = ConfigFactory.parseFile(new File(getResourceFile(PROPERTIES_FILE)));
    }

    private void initializeLogbackLogging() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();
        final Path path = new File(getResourceFile(LOGBACK)).toPath();
        try (InputStream stream = Files.newInputStream(path)){
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            configurator.doConfigure(stream);
        } catch (IOException | JoranException e) {
            LOGGER.error("Error occurred while reading logback.xml file", e);
        }
    }

    private String getResourceFile(String file) {
        return getClass().getClassLoader().getResource(file).getFile();
    }

    public String getStringOrDefault(String key, String defaultValue) {
        return config.hasPath(key) ? config.getString(key) : defaultValue;
    }

    public int getIntOrDefault(String key, int defaultValue) {
        return config.hasPath(key) ? config.getInt(key) : defaultValue;
    }

    public boolean getBooleanOrDefault(String key, boolean defaultValue) {
        return config.hasPath(key) ? config.getBoolean(key) : defaultValue;
    }

    public Config getConfig() {
        return config;
    }
}
