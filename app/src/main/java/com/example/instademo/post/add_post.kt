package com.example.instademo.post

import android.content.Intent
import android.os.Build.USER
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.instademo.HomeActivity
import com.example.instademo.databinding.ActivityAddPostBinding
import com.example.instademo.model.Post
import com.example.instademo.model.User
import com.example.instademo.utils.POST
import com.example.instademo.utils.POST_FOLDER
import com.example.instademo.utils.USER_NODE
import com.example.instademo.utils.uploadImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class add_post : AppCompatActivity() {
    private lateinit var binding: ActivityAddPostBinding

    private var Url: String? = null
    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            uploadImage(it, POST_FOLDER) { url ->
                if (url != null) {
                    binding.imageSelect.setImageURI(uri)
                   Url = url
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar4)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar4.setNavigationOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        binding.imageSelect.setOnClickListener {
            launcher.launch("image/*")
        }

        binding.post.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                FirebaseFirestore.getInstance().collection(USER_NODE).document(userId).get()
                    .addOnSuccessListener { document ->
                        val user = document.toObject<User>()
                        if (user != null && Url != null) {
                            var post = Post(Url!!,binding.caption.text.toString(),user.imageurl.toString(),System.currentTimeMillis().toString(),userId.toString(),0, mutableListOf())

                            FirebaseFirestore.getInstance().collection(POST).document().set(post)
                                .addOnSuccessListener {
                                    FirebaseFirestore.getInstance().collection(userId).document().set(post)
                                        .addOnSuccessListener {
                                            startActivity(Intent(this, HomeActivity::class.java))
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            // Handle the failure
                                            e.printStackTrace()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    // Handle the failure
                                    e.printStackTrace()
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle the failure
                        e.printStackTrace()
                    }
            }
        }

        binding.cancel.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }
}
