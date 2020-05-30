package com.jsw.tweetmap.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.jsw.tweetmap.model.Tweet
import com.jsw.tweetmap.repository.TweetRepository

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private var repository: TweetRepository = TweetRepository(application)
    private lateinit var tweets: LiveData<ArrayList<Tweet>>

    /**
     * Get the tweet whose ID is the desired ID
     */
    fun getTweetByID(id: Long?): Tweet? {
        return tweets.value?.find { it.id == id }
    }

    /**
     * Get the tweet list from the repository.
     * @param text: Filter tweets which contain this word
     * @param delay: Delay between data retrieval
     */
    fun getTweets(text: String, delay: Int): LiveData<ArrayList<Tweet>> {
        tweets = repository.getTweets(text, delay)
        return tweets
    }

    /**
     * This function stops the automatic data retrieval
     */
    fun stopSearch() {
        repository.stopSearch()
    }
}
