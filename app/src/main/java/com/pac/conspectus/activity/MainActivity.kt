package com.pac.conspectus.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.pac.conspectus.R
import com.pac.conspectus.fragment.ConspectusFragment
import com.pac.conspectus.fragment.DatesFragment
import com.pac.conspectus.fragment.HistoryFragment
import com.pac.conspectus.fragment.SelectFragment
import com.pac.conspectus.tool.Alert
import com.pac.conspectus.tool.FileManager
import com.pac.conspectus.tool.Storage
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupUI()
        requestPermissions()
        initTools()
    }

    private fun setupUI() {
        setDefaultFragment()
        bottom_navigation.setOnNavigationItemSelectedListener {
            navigationListener(it)
        }
    }

    private fun requestPermissions() {
        val necessaryPermissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
        )
        val permission =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        //if permissions are denied ask the user to grant permissions
        if (permission != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, necessaryPermissions, 1)
    }

    private fun initTools() {
        FileManager.init(this)
        Storage.init(this)
    }

    private fun setDefaultFragment() {
        //set select fragment as default
        supportFragmentManager.beginTransaction().replace(R.id.cell, SelectFragment()).commit()
    }

    private fun navigationListener(item: MenuItem): Boolean {
        val manager = supportFragmentManager
        //set fragment selection processing
        when (item.itemId) {
            R.id.fragment_select -> {
                manager.beginTransaction().replace(R.id.cell, SelectFragment()).commit()
                return true
            }
            R.id.fragment_history -> {
                manager.beginTransaction().replace(R.id.cell, HistoryFragment()).commit()
                return true
            }
            R.id.fragment_dates -> {
                manager.beginTransaction().replace(R.id.cell, DatesFragment()).commit()
                return true
            }
            R.id.fragment_conspectus -> {
                manager.beginTransaction().replace(R.id.cell, ConspectusFragment()).commit()
                return true
            }
            else -> return false
        }
    }
}