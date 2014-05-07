
import java.io.File

public trait WithDevice: WithADB {

    fun serial(): String

    fun install(apk: File) {
        //device("dewe").installPackage()
    }
}