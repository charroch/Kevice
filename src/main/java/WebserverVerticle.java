import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;

import java.io.File;
import java.util.regex.Pattern;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
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

        JsonObject config = new JsonObject();
        config.putString("address", "mongodb");
        config.putString("db_name", "adb");
        config.putString("host", "oceanic.mongohq.com");
        config.putNumber("port", 10038);
        String username = "hal9000";
        String password = "monn1v1da";
        if (username != null) {
            config.putString("username", username);
            config.putString("password", password);
        }
        config.putBoolean("fake", false);
        container.deployModule("io.vertx~mod-mongo-persistor~2.1.0", config,
                new Handler<AsyncResult<String>>()
        {
                    @Override
                    public void handle(AsyncResult<String> event) {

                        logger.info(event.result());
                        String s = "{\"action\": \"get_collections\"}";

                        eventBus.sendWithTimeout("mongodb", new JsonObject(s), 5000, new Handler<AsyncResult<Message<JsonObject>>>() {
                                    public void handle(AsyncResult<Message<JsonObject>> result) {
                                        if (result.succeeded()) {
                                            System.out.println("I received a reply " + result.result().body());
                                        } else {

                                            System.err.println("No reply was received before the 1 second timeout!");
                                        }
                                    }
                                }
                        );

                    }
                }
        );

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


        String s = "{\"action\": \"get_collections\"}";
//
//            eventBus.sendWithTimeout("mongodb", s, 1000, new Handler<AsyncResult<Message<String>>>() {
//                        public void handle(AsyncResult<Message<String>> result) {
//                            if (result.succeeded()) {
//                                System.out.println("I received a reply " + result.result().body());
//                            } else {
//
//                                System.err.println("No reply was received before the 1 second timeout!");
//                            }
//                        }
//            }
//    );
        logger.info("should have sent something");



            vertx.createHttpServer().requestHandler(httpRouteMatcher).listen(8080, "localhost");
        vertx.createHttpServer().websocketHandler(new WebSocketAdb(vertx, logger)).listen(8090);
    }
}
