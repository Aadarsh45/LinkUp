package com.example.instademo.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instademo.R
import com.example.instademo.databinding.ItemPostBinding
import com.example.instademo.model.Post
import com.example.instademo.model.User
import com.example.instademo.utils.POST
import com.example.instademo.utils.USER_NODE
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class PostAdapter(
    private val context: Context,
    private val postList: ArrayList<Post>
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        private val likeButton: ImageView = itemView.findViewById(R.id.like_button)
        private val likesCount: TextView = itemView.findViewById(R.id.likes_count)

        fun bind(post: Post) {
            likesCount.text = "${post.likesCount} likes"
            likeButton.setImageResource(if (post.likedBy.contains(Firebase.auth.currentUser?.uid)) R.drawable.ic_liked else R.drawable.ic_like)

            likeButton.setOnClickListener {
                val currentUserId = Firebase.auth.currentUser?.uid ?: return@setOnClickListener
                val newLikesCount = if (post.likedBy.contains(currentUserId)) {
                    // User has already liked the post, remove their like
                    post.likedBy.remove(currentUserId) // This should be correct if likedBy is a MutableList
                    post.likesCount - 1
                } else {
                    // User hasn't liked the post, add their like
                    post.likedBy.add(currentUserId)
                    post.likesCount + 1
                }

                // Update Firestore
                Firebase.firestore.collection(POST).document()
                    .update("likesCount", newLikesCount, "likedBy", post.likedBy)
                    .addOnSuccessListener {
                        // Update the UI
                        likesCount.text = "$newLikesCount likes"
                        likeButton.setImageResource(if (post.likedBy.contains(currentUserId)) R.drawable.ic_liked else R.drawable.ic_like)
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(context, "Error updating likes: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = postList[position]

        if (post.uid!!.isNotEmpty()) {
            // Fetch user details from Firestore
            Firebase.firestore.collection(USER_NODE).document(post.uid!!).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject<User>()
                        user?.let {
                            // Load user profile image using Glide
                            Glide.with(context)
                                .load(user.imageurl)
                                .placeholder(R.drawable.icon) // Placeholder image
                                .error(R.drawable.ic_error) // Error image
                                .into(holder.binding.imageViewProfile)

                            // Set username
                            holder.binding.textViewUsername.text = user.name
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

        holder.bind(post)
    }

    private fun handleError(holder: ViewHolder) {
        // Set default values for username and profile image in case of errors
        holder.binding.textViewUsername.text = context.getString(R.string.unknown_user)
        Glide.with(context).load(R.drawable.icon).into(holder.binding.imageViewProfile)
    }
}
