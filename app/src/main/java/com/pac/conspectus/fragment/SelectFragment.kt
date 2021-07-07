package com.pac.conspectus.fragment

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.*
import com.pac.conspectus.R
import com.pac.conspectus.tool.*
import kotlinx.android.synthetic.main.alert_dialog.*
import kotlinx.android.synthetic.main.fragment_select.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.longToast
import org.jetbrains.anko.uiThread
import kotlin.system.exitProcess

class SelectFragment : Fragment() {

    private val SELECT_PHOTO_RESULT: Int = 1
    private val SELECT_FILE_RESULT: Int = 2
    private val TAKE_PHOTO_RESULT: Int = 3
    private val photos = ArrayList<Bitmap>()
    private lateinit var photoURI: Uri
    private lateinit var loading: Dialog
    private lateinit var interstitialAd: InterstitialAd

    @SuppressLint("InflateParams")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_select, null)
        //init loading alert
        loading = Alert.getLoading(activity ?: return null)
        //init ads
        MobileAds.initialize(activity ?: return null)
        interstitialAd = InterstitialAd(activity)
        interstitialAd.adUnitId = getString(R.string.ads_id)
        interstitialAd.loadAd(AdRequest.Builder().build())
        interstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                interstitialAd.loadAd(AdRequest.Builder().build())
            }
        }

        //init buttons
        view.select_photo.setOnClickListener { selectPhoto() }
        view.select_file.setOnClickListener { selectFile() }
        view.take_photo.setOnClickListener { takePhoto() }
        view.show_menu.setOnClickListener { showMenu(it) }
        return view
    }

    private fun selectPhoto() {
        val selectPhotoIntent = FileManager.getSelectPhotoIntent()
        startActivityForResult(selectPhotoIntent, SELECT_PHOTO_RESULT)
        loading.show()
    }

    private fun selectFile() {
        val selectPhotoIntent = FileManager.getSelectFileIntent()
        startActivityForResult(selectPhotoIntent, SELECT_FILE_RESULT)
        loading.show()
    }

    private fun takePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //set uri for photo from camera
        photoURI = FileManager.getTempImageUri(activity ?: return)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        startActivityForResult(takePictureIntent, TAKE_PHOTO_RESULT)
        loading.show()
    }

    private fun showMenu(item: View) {
        val popupMenu = PopupMenu(activity, item)
        popupMenu.inflate(R.menu.menu_options)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.make_backup -> {
                    Storage.makeBackup()
                }
                R.id.upload_backup -> {
                    Storage.uploadBackup()
                }
                R.id.sign_out -> {
                    Registrar.signOut(activity ?: return@setOnMenuItemClickListener true)
                }
                R.id.exit -> {
                    activity?.finishAffinity()
                    exitProcess(0)
                }
            }
            true
        }
        popupMenu.show()
    }

    private fun showAds() {
        if (interstitialAd.isLoaded) interstitialAd.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK)
            when (requestCode) {
                SELECT_PHOTO_RESULT -> {
                    processSelectPhotoResult(data)
                }
                SELECT_FILE_RESULT -> {
                    processSelectFileResult(data)
                }
                TAKE_PHOTO_RESULT -> {
                    processTakePhotoResult()
                }
            }
        else loading.dismiss()
    }

    private fun processSelectPhotoResult(data: Intent?) {
        val bitmaps = FileManager.getImageBitmaps(activity ?: return, data)
        showAds()
        doAsync {
            val res = Requester.ocrBitmaps(bitmaps)
            uiThread {
                if (res == null) activity?.longToast(getString(R.string.check_connection))
                else Storage.saveText(res)
                loading.dismiss()
            }
        }
    }

    private fun processSelectFileResult(data: Intent?) {
        showAds()
        val res = FileManager.getTextFromFile(activity ?: return, data?.data?: return)
        if (res == null) activity?.longToast(getString(R.string.wrong_path))
        else Storage.saveText(res)
        loading.dismiss()
    }

    private fun processTakePhotoResult() {
        //show take photo alert dialog
        val alert = Alert.getDialogForm(activity ?: return)
        val bitmap = FileManager.getImageBitmap(activity ?: return, photoURI)
        photos.add(bitmap)
        //delete useless file
        FileManager.deleteTempImages()
        loading.dismiss()
        //init buttons
        alert.cancel.setOnClickListener {
            alert.dismiss()
            photos.clear()
        }
        alert.yes.setOnClickListener {
            alert.dismiss()
            takePhoto()
        }
        alert.process_all.setOnClickListener {
            alert.dismiss()
            loading.show()
            showAds()
            doAsync {
                val res = Requester.ocrBitmaps(photos)
                uiThread {
                    if (res == null) activity?.longToast(getString(R.string.check_connection))
                    else Storage.saveText(res)
                    photos.clear()
                    loading.dismiss()
                }
            }
        }
        alert.text.text = getString(R.string.add_photo)
    }
}