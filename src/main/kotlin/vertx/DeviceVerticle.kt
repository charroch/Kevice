package vertx

import org.vertx.java.core.http.HttpServerRequest
import java.io.File
import future.Success
import future.Failure
import org.vertx.java.core.json.JsonArray
import org.vertx.java.core.json.JsonObject

public class DeviceVerticle : RESTx(), WithDevice {
    {
        get("/") {
            it?.response()?.sendFile("web/devices.html");
        }

        get("/devices") { request ->
            val json = JsonArray()
            val response = devices().fold(json) {(j, device) ->
                j.addObject(device.asJsonObject())
                j
            }

            container?.logger()?.info("will fetch from MongoDB")

            val s = """{"action": "find", "collection": "devices"}"""
            vertx?.eventBus()?.sendWithTimeout<JsonObject>("mongodb", JsonObject(s), 5000) {
                if (it?.succeeded()!!) {
                    val jso:JsonObject = it!!.result()!!.body() as JsonObject
                    val devices = jso.getArray("results")!!
                    json.forEach { d -> devices.addObject(d as JsonObject) }
                    container?.logger()?.info("I received a reply " + devices.size() + " " + json.size())

                    request!!.response()?.headers()?.set("Content-Type", "application/json");
                    request!!.response()?.headers()?.set("Access-Control-Allow-Origin", "*");
                    request.response()?.end(devices.toString());
                } else {
                    container?.logger()?.info("No reply was received before the 1 second timeout!")
                }
            }
//
//            request!!.response()?.headers()?.set("Content-Type", "application/json");
//            request!!.response()?.headers()?.set("Access-Control-Allow-Origin", "*");
//            request.response()?.end(response.toString());
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
                        val install = device.install(it.r as File)
                        install.onSuccess {
                            container?.logger()?.info("Installed Apk correctly")
                        }
                        install.onFailure {
                            container?.logger()?.error("failed to install", it)
                        }
                    };
                    is Failure -> println("failure")
                }
            }
            println("Look ma I am asynchronous")
        }
    }

    override fun start() {
        routes.noMatch {
            it?.response()?.sendFile("web/" + java.io.File(it?.path().toString()));
        }
        vertx?.createHttpServer()?.requestHandler(routes)?.listen(8081, "localhost");
    }
}