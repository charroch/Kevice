/**
 * Created by acsia on 09/04/14.
 */
import org.spek.Spek
import org.json.simple.JSONValue
import org.json.simple.JSONObject
import kotlin.test.assertEquals


fun Map<String, Object>.toJSON(): JSONObject {
    return this.entrySet().fold(JSONObject(), {(acc, e) ->
        val jsonObj = toJSON(e.key, e.value)
        merge(acc, jsonObj)
    })
}

fun toJSON(s: String, value: Object): JSONObject {
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
            if(value is JSONObject) {
                acc.put(entry.key, merge(value, entry.value as JSONObject))
            }
        }
        acc
    })
}


class SeveralStringToJsonSpec : Spek() {{

    given("the simple key simple and the value: 1") {
        val toJson = "simple"
        val value = 1
        on("making it into JSON") {
            val expected = JSONValue.parse("""{"simple":  1}""") as JSONObject;
            assertEquals(expected.toJSONString(), toJSON(toJson, value as Object).toJSONString())
        }
    }

    given("""the string simple.test.hello and the value: 1""") {
        val toJson = "simple.test.hello"
        val value = 1
        on("making it into JSON") {
            val expected = JSONValue.parse("""{"simple": { "test": { "hello": 1}}}""") as JSONObject;
            assertEquals(expected.toJSONString(), toJSON(toJson, value as Object).toJSONString())
        }
    }

    given("2 nearly identical JSONObject") {
        val json1 = JSONValue.parse("""{"simple": { "test": { "hello": 1}}}""") as JSONObject;
        val json2 = JSONValue.parse("""{"simple": { "test": { "world": 1}}}""") as JSONObject;
        on("merging them together") {
            val expected = JSONValue.parse("""{"simple": { "test": { "hello": 1, "world": 1}}}""") as JSONObject;
            assertEquals(expected.toJSONString(), merge(json1, json2).toJSONString())
        }
    }

    given("2 completely different JSONObject") {
        val json1 = JSONValue.parse("""{"simple": { "test": { "hello": 1}}}""") as JSONObject;
        val json2 = JSONValue.parse("""{"hard": { "test": { "world": 1}}}""") as JSONObject;
        on("merging them together") {
            val expected = JSONValue.parse("""
            {"simple": { "test": { "hello": 1}}, "hard": { "test": { "world": 1}}}
            """) as JSONObject;
            assertEquals(expected.toJSONString(), merge(json1, json2).toJSONString())
        }
    }

}
}

class DeviceAsJsonSpec : Spek() {{
    given("A Map of key value") {
        val m = mapOf<String, Object>(
                "simple.one" to "value1" as Object,
                "simple.second" to "value2" as Object,
                "simple.third" to "value3" as Object
        )
        on("reading first key") {
            it("should parse the key with single value") {
                val obj = JSONValue.parse("""
                                 {"simple": { "one": "value1", "second": "value2", "third": "value3"}}
                """) as JSONObject;
                assertEquals(obj.toJSONString(), m.toJSON().toJSONString())
            }
        }
    }
}
}