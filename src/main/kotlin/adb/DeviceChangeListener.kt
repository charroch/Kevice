/**
 * Created by acsia on 09/04/14.
 */

package adb

import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener
import com.android.ddmlib.IDevice
import com.mongodb.Mongo
import com.mongodb.BasicDBObject
import org.slf4j.LoggerFactory
import kotlin.properties.Delegates
import com.mongodb.DBCollection

public class DeviceChangeListener : IDeviceChangeListener {

    val log = LoggerFactory.getLogger("DeviceChangeListener");

    val devices: DBCollection? by Delegates.lazy<DBCollection?> {
        val mongo = Mongo("oceanic.mongohq.com", 10038);
        val db = mongo.getDB("adb")
        db?.authenticate("hal9000", "monn1v1da".toCharArray())
        val coll = db?.getCollection("devices")
        coll?.createIndex(
                BasicDBObject().append("serial", 1),
                BasicDBObject().append("unique", true)?.append("dropDups", true)
        );
        coll
    }

    override fun deviceDisconnected(device: IDevice?) {
        if (device!!.getSerialNumber()!!.contains("?")) {
            return;
        }
        log?.debug("disconnected:\t\t" + device?.pp())
        devices?.update(BasicDBObject().append(
                "serial", device?.getSerialNumber()
        ), BasicDBObject().append(
                "\$set", device?.toJSON()
        ), true, true);
    }

    override fun deviceChanged(device: IDevice?, p1: Int) {
        if (device!!.getSerialNumber()!!.contains("?")) {
            return;
        }
        log?.debug("changed:\t\t" + device?.pp())
        devices?.update(BasicDBObject().append(
                "serial", device?.getSerialNumber()
        ), BasicDBObject().append(
                "\$set", device?.toJSON()
        ), true, false);
    }

    override fun deviceConnected(device: IDevice?) {
        if (device!!.getSerialNumber()!!.contains("?")) {
            return;
        }
        log?.debug("connected:\t\t" + device?.pp())
        devices?.update(BasicDBObject().append(
                "serial", device?.getSerialNumber()
        ), BasicDBObject().append(
                "\$set", device?.toJSON()
        ), true, false);
    }
}
