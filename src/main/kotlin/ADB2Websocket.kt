/**
 * Created by acsia on 30/04/14.
 */


import org.vertx.java.core.Handler
import org.vertx.java.core.http.ServerWebSocket
import org.vertx.java.core.Vertx
import java.io.IOException
import com.fasterxml.jackson.databind.node.ObjectNode
import java.util.Date
import com.fasterxml.jackson.databind.ObjectMapper
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener
import com.android.ddmlib.IDevice
import com.android.ddmlib.AndroidDebugBridge
import org.vertx.java.core.logging.Logger
import org.vertx.java.core.json.JsonObject
import org.vertx.java.core.eventbus.Message

public class WebSocketAdb(val vertx: Vertx, val logger: Logger) : Handler<ServerWebSocket> {

    override fun handle(ws: ServerWebSocket?) {
        val eventBus = vertx.eventBus()
        if (eventBus == null) return;
        val s = """{
            "action": "find",
            "collection": "devices"
        }"""

        eventBus.send("mongodb", JsonObject(s), Handler<Message<JsonObject>>() { (json) ->
            println( "hello" + json?.body());
        })


        if (ws != null && eventBus != null) {

            val chatRoom = "devices"
            val id = ws.textHandlerID() ?: "-1"
            vertx.sharedData()?.getSet<String>("chat.room." + chatRoom)?.add(id)

            ws.closeHandler { evt ->
                vertx.sharedData()?.getSet<String>("chat.room." + chatRoom)?.remove(id)
            }

            ws.dataHandler({ data ->
                val m = ObjectMapper()
                try {
                    val rootNode = m.readTree(data.toString())
                    ((rootNode as ObjectNode)).put("received", Date().toString())
                    val jsonOutput = m.writeValueAsString(rootNode)
                    for (chatter in vertx.sharedData()?.getSet<String>("chat.room." + chatRoom)?.iterator()) {
                        eventBus.send((chatter as String), jsonOutput)
                    }
                } catch (e: IOException) {
                    ws.reject()
                }
            })
        }
    }
}