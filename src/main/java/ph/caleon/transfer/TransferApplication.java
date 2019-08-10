package ph.caleon.transfer;

import ph.caleon.transfer.configuration.ApplicationProperties;
import ph.caleon.transfer.configuration.DatabaseConfiguration;
import ph.caleon.transfer.configuration.server.HttpPathManager;
import ph.caleon.transfer.configuration.server.ServerConfiguration;
import ph.caleon.transfer.configuration.server.ServerFileProperties;
import ph.caleon.transfer.handler.TransferHandler;
import io.undertow.util.Methods;
import ph.caleon.transfer.service.TransferServiceImpl;

/**
 * @author arvin.caleon on 2019-08-03
 **/
public class TransferApplication {

    private static final String TRANSFER_PATH = "/transfer";

    public static void main(String[] args) {
        ApplicationProperties properties = new ApplicationProperties();
        DatabaseConfiguration dbConfig = new DatabaseConfiguration(properties);
        HttpPathManager pathManager = new HttpPathManager();
        pathManager.addHandler(Methods.POST, TRANSFER_PATH,
                new TransferHandler(new TransferServiceImpl(dbConfig.getJdbi())));
        ServerConfiguration serverConfiguration = new ServerConfiguration(new ServerFileProperties(properties),
                pathManager.getPathHandler());
        serverConfiguration.startServer();
        Runtime.getRuntime().addShutdownHook(new Thread(serverConfiguration::stopServer));
    }

}
