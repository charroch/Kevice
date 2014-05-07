import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice

public trait  WithADB {

    fun adb(): AndroidDebugBridge {
        AndroidDebugBridge.initIfNeeded(false)
        return AndroidDebugBridge.createBridge()!!
    }

    fun devices(): List<IDevice> {
        return adb().getDevices()!!.toList()
    }

    fun device(serial: String): IDevice {
        return devices().first { d -> d.getSerialNumber() == serial }
    }
}