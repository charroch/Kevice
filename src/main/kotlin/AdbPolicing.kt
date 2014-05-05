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

fun IDevice.log(): String {
    return this.pp()
}

fun IDevice.toJSON(): DBObject? {
    val device = BasicDBObject("serial", this.getSerialNumber()).append("state", this.getState().toString())
    if (this.arePropertiesSet()) {
        device?.append("properties", this.getProperties()?.toJSON());
    }
    return device
}

fun IDevice.pp(): String {
    val buf = StringBuffer(this.getSerialNumber() as String).append('\t');
    if (this.arePropertiesSet()) {
        buf.append(this.getProperty("ro.product.brand"))
                .append('\t')
                .append(this.getProperty("ro.build.characteristics"))
                .append('\t')
                .append(this.getProperty("ro.build.version.release"))
                .append('\t')
                .append(this.getProperty("ro.build.version.sdk"))
    }
    return buf.toString()
}

fun Map<String, String>.toJSON(): JSONObject {
    return this.entrySet().fold(JSONObject(), {(acc, e) ->
        val jsonObj = toJSON(e.key, e.value)
        merge(acc, jsonObj)
    })
}

fun toJSON(s: String, value: Any): JSONObject {
    fun internalToJson(keys: List<String>, json: JSONObject): JSONObject {
        return when (keys.size) {
            1 -> {
                json.put(keys.first(), value)
                json
            }
            else -> {
                json.put(keys.first(), internalToJson(keys.tail, JSONObject()))
                json
            }
        }
    }
    val keys = s.split('.')
    return internalToJson(keys.toList(), JSONObject())
}

fun merge(j1: JSONObject, j2: JSONObject): JSONObject {
    return j1.entrySet().fold(JSONValue.parse(j2.toJSONString()) as JSONObject, {(acc, entry) ->
        if (!acc.containsKey(entry.key)) {
            acc.put(entry.key, entry.value)
        } else {
            val value = acc.get(entry.key)
            if (value is JSONObject) {
                if (entry.value is JSONObject)
                    acc.put(entry.key, merge(value, entry.value  as JSONObject))
            }
        }
        acc
    })
}
