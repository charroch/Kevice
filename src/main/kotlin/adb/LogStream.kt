package adb

import org.vertx.java.core.Handler
import org.vertx.java.core.buffer.Buffer
import org.vertx.java.core.streams.ReadStream
import java.util.concurrent.atomic.AtomicBoolean
import com.android.ddmlib.log.LogReceiver.ILogListener
import com.android.ddmlib.log.LogReceiver
import com.android.ddmlib.log.EventLogParser
import com.android.ddmlib.log.EventContainer.EventValueType

class LogStream(val device: Device) : ILogListener, ReadStream<LogStream> {


    override fun newEntry(entry: LogReceiver.LogEntry?) {


        val els = EventLogParser()
        els.init(device)
        val a = els.parse(entry)
        if (a?.getType() == EventValueType.STRING) {
            println(" " + a?.getString())
        } else if (a?.getType() == EventValueType.LIST) {
            (0..4).forEach {
                print(":" + a?.getValue(it))
            }
            println("")
        }

        if (handler != null && entry != null) {
            val b = Buffer(entry.data)

            b.appendString("\n")
            handler?.handle(b)
        }
    }

    override fun newData(data: ByteArray?, offset: Int, length: Int) {
        // throw UnsupportedOperationException()
    }

    val paused: AtomicBoolean = AtomicBoolean(false)
    var handler: Handler<Buffer>? = null
    var exceptionHandler: Handler<Throwable>? = null

    override fun exceptionHandler(handler: Handler<Throwable>?): LogStream? {
        exceptionHandler = handler
        return this
    }

    override fun dataHandler(handler: Handler<Buffer>?): LogStream? {
        this.handler = handler;
        return this
    }

    override fun pause(): LogStream? {
        paused.set(true)
        return this
    }

    override fun resume(): LogStream? {
        paused.set(false)
        return this
    }

    override fun endHandler(endHandler: Handler<Void>?): LogStream? {
        throw UnsupportedOperationException()
    }

    //    override fun addOutput(data: ByteArray?, offset: Int, length: Int) {
    //        if (!isCancelled() && data != null && handler != null) {
    //            val s = String(data, offset, length, "UTF-8")
    //            val b = Buffer(s)
    //            handler?.handle(b)
    //        }
    //    }
    //
    //    override fun flush() {
    //        pause();
    //    }
    //
    //    override fun isCancelled(): Boolean {
    //        return paused.get()
    //    }
}
