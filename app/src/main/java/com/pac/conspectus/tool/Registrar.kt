package com.pac.conspectus.tool

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log.e
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.pac.conspectus.R
import com.pac.conspectus.activity.MainActivity
import com.pac.conspectus.activity.RegistrationActivity
import org.jetbrains.anko.longToast

object Registrar {

    private lateinit var googleSignOut: Task<Void>
    private lateinit var googleSignInIntent: Intent

    fun init(context: Activity) {
        val googleSignInClient = GoogleSignIn.getClient(
            context, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )
        googleSignOut = googleSignInClient.signOut()
        googleSignInIntent = googleSignInClient.signInIntent
    }

    fun isUserRegistered(): Boolean = FirebaseAuth.getInstance().currentUser != null

    fun getGoogleSignInIntent(): Intent = googleSignInIntent

    fun getUserName(): String =
        //firebase does not support dots in the path
        FirebaseAuth.getInstance().currentUser?.email?.replace(".", "") ?: "default"

    fun signOut(activity: Activity) {
        FirebaseAuth.getInstance().signOut()
        googleSignOut
        //start RegistrationActivity for user registration
        activity.startActivity(Intent(activity, RegistrationActivity::class.java))
        activity.finish()
    }

    fun signUpWithFirebase(activity: Activity, fields: Pair<String, String>?) {
        if (fields == null) return
        val email = fields.first
        val password = fields.second
        //ask firebase to create user using email and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) {
                if (it.isSuccessful) {
                    //start MainActivity with registered user
                    activity.startActivity(Intent(activity, MainActivity::class.java))
                    activity.finish()
                } else {
                    //check email already in use exception
                    if (it.exception?.message?.startsWith("The email address is already in use") == true)
                        activity.longToast(R.string.email_already_use)
                    else {
                        //catch another exception
                        e("MyLogger", it.exception?.message.toString())
                        activity.longToast(R.string.sign_in_fail)
                    }
                }
            }
    }

    fun signInWithFireBase(activity: Activity, fields: Pair<String, String>?) {
        if (fields == null) return
        val email = fields.first
        val password = fields.second
        //ask firebase to sign in user using email and password
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) {
                if (it.isSuccessful) {
                    //start MainActivity with registered user
                    activity.startActivity(Intent(activity, MainActivity::class.java))
                    activity.finish()
                } else {
                    when {
                        //check no such user exception
                        it.exception?.message?.startsWith("There is no user record corresponding") == true ->
                            activity.longToast(R.string.no_such_user)
                        //check wrong password exception
                        it.exception?.message?.startsWith("The password is invalid") == true ->
                            activity.longToast(R.string.password_wrong)
                        else -> {
                            //catch another exception
                            e("MyLogger", it.exception?.message.toString())
                            activity.longToast(R.string.sign_in_fail)
                        }
                    }
                }
            }
    }

    fun firebaseAuthWithGoogle(activity: Activity, data: Intent?) {
        val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
            //ask firebase to sign in using google
            FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                if (it.isSuccessful) {
                    //start MainActivity with registered user
                    activity.startActivity(Intent(activity, MainActivity::class.java))
                    activity.finish()
                } else {
                    e("MyLogger", it.exception?.message.toString())
                    activity.longToast(R.string.sign_in_fail)
                }
            }
        } catch (e: ApiException) {
            e("MyLogger", e.message.toString())
            activity.longToast(R.string.sign_in_fail)
        }
    }
}