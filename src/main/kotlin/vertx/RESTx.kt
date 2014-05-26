package vertx

import org.vertx.java.core.http.RouteMatcher
import org.vertx.java.core.http.HttpServerRequest
import org.vertx.java.platform.Verticle
import java.io.File
import future.Result
import org.vertx.java.core.AsyncResultHandler
import org.vertx.java.core.AsyncResult
import org.vertx.java.core.streams.Pump
import future.Success
import org.vertx.java.core.Handler


public abstract class RESTx : Verticle(), RouteHelper, Upload {

    override val routes = RouteMatcher()

    override fun start() {
        vertx?.createHttpServer()?.requestHandler(routes)?.listen(8081, "localhost");
    }
}

public trait RouteHelper {
    val routes: RouteMatcher
    fun get(route: String, routeHandler: (HttpServerRequest?) -> Unit) = routes.get(route, routeHandler)
    fun put(route: String, routeHandler: (HttpServerRequest?) -> Unit) = routes.put(route, routeHandler)
    fun post(route: String, routeHandler: (HttpServerRequest?) -> Unit) = routes.post(route, routeHandler)
    fun head(route: String, routeHandler: (HttpServerRequest?) -> Unit) = routes.head(route, routeHandler)
    fun trace(route: String, routeHandler: (HttpServerRequest?) -> Unit) = routes.trace(route, routeHandler)
    fun delete(route: String, routeHandler: (HttpServerRequest?) -> Unit) = routes.delete(route, routeHandler)
}

public trait Async {
    fun <T> AsyncResult<T>.onSuccess(f: (T) -> Unit) {
        if (this.succeeded()) {
            f(this.result()!!)
        }
    }

    fun <T> AsyncResult<T>.onFailure(f: (Throwable) -> Unit) {
        if (!this.succeeded()) {
            f(this.cause()!!)
        }
    }
}

public trait Upload : Verticle, Async {

    fun HttpServerRequest.save(file: File, r: (Result) -> Unit) {
        this.pause()
        val request = this
        getVertx()?.fileSystem()?.open(file.canonicalPath) {
            it!!.onSuccess { f ->
                val pump = Pump.createPump(request, f)
                request.endHandler {
                    f.close (Handler<AsyncResult<Void>> {
                        if (it?.succeeded() as Boolean) {
                            request.response()?.end();
                            r(Success<File>(file))
                        } else {
                            it?.cause()?.printStackTrace(System.err);
                        }
                    })
                }
                pump?.start()
                request.resume()
            }
        }
    }

}
