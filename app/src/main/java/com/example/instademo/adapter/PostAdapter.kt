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
        val post = postList.get(position)


        // Ensure post.uid is valid
        if (post.uid!!.isNotEmpty()) {
            // Log the UID for debugging


            // Fetch user details from Firestore
            Firebase.firestore.collection(USER_NODE).document(post.uid!!).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject<User>()
                        if (user != null) {
                            // Load user profile image
                            Glide.with(context).load(user.imageurl)
                                .placeholder(R.drawable.icon)
                                .into(holder.binding.imageViewProfile)
                            // Set username
                            holder.binding.textViewUsername.text = user.name
                        } else {
                            // Handle case where user object is null
                            Log.w("PostAdapter", "User object is null for UID: ${post.uid}")
                            holder.binding.textViewUsername.text = context.getString(R.string.unknown_user)
                            Glide.with(context).load(R.drawable.icon).into(holder.binding.imageViewProfile)
                        }
                    } else {
                        // Handle case where user document is not found
                        Log.w("PostAdapter", "User document not found for UID: ${post.uid}")
                        holder.binding.textViewUsername.text = context.getString(R.string.unknown_user)
                        Glide.with(context).load(R.drawable.icon).into(holder.binding.imageViewProfile)
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle any errors
                    Log.e("PostAdapter", "Error fetching user details for UID: ${post.uid}", exception)
                    holder.binding.textViewUsername.text = context.getString("error_loading_user".toInt())
                    Glide.with(context).load(R.drawable.icon).into(holder.binding.imageViewProfile)
                }
        } else {
            // Handle case where post.uid is invalid
            Log.w("PostAdapter", "Invalid UID for post: $post")
            holder.binding.textViewUsername.text = context.getString(R.string.unknown_user)
            Glide.with(context).load(R.drawable.icon).into(holder.binding.imageViewProfile)
        }

        // Load post image and set caption
        Glide.with(context).load(post.post)
            .placeholder(R.drawable.placeholder)
            .into(holder.binding.imageViewPost)
        holder.binding.textViewCaption.text = post.caption
    }
}
