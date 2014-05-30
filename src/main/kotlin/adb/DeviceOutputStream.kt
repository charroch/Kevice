package adb

import com.android.ddmlib.IShellOutputReceiver
import org.vertx.java.core.Handler
import org.vertx.java.core.buffer.Buffer
import org.vertx.java.core.streams.ReadStream
import java.util.concurrent.atomic.AtomicBoolean

class DeviceOutputStream : IShellOutputReceiver, ReadStream<DeviceOutputStream> {

    val paused: AtomicBoolean = AtomicBoolean(false)
    var handler: Handler<Buffer>? = null
    var exceptionHandler: Handler<Throwable>? = null

    override fun exceptionHandler(handler: Handler<Throwable>?): DeviceOutputStream? {
        exceptionHandler = handler
        return this
    }

    override fun dataHandler(handler: Handler<Buffer>?): DeviceOutputStream? {
        this.handler = handler;
        return this
    }

    override fun pause(): DeviceOutputStream? {
        paused.set(true)
        return this
    }

    override fun resume(): DeviceOutputStream? {
        paused.set(false)
        return this
    }

    override fun endHandler(endHandler: Handler<Void>?): DeviceOutputStream? {
        throw UnsupportedOperationException()
    }

    override fun addOutput(data: ByteArray?, offset: Int, length: Int) {
        if (!isCancelled() && data != null && handler != null) {
            val s = String(data, offset, length, "UTF-8")
            val b = Buffer(s)
            handler?.handle(b)
        }
    }

    override fun flush() {
        pause();
    }

    override fun isCancelled(): Boolean {
        return paused.get()
    }
}
