package adb

import com.android.ddmlib.IDevice
import org.json.simple.JSONObject
import org.json.simple.JSONValue
import org.vertx.java.core.json.JsonObject
import java.io.File
import org.vertx.java.core.AsyncResult

public class Device(device: IDevice) : IDevice by device {
    fun install(apk: File): AsyncResult<Void> {
        class InstallPackageAsync : AsyncResult<Void> {
            var t: Throwable? = null;
            var succeeded = false
            {
                try {
                    this@Device.installPackage(apk.canonicalPath, true)
                    succeeded = true
                } catch(e: Exception) {
                    t = e;
                }
            }
            override fun cause(): Throwable? = t
            override fun succeeded(): Boolean = succeeded
            override fun failed(): Boolean = !succeeded()
            override fun result(): Void? = Void.TYPE.newInstance()
        }
        return InstallPackageAsync()
    }
}

fun IDevice.log(): String {
    return this.pp()
}

public fun IDevice.asJsonObject(): JsonObject {
    return JsonObject(this.toJSON().toJSONString())
}

public fun IDevice.toJSON(): JSONObject {
    val json = JSONObject()
    json.put("serial", this.getSerialNumber())
    json.put("state", this.getState().toString())
    if (this.arePropertiesSet()) {
        json.put("properties", this.getProperties()?.toJSON());
    }
    return json
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
