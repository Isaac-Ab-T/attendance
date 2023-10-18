package com.example.attendance

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.FirebaseApp
class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    override fun onResume() {
        super.onResume()
        firebaseAuth=FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser
        if (user != null) {
            finish()
            val intent=Intent(this,Home_Page::class.java)
            startActivity(intent)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)
        firebaseAuth = FirebaseAuth.getInstance()
        val loginButton = findViewById<Button>(R.id.loginButton)
        val signButton=findViewById<Button>(R.id.signButton)
        signButton.setOnClickListener {
            val intent= Intent(this,SignUp::class.java)
            startActivity(intent)
        }
        loginButton.setOnClickListener {
            val email = (findViewById<EditText>(R.id.emailEditText)).text.toString().trim()
            val pwd = (findViewById<EditText>(R.id.pwdEditText)).text.toString().trim()
            if(email.isNotEmpty()&&pwd.isNotEmpty())
            {
                Log.d("String SENT",email+pwd)

                firebaseAuth.signInWithEmailAndPassword(email,pwd).addOnCompleteListener {
                 if(it.isSuccessful)
                 {
                     val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                     val editor = sharedPreferences.edit()
                     val db=FirebaseFirestore.getInstance()
                     Log.d("Logged","Done")
                     db.collection("students").whereEqualTo("Email",email)
                         .get()
                         .addOnSuccessListener { querySnapshot ->
                             if (!querySnapshot.isEmpty) {
                                 val document = querySnapshot.documents[0]
                                 editor.putString("rno", document.getString("Roll Number"))
                                 editor.putString("dept", document.getString("Dept"))
                                 editor.putString("name",  document.getString("Name") )
                                 editor.apply()
                                 Log.d("Firebase","Done")
                                 val intent = Intent(this, Home_Page::class.java)
                                 finish()
                                 startActivity(intent)
                             }

                         }
                         .addOnFailureListener{
                             Log.d("Verification Error",it.toString())
                             Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
                         }
                         .addOnCanceledListener {
                             Log.d("Canceled","Midway")
                         }
                         .addOnCompleteListener{
                             if (it.isSuccessful)
                             {
                                 Log.d("Completed and Successfule","Just if on Complete Listener")
                             }
                             else{
                                 Log.d("Nahc","Not Succesfful but completed")
                             }
                             Log.d("Completed","Done")
                         }

                 }
                 else
                 {
                     Log.d("Verification Error",it.exception.toString())
                     Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                 }
             }

            }
            else
            {
                Toast.makeText(this, "Empty Fields", Toast.LENGTH_SHORT).show()
            }
        }


    }
}