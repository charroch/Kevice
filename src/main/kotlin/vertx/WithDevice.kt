package vertx

import java.io.File
import org.vertx.java.core.http.HttpServerRequest
import adb.Device
import com.fasterxml.jackson.databind.JsonNode
import json.JSON

public trait WithDevice : WithADB, RESTx, RouteHelper {

    fun install(s: String, apk: File) {
        device(s).installPackage(apk.canonicalPath, true)
    }

    fun put(route: String, routeHandler: (HttpServerRequest, Device) -> Unit) {
        put(route) { req ->
            val serial = req?.params()?.get("serial") ?: "unknown"
            try {
                routeHandler(req!!, device(serial))
            } catch(e: IllegalArgumentException) {
                req?.response()?.setStatusCode(404)?.setStatusMessage("device %s not found" format serial)?.end()
            }
        }
    }

    fun <T> get(route: String, routeHandler: (HttpServerRequest, Device) -> T) {
        get(route) { req ->
            val serial = req?.params()?.get("serial") ?: "unknown"
            try {
                val t = routeHandler(req!!, device(serial))
                when (t) {
                    is JSON -> req.response()?.headers()?.add("Content-Type", "application/json")
                }
                req.response()?.putHeader("Content-length" as String, Integer.toString(t.toString().length))
                req.response()?.write(t.toString())?.end()

            } catch(e: IllegalArgumentException) {
                req?.response()?.setStatusCode(404)?.setStatusMessage("device %s not found" format serial)?.end()
            }
        }
    }

    fun <T> delete(route: String, routeHandler: (HttpServerRequest, Device, String) -> T) {
        delete(route) { req ->
            val serial = req?.params()?.get("serial") ?: "unknown"
            val pkg = req?.params()?.get("package")
            try {
               routeHandler(req!!, device(serial), pkg!!)
            } catch(e: IllegalArgumentException) {
                req?.response()?.setStatusCode(404)?.setStatusMessage("device %s not found" format serial)?.end()
            }
        }
    }

    fun <T> post(route: String, routeHandler: (HttpServerRequest, Device) -> T) {
        post(route) { req ->
            val serial = req?.params()?.get("serial") ?: "unknown"
            try {
                routeHandler(req!!, device(serial))
            } catch(e: IllegalArgumentException) {
                req?.response()?.setStatusCode(404)?.setStatusMessage("device %s not found" format serial)?.end()
            }
        }
    }

}