import java.util.regex.Pattern;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import adb.LocalDeviceBridgeVerticle;
import vertx.MyREST;

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
                new Handler<AsyncResult<String>>() {
                    @Override
                    public void handle(AsyncResult<String> event) {

                        logger.info(event.result());
                        String s = "{\"action\": \"find\"}";

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

        container.deployWorkerVerticle(LocalDeviceBridgeVerticle.class.getName(), null, 1, false);
        vertx.createHttpServer().websocketHandler(new WebSocketAdb(vertx, logger)).listen(8090);
        container.deployVerticle(MyREST.class.getName(), new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> event) {

                System.err.println("An event" + event.result());
            }
        });
    }
}
