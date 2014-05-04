import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;

import java.io.File;
import java.util.regex.Pattern;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

public class WebserverVerticle extends Verticle {

    @Override
    public void start() {
        final Pattern chatUrlPattern = Pattern.compile("/chat/(\\w+)");
        final EventBus eventBus = vertx.eventBus();
        final Logger logger = container.logger();

        JsonObject appConfig = container.config();
        container.deployModule("io.vertx~mod-mongo-persistor~2.1.0", appConfig.getObject("mongo-persistor"));

        AndroidDebugBridge.initIfNeeded(false);
        AndroidDebugBridge.createBridge();
        AndroidDebugBridge.addDeviceChangeListener(new AndroidDebugBridge.IDeviceChangeListener() {
            @Override
            public void deviceConnected(IDevice iDevice) {
                logger.info("iDe");
                String msg = "{\"received\":\"effe\", \"sender\":\"hello\", \"message\": \"hello" + iDevice.getName() + " \"}";
                for (Object chatter : vertx.sharedData().getSet("chat.room.devices")) {
                    eventBus.send((String) chatter, msg);
                }
            }

            @Override
            public void deviceDisconnected(IDevice iDevice) {
            }

            @Override
            public void deviceChanged(IDevice iDevice, int i) {

            }
        });

        RouteMatcher httpRouteMatcher = new RouteMatcher().get("/", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest request) {
                request.response().sendFile("web/chat.html");
            }
        }).get(".*\\.(css|js)$", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest request) {
                request.response().sendFile("web/" + new File(request.path()));
            }
        });

        vertx.createHttpServer().requestHandler(httpRouteMatcher).listen(8080, "localhost");
        vertx.createHttpServer().websocketHandler(new WebSocketAdb(vertx, logger)).listen(8090);
    }
}
