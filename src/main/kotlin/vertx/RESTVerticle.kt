package vertx

import org.vertx.java.core.http.HttpServerRequest
import java.io.File
import future.Success
import future.Failure
import org.vertx.java.platform.Verticle

public class DeviceVerticle : RESTx(), WithDevice {
    {
        get("/hello") { request ->
            request!!.response()?.headers()?.set("Content-Type", "text/plain");
            request.response()?.end("Hello World");
        }

        get("/hello2") { request ->
            request?.response()?.write("web/devices.html")
        }

        get("/hello3") { request ->
            request?.response()?.write("web/devices.html")
        }

        fun RESTx.get(path: String, m: (HttpServerRequest, String) -> Unit) {
            this.get(path) { request ->
                m(request!!, "hello")
            }
        }

        get("/tete") { request, device ->
            request.response()?.headers()?.set("Content-Type", "text/plain");
            request.response()?.end("Hello World you are reading" + device);
        }

        put("/device/:serial") {(request, device) ->
            request?.save(File("/tmp/" + device.getSerialNumber() + ".apk")) {
                when(it) {
                    is Success<*> -> {
                        println(it.r)
                        device.install(it.r as File).onSuccess {
                            println("Done!")
                        }
                    };
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