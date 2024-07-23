package com.example.instademo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.instademo.databinding.ActivitySignInBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class signIn : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
//        FirebaseApp.initializeApp(this@signIn)
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Delay the login check by 2 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            if (auth.currentUser != null) {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
        }, 2000)

        binding.noAccount.setOnClickListener {
            val intent = Intent(this, signUp::class.java)
            startActivity(intent)
        }
        binding.login.setOnClickListener {
            val email:String = binding.email.text.toString()
            val password:String = binding.password.text.toString()
            val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"

            if(email.equals("") or password.equals("") ){
                Toast.makeText(this,"please fill the attributes correctly",Toast.LENGTH_LONG).show()
            }
            else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "user logged in", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "user not logged in", Toast.LENGTH_LONG).show()
                    }
                }

            }
        }
    }
}