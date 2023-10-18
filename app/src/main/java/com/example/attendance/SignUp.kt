package com.example.attendance

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import android.widget.*;
import com.google.firebase.firestore.FirebaseFirestore

class SignUp : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onResume() {
        super.onResume()
        firebaseAuth=FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser
        if (user != null) {
            val intent=Intent(this,Home_Page::class.java)
            startActivity(intent)
            finish()

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        firebaseAuth=FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser
        if (user != null) {
            val intent=Intent(this,Home_Page::class.java)
            startActivity(intent)

        }
        val signUpButton = findViewById<Button>(R.id.signButton)
        val loginButton=findViewById<Button>(R.id.loginButton)
        val dept=findViewById<Spinner>(R.id.dept)
        val depts = arrayOf("Civil", "CSE", "ECE", "EEE","Mech")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, depts)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dept.adapter=adapter

        loginButton.setOnClickListener {
            val intent= Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
        signUpButton.setOnClickListener {
            val email=(findViewById<EditText>(R.id.emailEditText)).text.toString().trim()
            val pwd= (findViewById<EditText>(R.id.pwdEditText)).text.toString().trim()
            val confirmpwd=(findViewById<EditText>(R.id.repwdEditText)).text.toString().trim()
            val rno=(findViewById<EditText>(R.id.rnoEditText)).text.toString().trim()
            val name=(findViewById<EditText>(R.id.nameEditText)).text.toString().trim()
            Log.d("email", email+" "+pwd+" "+confirmpwd)
            if(email.isNotEmpty() && pwd.isNotEmpty() && confirmpwd.isNotEmpty() && rno.isNotEmpty() && name.isNotEmpty())
            {
                if(pwd == confirmpwd)
                {
                    firebaseAuth.createUserWithEmailAndPassword(email,pwd).addOnCompleteListener{
                        if(it.isSuccessful)
                        {
                            val db=FirebaseFirestore.getInstance()
                            val newd=HashMap<String,Any>()
                            newd["Roll Number"]=rno
                            newd["Dept"]=dept.selectedItem.toString().trim()
                            newd["Name"]=name
                            newd["Email"]=email
                            db.collection("students")
                                .add(newd)
                                .addOnSuccessListener {
                                    val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                                    val editor = sharedPreferences.edit()
                                    editor.putString("rno", rno)
                                    editor.putString("dept",dept.selectedItem.toString())
                                    editor.putString("name",name)

                                    editor.apply()
                                    val intent= Intent(this,Home_Page::class.java)
                                    startActivity(intent)
                                }
                                .addOnFailureListener{
                                    Log.d("Error Adding to DB",it.stackTraceToString())
                                    val tv=findViewById<TextView>(R.id.infoText)
                                    tv.setText(it.stackTraceToString())
                                }

                        }
                        else
                        {
                            Log.d("Failure",it.exception.toString())
                            val tv=findViewById<TextView>(R.id.infoText)
                            tv.setText(it.exception.toString())
                            Toast.makeText(this, "Sign Up Fails"+it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else
                {
                    Toast.makeText(this, "Not Matching Passwords", Toast.LENGTH_SHORT).show()
                }
            }
            else
            {
                Toast.makeText(this, "Fill All Fields", Toast.LENGTH_SHORT).show()
            }

        }

    }
}