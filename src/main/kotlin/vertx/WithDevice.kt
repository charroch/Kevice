package vertx

import java.io.File

public trait WithDevice {

    fun serial(): String

    fun install(apk: File) {
        //device("dewe").installPackage()
    }
}