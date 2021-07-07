package com.pac.conspectus.tool

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import com.aspose.words.Document
import com.pac.conspectus.BuildConfig
import kotlinx.android.synthetic.main.fragment_select.*
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

object FileManager {

    private val tempImages: MutableList<File> = mutableListOf()
    private lateinit var pathToTempDir: String

    fun init(activity: Activity) {
        //path to local dir with pictures
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath
        val directory = File("$storageDir")
        pathToTempDir = directory.absolutePath
    }

    fun getSelectPhotoIntent(): Intent {
        val selectPhotoIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        //open media with content type image
        selectPhotoIntent.type = "image/*"
        selectPhotoIntent.addCategory(Intent.CATEGORY_OPENABLE)
        //allow multiple selecting images
        selectPhotoIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        return selectPhotoIntent
    }

    fun getSelectFileIntent(): Intent {
        //corresponds .doc .docx .txt files
        val mimeTypes = arrayOf(
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
        )
        val selectFileIntent = Intent(Intent.ACTION_GET_CONTENT)
        //open file explorer
        selectFileIntent.type = "*/*"
        selectFileIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        return selectFileIntent
    }

    fun getTempImageUri(context: Context): Uri {
        val fileName = "${pathToTempDir}/${System.currentTimeMillis()}.jpg"
        val file = File(fileName)
        tempImages.add(file)
        return FileProvider.getUriForFile(
            context,
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )
    }

    fun deleteTempImages() {
        for (image in tempImages) image.delete()
    }

    fun getImageBitmap(activity: Activity, uri: Uri): Bitmap {
        /*
        *  I do not know how it really works,
        *  but the image from the gallery
        *  can only be obtained using the assignment
        *  of a uri image and getting a bitmap from there
         */
        val imageView = activity.image
        imageView.setImageURI(uri)
        return imageView.drawable.toBitmap()
    }

    fun getImageBitmaps(activity: Activity, data: Intent?): List<Bitmap> {
        val imageBitmaps = mutableListOf<Bitmap>()
        val imageUris = mutableListOf<Uri>()
        if (data?.clipData != null) {
            //selected >1 photo
            val count = data.clipData!!.itemCount
            for (i in 0 until count)
                imageUris.add(data.clipData!!.getItemAt(i).uri)

        } else if (data?.data != null) {
            //selected 1 photo
            imageUris.add(data.data!!)
        }
        for (uri in imageUris) imageBitmaps.add(getImageBitmap(activity, uri))
        return imageBitmaps
    }

    fun getTextFromFile(context: Context, uri: Uri): String {
        //check uri format to avoid null
        val extension =
            if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
                //if scheme is a content
                val mime = MimeTypeMap.getSingleton()
                mime.getExtensionFromMimeType(context.contentResolver.getType(uri))
            } else {
                //if scheme is a File
                //this will replace white spaces with %20 and also other special characters
                //this will avoid returning null values on file name with spaces and special characters
                MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path!!)).toString())
            }

        if (extension == "txt") {
            //read txt file with bufferedReader
            //get input stream from uri
            val inputStream = context.contentResolver.openInputStream(uri)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            var text = ""
            do {
                line = bufferedReader.readLine()
                text += line
            } while (line != null)
            bufferedReader.close()
            return text
        } else {
            //read docx file with aspose
            //get input stream from uri
            val inputStream = context.contentResolver.openInputStream(uri)
            //local directory with documents
            val storageDirPath =
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath
            //create temp file
            val tempFile = File("$storageDirPath/temp$extension")
            tempFile.outputStream().use { inputStream?.copyTo(it) }
            //use aspose words
            val document = Document("$storageDirPath/temp$extension")
            tempFile.delete()
            //first 79 chars is the information about aspose
            return document.text.substring(79)
        }
    }

}

