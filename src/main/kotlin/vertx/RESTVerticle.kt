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
import com.android.ddmlib.IDevice

public trait RESTx {

    val routes: RouteMatcher

    fun get(route: String, routeHandler: (HttpServerRequest) -> Unit) {
        routes.get(route, Handler<HttpServerRequest>() { req -> routeHandler(req!!) })
    }

    fun put(route: String, routeHandler: (HttpServerRequest) -> Unit) {
        routes.put(route, Handler<HttpServerRequest>() { req -> routeHandler(req!!) })
    }

    fun post(route: String, routeHandler: (HttpServerRequest) -> Unit) {
        routes.post(route) {
            routeHandler(it!!)
        }
    }
}

trait DeviceVerticle : RESTx, WithDevice, Verticle

trait Result
public class Success<T>(val r: T) : Result
public class Failure() : Result

trait Vert : DeviceVerticle,  Verticle {

    /**
     * fun get(route:String, routeHandler(HttpServerRequest, IDevice) -> Unit) {}
     */

    fun <T> AsyncResult<T>.onSuccess(f: (T) -> Unit) {
        if (this.succeeded()) {
            f(this.result()!!)
        }
    }

    class B(val request: HttpServerRequest, val file: File, val r: (Result) -> Unit) : AsyncResultHandler<Void> {
        override fun handle(ar: AsyncResult<Void>?) {
            if (ar?.succeeded() as Boolean) {
                request?.response()?.end();
                r(Success(file))
            } else {
                ar?.cause()?.printStackTrace(System.err);
            }
        }
    }

    fun HttpServerRequest.save(file: File, r: (Result) -> Unit) {
        this.pause()
        class A(val request: HttpServerRequest) : AsyncResultHandler<AsyncFile> {
            override fun handle(a: AsyncResult<AsyncFile>?) {
                a!!.onSuccess { f ->
                    val pump = Pump.createPump(request, f)
                    request?.endHandler {
                        f.close(B(request, file, r))
                    }
                    pump?.start()
                    request?.resume()
                }
            }
        }
        vertx?.fileSystem()?.open(file.canonicalPath, A(this))
    }



}

class MyREST : Vert,  Verticle() {

    override val routes: RouteMatcher = RouteMatcher();

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

        get2("/hello3") { request ->
            request?.response()?.write("web/devices.html")
        }

        fun RESTx.get(path: String, m: (HttpServerRequest, String) -> Unit) {
            this.get(path) { request ->
                m(request, "hello")
            }
        }

        get("/tete") { request, device ->
            request.response()?.headers()?.set("Content-Type", "text/plain");
            request.response()?.end("Hello World you are reading" + device);
        }

        put("/device/:serial") {(request, device) ->
            val serial = request.params()?.get("serial") ?: "unknonwn"
            request.save(File("/tmp/" + serial + ".apk")) {
                when(it) {
                    is Success<*> -> println(it.r)
                    is Failure -> println("failure")
                }
            }

            println("Look ma I am asynchronous")
        }
    }

    override fun start() {
        vertx?.createHttpServer()?.requestHandler(routes)?.listen(8081, "localhost");
    }
}