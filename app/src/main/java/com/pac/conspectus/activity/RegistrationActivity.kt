package com.pac.conspectus.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pac.conspectus.R
import com.pac.conspectus.tool.Registrar
import kotlinx.android.synthetic.main.activity_registration.*
import org.jetbrains.anko.longToast

class RegistrationActivity : AppCompatActivity() {

    private val GOOGLE_SIGN_IN: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        setupUI()
    }

    private fun setupUI() {
        google_button.setOnClickListener {
            startActivityForResult(Registrar.getGoogleSignInIntent(), GOOGLE_SIGN_IN)
        }
        signUp.setOnClickListener {
            Registrar.signUpWithFirebase(this, readFields())
        }
        signIn.setOnClickListener {
            Registrar.signInWithFireBase(this, readFields())
        }
    }

    private fun readFields(): Pair<String, String>? {
        val email = email.text.toString()
        val password = password.text.toString()
        //check the correctness of the data
        //check that user email is correct using regexp
        if (!email.contains(Regex("^\\w(\\w|-)+@\\w\\w+\\.\\w\\w+")))
            longToast(R.string.email_error)
        //check password length that should be not less 6 chars
        else if (password.length < 6)
            longToast(R.string.password_error)
        //return correct fields
        else return Pair(email, password)
        //if fields are incorrect return null
        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN)
            Registrar.firebaseAuthWithGoogle(this, data)
    }
}