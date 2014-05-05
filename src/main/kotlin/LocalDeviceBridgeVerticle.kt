/**
 * Created by acsia on 05/05/14.
 */
import org.vertx.java.platform.Verticle
import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener
import com.android.ddmlib.IDevice
import adb.toJSON
import adb.log

public class LocalDeviceBridgeVerticle : Verticle(), IDeviceChangeListener {

    override fun deviceConnected(device: IDevice?) {
        if (device != null && vertx != null && vertx!!.isEventLoop()) {
            container?.logger()?.info(device.log())
            vertx?.eventBus()?.publish(address(), device.toJSON())
        }
    }
    override fun deviceDisconnected(device: IDevice?) {
        if (device != null && vertx != null && vertx!!.isEventLoop()) {
            container?.logger()?.info(device.log())
            vertx?.eventBus()?.publish(address(), device.toJSON())
        }
    }
    override fun deviceChanged(device: IDevice?, status: Int) {
        if (device != null && vertx != null && vertx!!.isEventLoop()) {
            container?.logger()?.info(device.log())
            vertx?.eventBus()?.publish(address(), device.toJSON())
        }
    }

    override fun start() {
        AndroidDebugBridge.initIfNeeded(true)
        AndroidDebugBridge.createBridge()
        AndroidDebugBridge.addDeviceChangeListener(this)
    }

    fun address(): String {
        val host = container?.config()?.getString("clientname") ?: "london"
        return "devices." + host
    }
}