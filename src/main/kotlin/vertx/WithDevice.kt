package vertx

import java.io.File
import org.vertx.java.core.http.HttpServerRequest
import adb.Device

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

}