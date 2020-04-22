package com.rrdsolutions.paleodelightsrider

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    lateinit var vm: LoginViewModel
    var username = ""
    var password = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        FirebaseApp.initializeApp(this)
        vm = ViewModelProvider(this).get(LoginViewModel::class.java)

        checkLogin()

        loginbutton.setOnClickListener{

            loadingscreen.visibility = View.VISIBLE

            username = username_edt.text.toString()
            password = password_edt.text.toString()
            Log.d("_login", "username = $username, password = $password")

            vm.loginWith(username, password){ callback->
                when (callback){
                    "login success"->{
                        Log.d("_login", "login success")
                        status.text = "Login success"
                        status.setTextColor("#a4c639".toColorInt())

                        if (checkbox.isChecked) saveLogin()
                        toMainActivity()
                    }
                    "password incorrect"->{
                        Log.d("_login", "password incorrect")
                        status.text = "Password incorrect"
                        status.setTextColor("#FF0000".toColorInt())
                    }
                    "username incorrect"->{
                        Log.d("_login", "username incorrect")
                        status.text = "Username incorrect"
                        status.setTextColor("#FF0000".toColorInt())
                    }
                    "login fail"->{
                        Log.d("_login", "login fail")
                        status.text = "Login failed"
                        status.setTextColor("#FF0000".toColorInt())
                    }
                }
                loadingscreen.visibility = View.GONE
            }
        }

    }

    private fun checkLogin(){
        username = getPreferences(0).getString("username", "").toString()
        password = getPreferences(0).getString("password", "").toString()
        Log.d("_login", "username = $username, password = $password")
        if (username !="" && password !="") toMainActivity()
    }

    private fun saveLogin(){
        getPreferences(0).edit().putString("username", username).apply()
        getPreferences(0).edit().putString("password", password).apply()
        //
        val usernametest = getPreferences(0).getString("username", "").toString()
        val passwordtest = getPreferences(0).getString("password", "").toString()
        Log.d("_login", "username and password saved; " +
                "username = $usernametest, password = $passwordtest")
    }
    private fun toMainActivity(){
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("username", username)
        }
        startActivity(intent)
        Log.d("_login", "moving to MainActivity")
    }
}

class LoginViewModel: ViewModel(){
    fun loginWith(username: String, password:String, callback:(String)-> Unit){
        val db = FirebaseFirestore
            .getInstance().collection("riders")

        db.document(username).get()
            .addOnSuccessListener{it->

                if (it.exists()){
                    val realpassword = it.data?.get("password") as String
                    Log.d("_login", "realpassword = $realpassword")

                    if (realpassword == password){
                        callback("login success")
                    }
                    else{
                        callback("password incorrect")
                    }
                }
                else callback ("username incorrect")

            }
            .addOnFailureListener{
                callback("login fail")
            }
    }
}