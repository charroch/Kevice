/**
 * Created by acsia on 05/05/14.
 */

package adb;

import org.vertx.java.platform.Verticle
import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener
import com.android.ddmlib.IDevice

public class LocalDeviceBridgeVerticle : Verticle(), IDeviceChangeListener {

    override fun deviceConnected(device: IDevice?) {
        if (device != null && vertx != null) {
            container?.logger()?.info(device.log())
            container?.logger()?.debug(device.log())
            vertx?.eventBus()?.publish(address(), device.asJsonObject())
        }
    }
    override fun deviceDisconnected(device: IDevice?) {
        if (device != null && vertx != null) {
            container?.logger()?.info(device.log())
            vertx?.eventBus()?.publish(address(), device.asJsonObject())
        }
    }
    override fun deviceChanged(device: IDevice?, status: Int) {
        if (device != null && vertx != null) {
            container?.logger()?.info(device.log())
            vertx?.eventBus()?.publish(address(), device.asJsonObject())
        }
    }

    override fun start() {
        container?.logger()?.info("Starting device monitoring VERTICLE")
        AndroidDebugBridge.initIfNeeded(true)
        AndroidDebugBridge.createBridge()
        AndroidDebugBridge.addDeviceChangeListener(this)
    }

    fun address(): String {
        val host = container?.config()?.getString("clientname") ?: "london"
        return "devices." + host
    }
}