package com.example.instademo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.instademo.adapter.ReelAdapter
import com.example.instademo.databinding.FragmentReelBinding
import com.example.instademo.model.Reel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase


class ReelFragment : Fragment() {

    private var _binding: FragmentReelBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ReelAdapter
    private val reelList = ArrayList<Reel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ReelAdapter(requireContext(), reelList)
        binding.viewPager1.orientation = ViewPager2.ORIENTATION_VERTICAL
        binding.viewPager1.adapter = adapter

        fetchReels()
    }

    private fun fetchReels() {
        Firebase.firestore.collection(REEL_COLLECTION).get()
            .addOnSuccessListener { documents ->
                val tempList = ArrayList<Reel>()
                reelList.clear()
                for (document in documents) {
                    val reel = document.toObject<Reel>()
                    tempList.add(reel)
                }
                reelList.addAll(tempList)
                reelList.reverse()
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load reels", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val REEL_COLLECTION = "reels"
    }
}
