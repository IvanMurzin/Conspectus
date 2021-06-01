package com.pac.conspectus.tool

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import java.nio.charset.StandardCharsets

/*
*  api responds to a post request
*  which contains a picture in the base64 string
*  with a string that contains the text from this picture
 */

object Requester {

    fun ocrBitmaps(data: List<Bitmap>): String? {
        if (isNotOnline()) return null
        var result = ""
        for (bitmap in data) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val bytes = byteArrayOutputStream.toByteArray()
            //encode bytes from bitmap image to base64 string
            val base64String = Base64.encodeToString(bytes, Base64.DEFAULT)
            val postData: ByteArray = base64String.toByteArray(StandardCharsets.UTF_8)
            //create connection to api
            val connection =
                URL("https://api.wowchef.ru/ocr/").openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 10000
            connection.doOutput = true
            connection.setRequestProperty("charset", "utf-8")
            connection.setRequestProperty("Content-lenght", postData.size.toString())
            connection.setRequestProperty("Content-Type", "application/text")
            //send request
            try {
                val outputStream = DataOutputStream(connection.outputStream)
                outputStream.write(postData)
                outputStream.flush()
            } catch (exception: Exception) {
                Log.e("MyLogger", exception.message.toString())
            }
            //create response string from api response
            connection.inputStream.bufferedReader().use { response ->
                response.lines().forEach {
                    result += it
                }
            }
        }
        return result
    }

    private fun isNotOnline(): Boolean {
        return try {
            val socket = Socket()
            val socketAddress = InetSocketAddress("8.8.8.8", 53)
            socket.connect(socketAddress, 1500)
            socket.close()
            false
        } catch (e: IOException) {
            true
        }
    }
}