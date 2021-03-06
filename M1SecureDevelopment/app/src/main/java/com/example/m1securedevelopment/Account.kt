package com.example.m1securedevelopment

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.jvm.Throws


class Account : AppCompatActivity() {

    private val client = OkHttpClient.Builder()
            .certificatePinner(
                CertificatePinner.Builder()
                    .add("publicobject.com", "sha256/afwiKY3RxoMmLkuRW1l7QsPZTJPwDS2pdDROQjXw8ig=")
                    .build())
            .build()

    private val PREFS_NAME = "MyPrefsFile"
    var id : String = "";
    var name : String = "";
    var lastname : String = "";
    var amount : String = "";
    var currency : String = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
    }

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

    override fun onStart() {
        super.onStart()

        id = intent.getStringExtra("id") as String;


        var dataConfig : String = "";
        dataConfig = run(baseUrlFromJNI()+"config")

        dataConfig = dataConfig.replace("{","")
        dataConfig = dataConfig.replace("}","")
        dataConfig = dataConfig.replace(":"," ")
        dataConfig = dataConfig.replace(","," ")
        dataConfig = dataConfig.replace("[","")
        dataConfig = dataConfig.replace("]","")
        dataConfig = dataConfig.replace("\"","")

        var delimiter = " ";
        val parts1 = dataConfig.split(delimiter)

        val alis1: MutableList<String> = ArrayList()
        for(element in parts1){
            alis1.add(element)
        }

        val list1: ArrayList<String> = alis1 as ArrayList<String>;
        for(i in 0 until list1.size){
            if(id == list1[i]){
                name = list1[i+2]
                lastname = list1[i+4]
            }
        }

        var dataAccounts: String = "";
        dataAccounts = run(baseUrlFromJNI()+"accounts")

        dataAccounts = dataAccounts.replace(" ","")
        dataAccounts = dataAccounts.replace("{","")
        dataAccounts = dataAccounts.replace("}","")
        dataAccounts = dataAccounts.replace(":"," ")
        dataAccounts = dataAccounts.replace(","," ")
        dataAccounts = dataAccounts.replace("[","")
        dataAccounts = dataAccounts.replace("]","")
        dataAccounts = dataAccounts.replace("\"","")

        val parts2 = dataAccounts.split(delimiter)
        val alis2: MutableList<String> = ArrayList()
        for(element in parts2){
            alis2.add(element)
        }

        val list2: ArrayList<String> = alis2 as ArrayList<String>;
        for(i in 0 until list2.size){
            if(id == list2[i]){
                amount = list2[i+4]
                currency = list2[i+8]
            }
        }

        for(i in currency.indices){
            if(i != 0) {
                if (Character.isUpperCase(currency[i])) {
                    val string1: String = currency.substring(0, i)
                    val string2 : String = currency.substring(i,currency.length)
                    currency = "$string1 $string2";
                }
            }
        }


        val textView3 : TextView = findViewById(R.id.textView3)
        textView3.text = name;

        val textView5 : TextView = findViewById(R.id.textView5)
        textView5.text = lastname;

        val textView7 : TextView = findViewById(R.id.textView7)
        textView7.text = amount;

        val textView8 : TextView = findViewById(R.id.textView8)
        textView8.text = currency;

        val button1: Button = findViewById(R.id.button1);
        button1?.setOnClickListener()
        {
            val monIntent: Intent = Intent(this, Account::class.java);
            monIntent.putExtra("id", id)
            startActivity(monIntent)
        }

        val button2: Button = findViewById(R.id.button2);
        button2?.setOnClickListener()
        {
            val monIntent: Intent = Intent(this, MainActivity::class.java);
            startActivity(monIntent)

            val settings: SharedPreferences = getSharedPreferences(PREFS_NAME, 0)
            val editor: SharedPreferences.Editor = settings.edit()
            editor.putString("id", "")
            // Commit the edits!
            editor.commit()
        }
    }

    @Throws(IOException::class)
    private fun readStream(`is`: InputStream): String? {
        val sb = StringBuilder()
        val r = BufferedReader(InputStreamReader(`is`), 1000)
        var line: String = r.readLine()
        while (line != null) {
            sb.append(line)
            line = r.readLine()
        }
        `is`.close()
        return sb.toString()
    }

    companion object {
        init {
            System.loadLibrary("native-lib")
        }

        @JvmStatic
        external fun baseUrlFromJNI(): String

        @Volatile
        private var INSTANCE: Retrofit? = null

        fun getInstance(): Retrofit {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Retrofit.Builder()
                        .baseUrl(baseUrlFromJNI())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build().also { INSTANCE = it }
            }
        }
    }
}