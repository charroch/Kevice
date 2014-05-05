/**
 * Created by charroch on 05/05/2014.
 */


import com.android.ddmlib.AndroidDebugBridge
import adb.DeviceChangeListener
import java.io.BufferedReader
import java.io.InputStreamReader
import org.vertx.java.platform.impl.cli.Starter
import java.io.File

fun main(args: Array<String>) {
    println("Starting adb device policing")
    println ("press any key to exit")
    val a = BufferedReader(InputStreamReader(System.`in`));
    a.readLine()

//    Starter.main
    //(["run",           "WebserverVerticle", "-cp", "sourceSets.main.runtimeClasspath.files.join(File.pathSeparator)"])
}