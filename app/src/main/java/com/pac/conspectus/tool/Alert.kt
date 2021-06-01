package com.pac.conspectus.tool

import android.app.Activity
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.pac.conspectus.R
import kotlinx.android.synthetic.main.loading.*
import kotlin.math.roundToInt

object Alert {

    fun getDialogForm(activity: Activity): AlertDialog {
        //set my layout for alert dialog
        val view = LayoutInflater.from(activity).inflate(R.layout.alert_dialog, null)
        val builder = AlertDialog.Builder(activity).setView(view)
        val alert = builder.show()
        val width = activity.resources.displayMetrics.widthPixels
        //set height of dialog about 60% of parent size
        val height = (activity.resources.displayMetrics.heightPixels * 0.6).roundToInt()
        //init dialog size
        alert.window?.setLayout(width, height)
        //doesn't close when touch outside dialog or back button
        alert.setCancelable(false)
        alert.setCanceledOnTouchOutside(false)
        return alert
    }

    fun getLoading(activity: Activity): AlertDialog {
        //set my layout for loading alert
        val view = LayoutInflater.from(activity).inflate(R.layout.loading, null)
        val builder = AlertDialog.Builder(activity).setView(view)
        val alert = builder.show()
        //set loading gif
        alert.loadingGif.setMovieResource(R.drawable.gif)
        alert.loadingGif.setMovie(alert.loadingGif.getMovie())
        //doesn't close when touch outside dialog or back button
        alert.setCancelable(false)
        alert.setCanceledOnTouchOutside(false)
        //hide dialog
        alert.dismiss()
        return alert
    }

    fun getChangeParagraphForm(activity: Activity): AlertDialog? {
        //set my layout for loading alert
        val view = LayoutInflater.from(activity).inflate(R.layout.alert_dialog_paragraph_name, null)
        val builder = AlertDialog.Builder(activity).setView(view)
        val alert = builder.show()
        val width = activity.resources.displayMetrics.widthPixels
        //set height of dialog about 60% of parent size
        val height = (activity.resources.displayMetrics.heightPixels * 0.4).roundToInt()
        //init dialog size
        alert.window?.setLayout(width, height)
        return alert
    }
}