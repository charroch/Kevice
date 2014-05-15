package web

import org.vertx.java.platform.Verticle
import org.vertx.java.core.http.RouteMatcher

public class WebVerticle: Verticle() {

    override fun start() {
        //vertx?.createHttpServer()?.requestHandler(httpRouteMatcher)?.listen(8080, "localhost");
    }
}