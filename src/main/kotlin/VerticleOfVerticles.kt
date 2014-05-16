import org.vertx.java.platform.Verticle
import adb.LocalDeviceBridgeVerticle
import vertx.DeviceVerticle
import org.vertx.java.core.json.JsonObject

public class VerticleOfVerticles : Verticle() {

    override fun start() {

        val config = JsonObject()
        config.putString("address", "mongodb")
        config.putString("db_name", "adb")
        config.putString("host", "oceanic.mongohq.com")
        config.putNumber("port", 10038)
        val username = "hal9000"
        val password = "monn1v1da"
        if (username != null) {
            config.putString("username", username)
            config.putString("password", password)
        }
        config.putBoolean("fake", false)
        container?.deployModule("io.vertx~mod-mongo-persistor~2.1.0", config)
        container?.logger()?.info("Starting all verticles")
        VerticleManager.allVerticles.forEach {
            when (it.vtype) {
                is VerticleType.WORKER -> container?.deployWorkerVerticle(it.vert.getName(), null, 1, false);
                is VerticleType.DEFAULT -> container?.deployVerticle(it.vert.getName())
            }
        }
    }
}

data class RunnableVerticle(val vtype: VerticleType, val vert: Class<in Verticle>)

public enum class VerticleType {
    WORKER
    DEFAULT
}

object VerticleManager {

    val allVerticles: Array<RunnableVerticle> get() {
        val adb = RunnableVerticle(
                VerticleType.WORKER,
                javaClass<LocalDeviceBridgeVerticle>() as Class<in Verticle>
        )
        val deviceRest = RunnableVerticle(
                VerticleType.DEFAULT,
                javaClass<DeviceVerticle>() as Class<in Verticle>
        )
        return array(adb, deviceRest)
    }
}