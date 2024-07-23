package com.example.instademo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.instademo.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.instademo.model.User
import com.example.instademo.utils.USER_NODE
import com.example.instademo.utils.USER_PROFILE_FOLDER
import com.example.instademo.utils.uploadImage
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class signUp : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth

    lateinit var user: User
    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()){uri->
        uri?.let{
            uploadImage(uri, USER_PROFILE_FOLDER) {
                if(it != null){
                    user.imageurl = it
                    binding.profileImage.setImageURI(uri)
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        user = User()

        if(intent.hasExtra("MODE") and intent.getStringExtra("MODE").equals("EDIT")){
            binding.createAccount.text = "Update"

            FirebaseFirestore.getInstance().collection(USER_NODE).document(com.google.firebase.Firebase.auth.currentUser!!.uid).get()
                .addOnCompleteListener{
                    val user:User=it.result.toObject(User::class.java)!!
                    if(!user.imageurl.isNullOrEmpty()){
                        Picasso.get().load(user.imageurl).into(binding.profileImage)
                    }
                    binding.name.setText(user.name)
                    binding.email.setText(user.email)
                }

        }

        binding.alreadyUser.setOnClickListener {
            val intent = Intent(this, signIn::class.java)
            startActivity(intent)
        }

        auth = FirebaseAuth.getInstance()
        binding.addImage.setOnClickListener {
            launcher.launch("image/*")
        }

        binding.createAccount.setOnClickListener {
            if (intent.hasExtra("MODE") and intent.getStringExtra("MODE").equals("EDIT")){
                FirebaseFirestore.getInstance().collection(USER_NODE)
                    .document(user.id.toString()).set(user)

                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
            else{

                val email: String = binding.email.text.toString()
                val password: String = binding.password.text.toString()
                val name: String = binding.name.text.toString()
                val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"

                if (email.equals("") or name.equals("") or password.equals("")) {
                    Toast.makeText(this, "please fill the attributes", Toast.LENGTH_LONG).show()
                } else if (!email.matches(emailRegex.toRegex())) {
                    Toast.makeText(this, "check your email", Toast.LENGTH_LONG).show()
                } else {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                user.name = name;
                                user.email = email;
                                user.id = auth.currentUser?.uid;
                                FirebaseFirestore.getInstance().collection(USER_NODE)
                                    .document(user.id.toString()).set(user)
                                    .addOnSuccessListener {
                                        val intent = Intent(this, HomeActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }

                            } else {
                                Toast.makeText(this, "user not created", Toast.LENGTH_LONG).show()
                            }

                        }
                }


            }
        }


    }




}