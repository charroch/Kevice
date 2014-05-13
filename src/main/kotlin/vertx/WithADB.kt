package vertx

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import adb.Device

public trait  WithADB {

    fun adb(): AndroidDebugBridge {
        AndroidDebugBridge.initIfNeeded(false)
        return AndroidDebugBridge.createBridge()!!
    }

    fun devices(): List<Device> {
        return adb().getDevices()!!.toList().map { Device(it) }
    }

    fun device(serial: String): Device {
        return devices().first { d -> d.getSerialNumber() == serial }
    }
}