package vertx

import org.vertx.java.core.Handler
import org.vertx.java.core.http.HttpServerRequest
import org.vertx.java.core.http.RouteMatcher
import org.vertx.java.platform.Verticle
import java.io.File
import org.vertx.java.core.AsyncResultHandler
import org.vertx.java.core.file.AsyncFile
import org.vertx.java.core.AsyncResult
import org.vertx.java.core.streams.Pump

public abstract class Vert() : Verticle() {

    private val routes = RouteMatcher()

    fun get(route: String, routeHandler: (HttpServerRequest) -> Unit) {
        routes.get(route, Handler<HttpServerRequest>() { req -> routeHandler(req!!) })
    }

    fun put(route: String, routeHandler: (HttpServerRequest) -> Unit) {
        routes.put(route, Handler<HttpServerRequest>() { req -> routeHandler(req!!) })
    }

    override fun start() {
        vertx?.createHttpServer()?.requestHandler(routes)?.listen(8081, "localhost");
    }
}

class MyREST : Vert() {
    {
        get("/hello") { request ->
            request.response()?.headers()?.set("Content-Type", "text/plain");
            request.response()?.end("Hello World");
        }

        get("/hello2") { request ->
            request?.response()?.write("web/devices.html")
        }

        get("/hello3") { request ->
            request?.response()?.write("web/devices.html")
        }

        put("/device/:serial") { request ->
            val serial = request.params()?.get("serial") ?: "unknonwn"
            apkUploadHandler(File("/tmp/" + serial + ".apk"), request)
        }
    }

    class A(val request: HttpServerRequest) : AsyncResultHandler<AsyncFile> {
        override fun handle(a: AsyncResult<AsyncFile>?) {
            val result = a!!
            if (result.succeeded()) {
                val file = result.result()
                val pump = Pump.createPump(request, file)
                request?.endHandler { asyncResult ->
                    file?.close(AsyncResultHandler<Void>() { ar ->
                        if (ar?.succeeded() as Boolean) {
                            request?.response()?.end();
                        } else {
                            ar?.cause()?.printStackTrace(System.err);
                        }
                    })
                }
                pump?.start()
                request?.resume()
            }
        }
    }


    fun apkUploadHandler(file: File, request: HttpServerRequest) {
        request?.pause()
        vertx?.fileSystem()?.open(file.canonicalPath, A(request))
    }
}