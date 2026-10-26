package app.neon.com.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class FilebaseStorage(context: Context) {
    private val workerUrl = "https://neont-server.soal8456.workers.dev/"

    suspend fun uploadFile(filename: String, contentType: String, inputStream: InputStream): String =
        withContext(Dispatchers.IO) {
            val conn = URL(workerUrl).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            val body = JSONObject().apply {
                put("filename", filename)
                put("contentType", contentType)
            }.toString()
            conn.outputStream.use { it.write(body.toByteArray()) }
            val response = conn.inputStream.bufferedReader().readText()
            val json = JSONObject(response)
            val presignedUrl = json.getString("url")
            val key = json.getString("key")

            val uploadConn = URL(presignedUrl).openConnection() as HttpURLConnection
            uploadConn.requestMethod = "PUT"
            uploadConn.setRequestProperty("Content-Type", contentType)
            uploadConn.doOutput = true
            uploadConn.outputStream.use { out -> inputStream.copyTo(out) }
            val code = uploadConn.responseCode
            if (code !in 200..299) throw Exception("Upload gagal, HTTP $code")
            "https://s3.filebase.io/${json.getString("key")}"
        }
}
