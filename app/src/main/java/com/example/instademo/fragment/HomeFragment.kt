package com.example.instademo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instademo.R
import com.example.instademo.adapter.FollowRvAdapter
import com.example.instademo.adapter.PostAdapter
import com.example.instademo.databinding.FragmentHomeBinding
import com.example.instademo.model.Post
import com.example.instademo.model.User
import com.example.instademo.utils.FOLLOW
import com.example.instademo.utils.POST
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private var postList = ArrayList<Post>()
    private lateinit var adapter: PostAdapter
    private lateinit var followRvAdapter: FollowRvAdapter
    private var followList = ArrayList<User>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)

        // Initialize RecyclerView
        adapter = PostAdapter(requireContext(), postList)
        binding.rv9.layoutManager = LinearLayoutManager(requireContext())
        binding.rv9.adapter = adapter

        fetchPosts()
        followRvAdapter = FollowRvAdapter(requireContext(),followList)
        binding.followRv.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        binding.followRv.adapter = followRvAdapter


        // Set Toolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.materialToolbar12)

        Firebase.firestore.collection(Firebase.auth.currentUser!!.uid+ FOLLOW).get()
            .addOnSuccessListener {
                var tempList = ArrayList<User>()
                for(i in it.documents){
                    var user : User = i.toObject<User>()!!
                    tempList.add(user)

                }
                followList.addAll(tempList)
                followRvAdapter.notifyDataSetChanged()
            }

        // Fetch posts from Firestore


        return binding.root
    }

    private fun fetchPosts() {
        Firebase.firestore.collection(POST).get()
            .addOnSuccessListener { documents ->
                postList.clear()
                for (document in documents) {
                    val post = document.toObject(Post::class.java)
                    postList.add(post)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error fetching posts: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.option_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
}
