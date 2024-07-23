package com.example.instademo.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instademo.adapter.SearchAdapter
import com.example.instademo.databinding.FragmentSearchBinding
import com.example.instademo.model.User
import com.example.instademo.utils.USER_NODE
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: SearchAdapter
    private var searchList: ArrayList<User> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        binding.rvsearch.layoutManager = LinearLayoutManager(requireContext())
        adapter = SearchAdapter(requireContext(), searchList)
        binding.rvsearch.adapter = adapter

        Firebase.firestore.collection(USER_NODE).get().addOnSuccessListener {
            val temp = ArrayList<User>()
            for (document in it.documents) {
                if (document.id == Firebase.auth.currentUser?.uid) {
                    continue
                }

                val user = document.toObject<User>()
                if (user != null) {
                    temp.add(user)
                }
            }
            searchList.addAll(temp)
            adapter.notifyDataSetChanged()
        }

        binding.searchButton.setOnClickListener {
            val text = binding.search.text.toString()
            Firebase.firestore.collection(USER_NODE).whereEqualTo("name", text).get().addOnSuccessListener {
                val temp = ArrayList<User>()
                searchList.clear()
                if (!it.isEmpty) {
                    for (document in it.documents) {
                        if (document.id == Firebase.auth.currentUser?.uid) {
                            continue
                        }

                        val user = document.toObject<User>()
                        if (user != null) {
                            temp.add(user)
                        }
                    }
                    searchList.addAll(temp)
                    adapter.notifyDataSetChanged()
                }
            }
        }

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = SearchFragment()
    }
}
