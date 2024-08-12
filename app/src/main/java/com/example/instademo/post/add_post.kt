package com.example.instademo.post

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.instademo.HomeActivity
import com.example.instademo.databinding.ActivityAddPostBinding
import com.example.instademo.model.Post
import com.example.instademo.model.User
import com.example.instademo.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class add_post : AppCompatActivity() {
    private lateinit var binding: ActivityAddPostBinding  // View binding for accessing views

    private var Url: String? = null  // Variable to store the URL of the uploaded image
    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        // Handle the result of image selection
        uri?.let {
            uploadImage(it, POST_FOLDER) { url ->
                // Upload the image and get the URL
                if (url != null) {
                    binding.imageSelect.setImageURI(uri)  // Display the selected image
                    Url = url  // Store the URL
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddPostBinding.inflate(layoutInflater)  // Inflate the layout
        setContentView(binding.root)  // Set the content view to the binding's root

        setSupportActionBar(binding.toolbar4)  // Set the custom toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)  // Enable the back button
        supportActionBar?.setDisplayShowHomeEnabled(true)  // Show the home button

        // Set the navigation click listener to go back to HomeActivity
        binding.toolbar4.setNavigationOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        // Set the click listener for the image selection button
        binding.imageSelect.setOnClickListener {
            launcher.launch("image/*")  // Launch the image selection intent
        }

        // Set the click listener for the post button
        binding.post.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid  // Get the current user ID
            if (userId != null) {
                // Retrieve the user details from Firestore
                FirebaseFirestore.getInstance().collection(USER_NODE).document(userId).get()
                    .addOnSuccessListener { document ->
                        val user = document.toObject<User>()  // Convert document to User object
                        if (user != null && Url != null) {
                            // Create a new post object
                            val postId = FirebaseFirestore.getInstance().collection(POST).document().id
                            val post = Post(
                                Url!!,
                                binding.caption.text.toString(),
                                user.imageurl.toString(),
                                System.currentTimeMillis().toString(),
                                userId,
                                0,
                                mutableListOf(),
                                postId
                            )

                            // Save the post to Firestore
                            FirebaseFirestore.getInstance().collection(POST).document(postId).set(post)
                                .addOnSuccessListener {
                                    // Save the post under the user's collection
                                    FirebaseFirestore.getInstance().collection(userId).document(postId).set(post)
                                        .addOnSuccessListener {
                                            startActivity(Intent(this, HomeActivity::class.java))
                                            finish()  // Finish the current activity
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

        // Set the click listener for the cancel button
        binding.cancel.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()  // Finish the current activity
        }





        //Firestore Root
        //├── Users (Collection)
        //│   ├── {userId} (Document)
        //│   │   ├── name: String
        //│   │   ├── imageurl: String
        //│   │   ├── email: String
        //│   │   └── other user details...
        //│   └── ...
        //├── Posts (Collection)
        //│   ├── {postId} (Document)
        //│   │   ├── imageUrl: String
        //│   │   ├── caption: String
        //│   │   ├── userImageUrl: String
        //│   │   ├── timestamp: String
        //│   │   ├── userId: String
        //│   │   ├── likes: Int
        //│   │   ├── likedBy: List<String> (List of userIds who liked the post)
        //│   │   ├── postId: String
        //│   └── ...
        //├── {userId} (Collection for user's own posts)
        //│   ├── {postId} (Document)
        //│   │   ├── (Post data similar to Posts collection)
        //│   └── ...
    }
}
