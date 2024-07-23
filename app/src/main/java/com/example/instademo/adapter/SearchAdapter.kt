package com.example.instademo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instademo.R
import com.example.instademo.databinding.ItemSearchBinding
import com.example.instademo.model.User
import com.example.instademo.utils.FOLLOW
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.NonDisposableHandle.parent

class SearchAdapter (
    var context: Context,
    var UserList:ArrayList<User>
    ) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root) {
        // You can add view references and binding logic here if needed
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var binding = ItemSearchBinding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return UserList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var isfollow = false
        Glide.with(context).load(UserList.get(position).imageurl).placeholder(R.drawable.ic_person).into(holder.binding.userImage)
            holder.binding.profilename.text = UserList.get(position).name

        Firebase.firestore.collection(Firebase.auth.currentUser!!.uid+ FOLLOW)
            .whereEqualTo("email",UserList.get(position).email).get().addOnSuccessListener {
                if(it.documents.size == 0){
                    isfollow = false
                }
                else{
                    holder.binding.follow.text = "Unfollow"
                    isfollow = true
                }
            }

        holder.binding.follow.setOnClickListener{
            if(isfollow){
                Firebase.firestore.collection(Firebase.auth.currentUser!!.uid+ FOLLOW)
                    .whereEqualTo("email",UserList.get(position).email).get().addOnSuccessListener {
                        Firebase.firestore.collection(Firebase.auth.currentUser!!.uid+ FOLLOW).document(it.documents.get(0).id).delete()
                        holder.binding.follow.text = "follow"
                        isfollow = true
                    }

            }
            else {
                Firebase.firestore.collection(Firebase.auth.currentUser!!.uid + FOLLOW).document()
                    .set(UserList.get(position))

                holder.binding.follow.text = "Unfollow"
                isfollow = true
            }
        }

    }

}