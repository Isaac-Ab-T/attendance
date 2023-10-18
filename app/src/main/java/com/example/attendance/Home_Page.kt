package com.example.attendance

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import io.ktor.client.*
import io.ktor.client.engine.android.Android
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.math.BigInteger
import java.net.InetAddress

class Home_Page : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private val httpClient = HttpClient(Android) {
        install(JsonFeature)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        val logout=findViewById<Button>(R.id.logoutButton)
        val connecttoServer=findViewById<Button>(R.id.clientSendBtn)
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val textValue = sharedPreferences.getString("name", " ") // Provide the key and a default value

        findViewById<TextView>(R.id.txt).setText("Welcome "+textValue)
        connecttoServer.setOnClickListener {
            connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            val linkProperties = connectivityManager.getLinkProperties(connectivityManager.activeNetwork)
            if(networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ==true) {
                Log.d("Wifi Turned ON","Already on")
//                connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback(){
//                    @Override
//                    public void onAvailable(Network network) {
//                        Variables.isNetworkConnected = true; // Global Static Variable
//                    }
//                    @Override
//                    public void onLost(Network network) {
//                        Variables.isNetworkConnected = false; // Global Static Variable
//                    }
//                }

//                val networkRequest = NetworkRequest.Builder()
//                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
//                    .build()
//
//                connectivityManager.registerNetworkCallback(networkRequest,object:ConnectivityManager.NetworkCallback(){
//                    override fun onAvailable(network: Network) {
//                        super.onAvailable(network)
//                        Log.d("Connected to WIFI","Correct SHIT")
//                    }
//
//                })

                }

            else
            {
                Log.d("Wifi Not Turned ON",networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI).toString())
                Toast.makeText(this,"Not Connected To Teachers WIFI",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            lateinit var serverUrl:String
            val inputEditText = EditText(this)
            if(linkProperties!=null) {
                Log.d("Routes1",linkProperties?.getRoutes().toString())
                val arr=linkProperties.getRoutes().toList()
                Log.d("Last Route",arr.get(arr.lastIndex).toString())
                inputEditText.setText(arr.get(arr.lastIndex).gateway.toString())
            }
            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Enter IP Address of Teacher's Webpage")
                .setView(inputEditText)
                .setPositiveButton("OK") { dialog, which ->
                    serverUrl = "http://"+inputEditText.text.toString()+":8080/rno"
                    sendingInfo(serverUrl)
                }
                .setNegativeButton("Cancel"){dialog, which ->
                    dialog.dismiss()
                }
                .create()

            alertDialog.show()
//            val serverUrl = "http://192.168.100.59:8080/rno" // Replace with your server URL



        }
        firebaseAuth=FirebaseAuth.getInstance()

        logout.setOnClickListener {
            firebaseAuth.signOut()
            val intent= Intent(this,SignUp::class.java)
            startActivity(intent)
        }

    }
    private fun getHotspotAddress():String
    {
        val wm = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val longIp = wm.connectionInfo.ipAddress.toLong()
        val byteIp = BigInteger.valueOf(longIp).toByteArray().reversedArray()
        val strIp = InetAddress.getByAddress(byteIp).hostAddress
        return strIp.toString()
    }
    private fun sendingInfo(serverUrl:String)
    {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val MyRno = sharedPreferences.getString("rno", "NONE")

        runBlocking(Dispatchers.IO) {
            try {

                val postContent =MyRno.toString()
                Log.d("Before Sending",MyRno.toString())
                val response: Map<String, String> = httpClient.post(serverUrl) {
                    body = postContent
                }
                val message = response["receivedMessage"]
                Log.d("Server Response", message.toString())
                if(message.toString().startsWith("Marked Attendance for"))
                {
                    Log.d("MARKED ATTENDACE",message.toString())
                    Toast.makeText(applicationContext,message.toString(),Toast.LENGTH_LONG).show()
                }
                else
                {
                    Log.d("ATTENDANCE NOT MARKED",message.toString() )
                }
            } catch (e: Exception) {
                Log.d("Server Error", e.message.toString()+" "+serverUrl)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        httpClient.close()
    }


}