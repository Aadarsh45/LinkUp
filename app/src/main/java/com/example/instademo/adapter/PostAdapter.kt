package com.example.instademo.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instademo.R
import com.example.instademo.databinding.ItemPostBinding
import com.example.instademo.model.Post
import com.example.instademo.model.User


import com.example.instademo.utils.USER_NODE
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class PostAdapter(
    private val context: Context,
    private val postList: ArrayList<Post>
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = postList[position]

        // Note : post.uid is time and post.time  is uid there is a error is valid
        if (post.uid!!.isNotEmpty()) {
            // Fetch user details from Firestore
            Firebase.firestore.collection(USER_NODE).document(post.time!!).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val user= documentSnapshot.toObject<User>()
                        user?.let {
                            // Load user profile image using Glide
                            Glide.with(context)
                                .load(user.imageurl)
                                .placeholder(R.drawable.icon) // Placeholder image
                                .error(R.drawable.ic_error) // Error image
                                .into(holder.binding.imageViewProfile)

                            // Set username
                            holder.binding.textViewUsername.text =user.name
                        } ?: run {
                            // Handle case where user object is null
                            Log.w("PostAdapter", "User object is null for UID: ${post.uid}")
                            handleError(holder)
                        }
                    } else {
                        // Handle case where user document is not found
                        Log.w("PostAdapter", "User document not found for UID: ${post.uid}")
                        handleError(holder)
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle any errors during data fetching
                    Log.e("PostAdapter", "Error fetching user details for UID: ${post.uid}", exception)
                    handleError(holder)
                }
        } else {
            // Handle case where post.uid is invalid
            Log.w("PostAdapter", "Invalid UID for post: $post")
            handleError(holder)
        }

        // Load post image and set caption using Glide
        Glide.with(context)
            .load(post.post)
            .placeholder(R.drawable.placeholder) // Placeholder image
            .error(R.drawable.ic_error) // Error image
            .into(holder.binding.imageViewPost)
        holder.binding.textViewCaption.text = post.caption
    }

    private fun handleError(holder: ViewHolder) {
        // Set default values for username and profile image in case of errors
        holder.binding.textViewUsername.text = context.getString(R.string.unknown_user)
        Glide.with(context).load(R.drawable.icon).into(holder.binding.imageViewProfile)
    }
}