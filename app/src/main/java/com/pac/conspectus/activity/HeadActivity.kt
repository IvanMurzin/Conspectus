package com.pac.conspectus.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.MobileAds
import com.pac.conspectus.R
import com.pac.conspectus.tool.Registrar
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class HeadActivity : AppCompatActivity() {

    private val BOOT_TIME: Long = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_head)
        //must init for registration usage
        Registrar.init(this)
        //start RegistrationActivity or MainActivity after the boot time expires
        doAsync {
            Thread.sleep(BOOT_TIME)
            uiThread {
                if (Registrar.isUserRegistered())
                //start MainActivity with registered user
                    startActivity(Intent(this@HeadActivity, MainActivity::class.java))
                else
                //start RegistrationActivity for user registration
                    startActivity(Intent(this@HeadActivity, RegistrationActivity::class.java))
                finish()
            }
        }
    }
}