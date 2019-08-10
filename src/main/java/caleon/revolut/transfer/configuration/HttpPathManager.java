package caleon.revolut.transfer.configuration;

import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.util.HttpString;

/**
 * @author arvin.caleon on 2019-08-03
 **/
public final class HttpPathManager {

    private final PathHandler pathHandler = Handlers.path();

    private final RoutingHandler routingHandler = Handlers.routing(false);

    public void addHandler(HttpString method, String template, HttpHandler handler) {
        routingHandler.add(method, template, handler);
    }

    public PathHandler getPathHandler() {
        return pathHandler.addPrefixPath("v1", routingHandler);
    }
}
