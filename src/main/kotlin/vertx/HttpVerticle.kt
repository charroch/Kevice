package vertx

import org.vertx.java.platform.Verticle
import org.vertx.java.core.http.HttpServer
import org.vertx.java.core.Vertx
import org.vertx.java.core.http.RouteMatcher
import org.vertx.java.core.Handler
import org.vertx.java.core.http.HttpServerRequest


public class HttpVerticle : Verticle() {

    override fun start() {
        val routeMatcher =  RouteMatcher();
        val rest = MyREST()
        //routeMatcher.
       // vertx?.createHttpServer()?.requestHandler(routeMatcher)?.listen(8080, "localhost");
    }
}

public fun Vertx?.createHttpServer(config: HttpServer.() -> Unit): HttpServer {
    val httpServer = this!!.createHttpServer()!!
    httpServer.config()
    return httpServer
}

public fun Verticle.createHttpServer(config: HttpServer.() -> Unit): HttpServer = getVertx().createHttpServer(config)
