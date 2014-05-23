package json

import org.spek.Spek
import java.util.HashMap
import java.util.ArrayList
import kotlin.test.assertEquals
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode

/**
 * Created by acsia on 21/05/14.
 */
class JsonBuilderSpec : Spek() {{

    trait Value {

        fun render(builder: StringBuilder)
        fun asString(): String
    }

    class StringValue(val s: String) : Value {
        override fun asString(): String {
            return s;
        }
        override fun render(builder: StringBuilder) {
            throw UnsupportedOperationException()
        }
    }

    abstract class Tag : Value {
        val children: ArrayList<Value> = ArrayList<Value>()
        val attributes = HashMap<String, String>()
    }


    object: Object() {
        {
            println(this)
        }
    }

    class Object() : Tag() {

        {
            println(this)
        }

        override fun render(builder: StringBuilder) {
            for (c in children) {
                c.render(builder)
            }
        }

        fun String.to(other: String) {
            children.add(StringValue(other))
        }

        override fun asString(): String {
            // return fields.makeString("/")
            return ""
        }
    }

    class JSON : Tag() {

        object JSON {
            val mapper = ObjectMapper()
        }

        override fun asString(): String {

            val a:ObjectNode  = JSON.mapper.createObjectNode()!!


            throw UnsupportedOperationException()
        }
        override fun render(builder: StringBuilder) {
            for (c in children) {
                c.render(builder)
            }
        }
        fun obj(m: Object.() -> Unit): Object {
            val o = Object()
            o.m()
            children.add(o)
            return o;
        }
        fun a() {
        }
    }

    fun json(init: JSON.() -> Unit): JSON {
        val json = JSON()
        json.init()
        return json
    }

    given("a simple builder") {
        val j = json {
            obj {
                "hh" to "hh"
                "hh" to "hh"
            }
        }

        val a: Object.() -> Unit = {
            "hh" to "hh"
            "hh" to "hh"
        }

        val b = Object()
        b.a()

        assertEquals(b.children.first?.asString(), "huh")

    }
}
}