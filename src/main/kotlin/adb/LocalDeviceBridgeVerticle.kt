package adb

import org.vertx.java.platform.Verticle
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener
import com.android.ddmlib.IDevice
import com.android.ddmlib.AndroidDebugBridge

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
        AndroidDebugBridge.addDeviceChangeListener(DeviceChangeListener())
    }

    fun address(): String {
        val host = container?.config()?.getString("clientname") ?: "london"
        return "devices." + host
    }
}