package json

import org.spek.Spek
import java.util.HashMap
import java.util.ArrayList
import kotlin.test.assertEquals
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ContainerNode
import com.fasterxml.jackson.databind.node
import org.vertx.java.core.json.JsonObject
import org.fest.assertions.Assertions
import org.fest.assertions.Assertions.*


class JsonBuilderSpec : Spek() {

    fun String.j(): JsonNode {
        return ObjectMapper().readTree(this)!!
    }

    {
        given("a simple builder") {
            on("Mapping a key to a value", {
                val j = json {

                    obj {
                        "k" to "v"
                    }
                }
                assertEquals(j.json(), """ {"k": "v"} """.j())
            })

            on("Mapping 2 key/value pair") {

                assertEquals(
                        json {
                            obj {
                                "k" to "v"
                                "k2" to "v2"
                            }
                        }.json()

                        , """ {"k": "v", "k2": "v2"} """.j())
            }
        }


    }
}