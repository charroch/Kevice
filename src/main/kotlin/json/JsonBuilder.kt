package json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node
import com.fasterxml.jackson.databind.ObjectMapper
import org.vertx.java.core.buffer.Buffer

trait Value {
    public fun render(rootNode: ObjectNode)
}

class StringValue(val w: String, val s: String) : Value {
    override fun render(rootNode: ObjectNode) {
        rootNode.put(w, s)
    }
}

public class JsArray : Value {
    override fun render(rootNode: ObjectNode) {
        // throw UnsupportedOperationException()
    }
}

public class JsObject(val obj: ObjectNode) : Value {

    override public fun render(rootNode: node.ObjectNode) {
        if (rootNode.isContainerNode()) {
            rootNode.putAll(obj as ObjectNode)
        }
    }

    fun String.to(other: String) {
        obj.put(this, other)
    }
}

trait Template {
    fun render(buffer: Buffer)
}

class JSON {

    object JSON {
        val mapper = ObjectMapper()
    }

    val root = JSON.mapper.createObjectNode()!!

    fun obj(init: JsObject.() -> Unit) {
        val o = JsObject(JSON.mapper.createObjectNode()!!);
        o.init()
        o.render(root)
    }

    fun arr(init: JsArray.() -> Unit) {
        val a = JsArray()
        a.init()
        a.render(root)
    }


    override fun toString(): String {
        return root.toString()
    }

    fun json(): JsonNode {
        return root;
    }
}

fun json(init: JSON.() -> Unit): JSON {
    val json = JSON()
    json.init()
    return json
}