import org.vertx.java.platform.Verticle
import org.vertx.java.core.http.HttpServerRequest
import org.vertx.java.core.Handler
import java.io.File
import org.vertx.java.core.AsyncResultHandler
import org.vertx.java.core.file.AsyncFile
import org.vertx.java.core.AsyncResult
import org.vertx.java.core.streams.Pump

public class DeviceWebVerticle : Verticle(), WithDevice {

    override fun serial(): String {
        throw UnsupportedOperationException()
    }

    override fun start() {
        vertx?.createHttpServer()?.requestHandler(apkUploadHandler(File("/tmp/apk.apk")))?.listen(8080)
    }

    fun apkUploadHandler(file: File) = Handler<HttpServerRequest> () { request ->
        request?.pause()
        vertx?.fileSystem()?.open(file.canonicalPath, AsyncResultHandler<AsyncFile>() {(a: AsyncResult<AsyncFile>?) ->
            val result = a!!
            if (result.succeeded()) {
                val file = result.result()
                val pump = Pump.createPump(request, file)
                request?.endHandler { asyncResult ->
                    file?.close(AsyncResultHandler<Void>() { ar ->
                        if (ar?.succeeded() as Boolean) {
                            request?.response()?.end();
                        } else {
                            ar?.cause()?.printStackTrace(System.err);
                        }
                    })
                }
                pump?.start()
                request?.resume()
            }
        })
    }
}