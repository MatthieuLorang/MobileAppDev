package com.example.m1securedevelopment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private val PREFS_NAME = "MyPrefsFile"
    var id : String = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Restore preferences
        val settings: SharedPreferences = getSharedPreferences(PREFS_NAME, 0)
        if(settings.contains("id")){
            id = settings.getString("id", "").toString()
            if (id != "") {
                val monIntent: Intent = Intent(this, Account::class.java);
                monIntent.putExtra("id", id)
                startActivity(monIntent)
            }
        }

    }

    private val client = OkHttpClient.Builder()
            .certificatePinner(
                    CertificatePinner.Builder()
                            .add("publicobject.com", "sha256/afwiKY3RxoMmLkuRW1l7QsPZTJPwDS2pdDROQjXw8ig=")
                            .build())
            .build()

    private fun run(url: String) : String{
        val request = Request.Builder()
                .url(url)
                .build()
        var data : String = "";
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response){
                data  = response.body?.string().toString();
            }
        })
        Thread.sleep(1000);
        return data
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart(){
        super.onStart()

        var data : String = "";
        data = run(Account.baseUrlFromJNI() +"config")

        data = data.replace("{","")
        data = data.replace("}","")
        data = data.replace(":"," ")
        data = data.replace(","," ")
        data = data.replace("[","")
        data = data.replace("]","")
        data = data.replace("\"","")

        var delimiter1 = " ";
        val parts = data.split(delimiter1)


        val button : Button = findViewById(R.id.button);
        button?.setOnClickListener()
        {
            if(isNetworkAvailable()) {
                val editText: EditText = findViewById(R.id.editTextNumberPassword);
                val number : String = editText.text.toString()
                for(element in parts){
                    if(number == element){
                        id = number;

                        val settings: SharedPreferences = getSharedPreferences(PREFS_NAME, 0)
                        val editor: SharedPreferences.Editor = settings.edit()
                        editor.putString("id", id)
                        // Commit the edits!
                        editor.commit()
                    }
                }

                if (id != "") {
                    val monIntent: Intent = Intent(this, Account::class.java);
                    monIntent.putExtra("id", id)
                    startActivity(monIntent)

                } else {
                    val monIntent: Intent = Intent(this, MainActivity::class.java);
                    startActivity(monIntent)
                    val text = "Incorrect id"
                    val duration = Toast.LENGTH_SHORT
                    val toast = Toast.makeText(applicationContext, text, duration)
                    toast.show()
                }
            }
            else{
                val text = "No connection"
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(applicationContext, text, duration)
                toast.show()
            }
        }
    }
}