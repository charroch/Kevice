package vertx

import java.io.File
import future.Success
import future.Failure
import org.vertx.java.core.json.JsonArray
import org.vertx.java.core.json.JsonObject
import json.JSON
import com.android.ddmlib.IShellOutputReceiver
import org.vertx.java.core.http.HttpServerResponse
import java.util.concurrent.TimeUnit
import com.fasterxml.jackson.databind.node.TextNode
import org.vertx.java.platform.Container
import kotlin.properties.Delegates
import org.vertx.java.core.Vertx
import org.vertx.java.core.logging.Logger

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
            l.info("will fetch from MongoDB")
            val s = """
            {
                "action": "find",
                "collection": "devices",
                "matcher": {
                    "state": "OFFLINE"
                }
            }
            """
            v.eventBus()?.sendWithTimeout<JsonObject>("mongodb", JsonObject(s), 5000) {
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
                    l.info("No reply was received before the 5 second timeout!")
                }
            }
        }

        put("/device/:serial") {(request, device) ->
            request?.save(File("/tmp/" + device.getSerialNumber() + ".apk")) {
                when(it) {
                    is Success<*> -> {
                        val install = device.install(it.r as File)
                        install.onSuccess {
                            l.info("Installed Apk correctly")
                        }
                        install.onFailure {
                            l.error("failed to install", it)
                        }
                    };
                    is Failure -> println("failure")
                }
            }
            println("Look ma I am asynchronous")
        }

        get("/device/:serial/apilevel") {(request, device) ->
            json.json {
                obj {
                    "sdk" to (device.getProperty("ro.build.version.sdk") ?: 0) as String
                }
            }
        }

        delete("/device/:serial/packages/:package") {(request, device, pkg) ->
            val log = device.uninstallPackage(pkg)
            if (log == null) request.response()?.setStatusCode(200)?.end()
            else request.response()?.setStatusCode(500)?.setStatusMessage(log)?.end()
        }

        post("/device/:serial/shell") {(request, device) ->
            request.bodyHandler {
                val cmd = JSON.JSON.mapper.readTree(it.toString())?.get("command") as TextNode
                device.executeShellCommand(cmd.textValue(), ServerSentEventOutput(request.response()!!), 10, TimeUnit.SECONDS)
            }
        }


    }

    val c: Container by Delegates.lazy { getContainer()!! }
    val v: Vertx by Delegates.lazy { getVertx()!! }
    val l: Logger by Delegates.lazy { c.logger()!! }

    class ServerSentEventOutput(val response: HttpServerResponse) : IShellOutputReceiver {

        {
            response.setChunked(true)
            response.headers()?.set("Content-Type", "text/event-stream")
            response.headers()?.set("Access-Control-Allow-Origin", "*");
        }

        override fun addOutput(data: ByteArray?, offset: Int, length: Int) {
            if (!isCancelled() && data != null) {
                val s = String(data, offset, length, "UTF-8")
                response.write("data:" + s)
            }
        }

        override fun flush() {
            response.end();
        }

        override fun isCancelled(): Boolean {
            return false
        }
    }

    override fun start() {
        routes.noMatch {
            it?.response()?.sendFile("web/" + java.io.File(it?.path().toString()));
        }
        vertx?.createHttpServer()?.requestHandler(routes)?.listen(8081, "localhost");
    }
}