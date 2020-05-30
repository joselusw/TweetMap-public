package com.jsw.tweetmap.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.jsw.tweetmap.R
import com.jsw.tweetmap.model.Tweet
import com.squareup.picasso.Picasso

class DetailFragment(val tweet: Tweet) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //Get views
        val view = inflater.inflate(R.layout.fragment_detail, container, false)
        val imageView = view.findViewById<ImageView>(R.id.iv_userImg)
        val userName = view.findViewById<TextView>(R.id.tv_userName)
        val user = view.findViewById<TextView>(R.id.tv_user)
        val tweetText = view.findViewById<TextView>(R.id.tv_tweetText)
        val favorites = view.findViewById<TextView>(R.id.tv_favorites)
        val retweets = view.findViewById<TextView>(R.id.tv_retweets)
        val date = view.findViewById<TextView>(R.id.tv_date)

        //Fulfill views
        Picasso.get().load(tweet.profileImg).into(imageView)
        userName.text = tweet.name
        user.text = String.format("%s%s", "@", tweet.user)
        tweetText.text = tweet.text
        favorites.text = tweet.favorites.toString()
        retweets.text = tweet.retweets.toString()
        date.text = tweet.created

        return view
    }
}