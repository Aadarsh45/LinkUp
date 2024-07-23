
package com.example.instademo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.instademo.R
import com.example.instademo.databinding.ReelDgBinding
import com.example.instademo.model.Reel
import com.squareup.picasso.Picasso

class ReelAdapter(var context: Context, var reelList:ArrayList<Reel>) :
    RecyclerView.Adapter<ReelAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ReelDgBinding) : RecyclerView.ViewHolder(binding.root) {
        // You can add view references and binding logic here if needed
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ReelDgBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Picasso.get().load(reelList.get(position).profile).placeholder(R.drawable.ic_person).into(holder.binding.profileImage)

        holder.binding.tv1.setText(reelList.get(position).caption)
        holder.binding.videoView.setVideoPath(reelList.get(position).videoUrl)

        holder.binding.videoView.setOnPreparedListener{
            holder.binding.progressBar.visibility = View.GONE
            holder.binding.videoView.start()

        }
    }

    override fun getItemCount(): Int {
        return reelList.size
    }
}