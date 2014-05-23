package vertx

import java.io.File
import future.Success
import future.Failure
import org.vertx.java.core.json.JsonArray
import org.vertx.java.core.json.JsonObject

public class DeviceVerticle : RESTx(), WithDevice {
    {


        get("/") { request ->
            request?.response()?.sendFile("web/devices.html");
        }

        get("/devices") { request ->
            val json = devices().fold(JsonArray()) {(j, device) ->
                j.addObject(device.asJsonObject())
                j
            }
            container?.logger()?.info("will fetch from MongoDB")
            val s = """
            {
                "action": "find",
                "collection": "devices",
                "matcher": {
                    "state": "OFFLINE"
                }
            }
            """
            vertx?.eventBus()?.sendWithTimeout<JsonObject>("mongodb", JsonObject(s), 5000) {
                if (it?.succeeded()!!) {
                    val devicesFromMongo: JsonObject = it!!.result()!!.body() as JsonObject
                    val devices: JsonArray = devicesFromMongo.getArray("results")!!
                    json.forEach { d ->
                        val j = d as JsonObject;
                        if (!j.getString("serial")?.contains("?")!!) {
                            devices.addObject(d as JsonObject)
                        }
                    }
                    request!!.response()?.headers()?.set("Content-Type", "application/json");
                    request!!.response()?.headers()?.set("Access-Control-Allow-Origin", "*");
                    request.response()?.end(devices.toString());
                } else {
                    container?.logger()?.info("No reply was received before the 5 second timeout!")
                }
            }
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

        get("/device/:serial/apilevel") {(request, device) ->
            json {
                "sdk" to (device.getProperty("ro.build.version.sdk") ?: 0)
                "hh" to "jf"
            }
        }
    }

    fun json(f: Map<Any, Any>.() -> Any) {
        val json = JsonObject()
    }

    override fun start() {
        routes.noMatch {
            it?.response()?.sendFile("web/" + java.io.File(it?.path().toString()));
        }
        vertx?.createHttpServer()?.requestHandler(routes)?.listen(8081, "localhost");
    }
}