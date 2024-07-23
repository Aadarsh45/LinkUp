package com.example.instademo.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.instademo.R
import com.example.instademo.adapter.ViewPagerAdapter
import com.example.instademo.databinding.FragmentMyPostBinding
import com.example.instademo.databinding.FragmentProfileBinding
import com.example.instademo.model.User
import com.example.instademo.signUp
import com.example.instademo.utils.USER_NODE
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso


class ProfileFragment : Fragment() {
   private lateinit var binding: FragmentProfileBinding
   private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.editProfile.setOnClickListener{
            val intent = Intent(activity, signUp::class.java)
            intent.putExtra("MODE","EDIT")
            activity?.startActivity(intent)
        }
        viewPagerAdapter=ViewPagerAdapter(requireActivity().supportFragmentManager)
        viewPagerAdapter.addFragment(myPostFragment(),"My Post")
        viewPagerAdapter.addFragment(myReelsFragment(),"My Reels")
        binding.viewPager.adapter = viewPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        return binding.root
    }

    companion object {

    }
    override fun onStart() {
        super.onStart()
             FirebaseFirestore.getInstance().collection(USER_NODE).document(Firebase.auth.currentUser!!.uid).get()
                 .addOnCompleteListener{
                     val user:User=it.result.toObject(User::class.java)!!
                     binding.name.text=user.name
                     binding.bio.text = user.email
                     if(!user.imageurl.isNullOrEmpty()){
                         Picasso.get().load(user.imageurl).into(binding.profileImage)
                     }
                 }
    }
}