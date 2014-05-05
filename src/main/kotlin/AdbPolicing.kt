/**
 * Created by acsia on 09/04/14.
 */

package adb

import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener
import com.android.ddmlib.IDevice
import com.mongodb.Mongo
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import org.json.simple.JSONObject
import org.json.simple.JSONValue
import org.slf4j.LoggerFactory
import kotlin.properties.Delegates
import com.mongodb.DBCollection
import com.android.ddmlib.AndroidDebugBridge
import java.io.BufferedReader
import java.io.InputStreamReader

fun main(args: Array<String>) {
        println("Starting adb device policing")
        AndroidDebugBridge.initIfNeeded(false)
        AndroidDebugBridge.createBridge()
        AndroidDebugBridge.addDeviceChangeListener(DeviceChangeListener())
        println ("press any key to exit")
        val a = BufferedReader(InputStreamReader(System.`in`));
        a.readLine()
}

class DeviceChangeListener : IDeviceChangeListener {

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
        log?.debug("disconnected:\t\t" + device?.pp())
        val n = BasicDBObject("state", device?.getState().toString())
        devices?.update(BasicDBObject().append(
                "serial", device?.getSerialNumber()
        ), BasicDBObject().append(
                "\$set", device?.toJSON()
        ), true, true);
    }

    override fun deviceChanged(device: IDevice?, p1: Int) {
        log?.debug("changed:\t\t" + device?.pp())
        devices?.update(BasicDBObject().append(
                "serial", device?.getSerialNumber()
        ), BasicDBObject().append(
                "\$set", device?.toJSON()
        ), true, false);
    }

    override fun deviceConnected(device: IDevice?) {
        log?.debug("connected:\t\t" + device?.pp())
        devices?.update(BasicDBObject().append(
                "serial", device?.getSerialNumber()
        ), BasicDBObject().append(
                "\$set", device?.toJSON()
        ), true, false);
    }
}
