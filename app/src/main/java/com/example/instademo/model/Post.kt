package com.example.instademo.model

class Post{
    var post:String?=""
    var caption:String?=""
    var profileImageUrl: String?=""
    var uid: String?=""
//    var isLiked: Boolean=false
    var time:String?=""

    var likesCount: Int = 0
    var likedBy: MutableList<String> = mutableListOf()

    constructor()
    constructor(post:String,caption:String){
        this.post=post
        this.caption=caption
    }
    constructor(post: String
    , caption: String, profileImageUrl: String, time: String, uid: String, likesCount: Int, likedBy: MutableList<String>) {
        this.post = post
        this.caption = caption
        this.profileImageUrl = profileImageUrl
        this.uid = uid
        this.time = time
        this.likesCount = likesCount
        this.likedBy = likedBy
    }

}