package vertx

import java.io.File
import org.vertx.java.core.http.HttpServerRequest
import org.vertx.java.core.Handler
import com.android.ddmlib.IDevice

public trait WithDevice: WithADB, RESTx {

    //fun serial(): String

    fun install(s:String, apk: File) {
        device(s).installPackage(apk.canonicalPath, true)
    }

    fun put(route: String, routeHandler: (HttpServerRequest, IDevice) -> Unit) {
        super<RESTx>.put(route) {
            req ->
                val serial = req?.params()?.get("serial") ?: "unknonwn"
                routeHandler(req!!,  device(serial))
        }
    }

}