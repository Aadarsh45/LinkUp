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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class PostAdapter(
    private val context: Context,
    private val postList: ArrayList<Post>
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private val firestore = Firebase.firestore
    private val userCollection = firestore.collection(USER_NODE)
    private val postCollection = firestore.collection(POST)
    private val postListeners = mutableMapOf<String, ListenerRegistration>()

    inner class ViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        private val likeButton: ImageView = binding.likeButton
        private val likesCount: TextView = binding.likesCount

        fun bind(post: Post) {
            updateLikeUI(post)

            likeButton.setOnClickListener {
                val currentUserId = Firebase.auth.currentUser?.uid ?: return@setOnClickListener
                val newLikesCount: Int
                if (post.likedBy.contains(currentUserId)) {
                    // User has already liked the post, remove their like
                    post.likedBy.remove(currentUserId)
                    newLikesCount = post.likesCount - 1
                } else {
                    // User hasn't liked the post, add their like
                    post.likedBy.add(currentUserId)
                    newLikesCount = post.likesCount + 1
                }

                // Update Firestore
                val postRef = postCollection.document(post.id!!)
                firestore.runTransaction { transaction ->
                    transaction.update(postRef, "likesCount", newLikesCount)
                    transaction.update(postRef, "likedBy", post.likedBy)
                }.addOnSuccessListener {
                    // No need to update UI here as snapshot listener will handle it
                }.addOnFailureListener { exception ->
                    Toast.makeText(context, "Error updating likes: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun updateLikeUI(post: Post) {
            likesCount.text = "${post.likesCount} likes"
            likeButton.setImageResource(if (post.likedBy.contains(Firebase.auth.currentUser?.uid)) R.drawable.ic_liked else R.drawable.ic_like)
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

        // Attach a Firestore listener for real-time updates
        postListeners[post.id!!] = postCollection.document(post.id!!).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("PostAdapter", "Error listening to post changes", error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val updatedPost = snapshot.toObject<Post>()
                updatedPost?.let {
                    postList[position] = it // Update postList with the new data
                    holder.bind(it) // Bind updated data to the view holder
                }
            }
        }

        // Fetch user details from Firestore
        if (post.uid!!.isNotEmpty()) {
            userCollection.document(post.uid!!).get()
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
                            handleError(holder)
                        }
                    } else {
                        handleError(holder)
                    }
                }
                .addOnFailureListener { exception ->
                    handleError(holder)
                    Log.e("PostAdapter", "Error fetching user details for UID: ${post.uid}", exception)
                }
        } else {
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


//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val post = postList[position]
//
//        // Attach a Firestore listener for real-time updates
//        postListeners[post.id!!] = postCollection.document(post.id!!).addSnapshotListener { snapshot, error ->
//            if (error != null) {
//                Log.e("PostAdapter", "Error listening to post changes", error)
//                return@addSnapshotListener
//            }
//
//            if (snapshot != null && snapshot.exists()) {
//                val updatedPost = snapshot.toObject<Post>()
//                updatedPost?.let {
//                    postList[position] = it // Update postList with the new data
//                    holder.bind(it) // Bind updated data to the view holder
//                }
//            }
//        }
//
//        // Fetch user details from Firestore
//        if (post.uid!!.isNotEmpty()) {
//            userCollection.document(post.uid!!).get()
//                .addOnSuccessListener { documentSnapshot ->
//                    if (documentSnapshot.exists()) {
//                        val user = documentSnapshot.toObject<User>()
//                        user?.let {
//                            // Load user profile image using Glide
//                            Glide.with(context)
//                                .load(user.imageurl)
//                                .placeholder(R.drawable.icon) // Placeholder image
//                                .error(R.drawable.ic_error) // Error image
//                                .into(holder.binding.imageViewProfile)
//
//                            // Set username
//                            holder.binding.textViewUsername.text = user.name
//                        } ?: run {
//                            handleError(holder)
//                        }
//                    } else {
//                        handleError(holder)
//                    }
//                }
//                .addOnFailureListener { exception ->
//                    handleError(holder)
//                    Log.e("PostAdapter", "Error fetching user details for UID: ${post.uid}", exception)
//                }
//        } else {
//            handleError(holder)
//        }
//
//        // Load post image and set caption using Glide
//        Glide.with(context)
//            .load(post.post)
//            .placeholder(R.drawable.placeholder) // Placeholder image
//            .error(R.drawable.ic_error) // Error image
//            .into(holder.binding.imageViewPost)
//        holder.binding.textViewCaption.text = post.caption
//
//        holder.bind(post)
//    }

    private fun handleError(holder: ViewHolder) {
        holder.binding.textViewUsername.text = context.getString(R.string.unknown_user)
        Glide.with(context).load(R.drawable.icon).into(holder.binding.imageViewProfile)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        // Clean up listeners when the adapter is detached
        for (listener in postListeners.values) {
            listener.remove()
        }
    }
}
