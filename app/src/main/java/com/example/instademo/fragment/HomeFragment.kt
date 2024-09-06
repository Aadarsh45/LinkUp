package com.example.instademo.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instademo.Aboutus
import com.example.instademo.R
import com.example.instademo.adapter.FollowRvAdapter
import com.example.instademo.adapter.PostAdapter
import com.example.instademo.databinding.FragmentHomeBinding
import com.example.instademo.model.Post
import com.example.instademo.model.User
import com.example.instademo.utils.FOLLOW
import com.example.instademo.utils.POST
import com.example.instademo.utils.USER_NODE
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val postList = ArrayList<Post>()
    private lateinit var postAdapter: PostAdapter
    private val followList = ArrayList<User>()
    private lateinit var followRvAdapter: FollowRvAdapter

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.option_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.aboutUs -> {
                val intent = Intent(requireContext(), Aboutus::class.java)
                startActivity(intent)
                true
            }
            R.id.chat -> {
                // Handle chat item click
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        // Initialize RecyclerView for posts
        postAdapter = PostAdapter(requireContext(), postList)
        binding.rv9.layoutManager = LinearLayoutManager(requireContext())
        binding.rv9.adapter = postAdapter


        // Initialize RecyclerView for follows
        followRvAdapter = FollowRvAdapter(requireContext(), followList)
        binding.followRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.followRv.adapter = followRvAdapter

        // Set Toolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.materialToolbar12)

        FirebaseFirestore.getInstance().collection(USER_NODE).document(com.google.firebase.Firebase.auth.currentUser!!.uid).get()
            .addOnCompleteListener{
                val user:User=it.result.toObject(User::class.java)!!

                if(!user.imageurl.isNullOrEmpty()){
                    Picasso.get().load(user.imageurl).into(binding.userImage)
                }
            }

        // Fetch follows and posts
        fetchFollows()
        fetchPosts()

        return binding.root
    }

    private fun fetchFollows() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            // Fetch the user's profile image URL
            Firebase.firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val user = document.toObject<User>()
                        val profileImageUrl = user?.imageurl
                        if (!profileImageUrl.isNullOrEmpty()) {
                            // Load the profile image using Picasso
                            Picasso.get().load(profileImageUrl).into(binding.userImage)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error fetching user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }

            // Fetch follows
            Firebase.firestore.collection(currentUser.uid + FOLLOW).get()
                .addOnSuccessListener { documents ->
                    followList.clear()
                    val tempList = ArrayList<User>()
                    for (document in documents.documents) {
                        val user = document.toObject<User>()!!

                        tempList.add(user)
                    }
                    followList.addAll(tempList)
                    followRvAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error fetching follows: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }


    private fun fetchPosts() {
        val currentTime = System.currentTimeMillis()

        Firebase.firestore.collection(POST)
            .get()
            .addOnSuccessListener { documents ->
                postList.clear()
                for (document in documents) {
                    val post = document.toObject<Post>()

                    // Check if the post is scheduled for a future time
                    if (post.scheduledTime == null || post.scheduledTime!! <= currentTime) {
                        postList.add(post)
                    }
                }
                postAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error fetching posts: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }



}
