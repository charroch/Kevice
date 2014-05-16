package json

import org.spek.Spek
import org.vertx.java.core.json.JsonArray

class JsonSpec : Spek() {{

    fun String.ja(): JsonArray = JsonArray(this)

    fun JsonArray.merge(other: JsonArray): JsonArray {
        return this
    }

    given("2 simple jsons array") {
        val json1: JsonArray = """[{ "k1": "v"}]""".ja()
        val json2: JsonArray = """[{ "k2": "v"}]""".ja()
        on("merging them") {
            val result = json1.merge(json2)
            //assert(json1.merge(json2).toString().contains(json1.g), result.toString() + " does not contain " + json1 )
        }
    }
}
}