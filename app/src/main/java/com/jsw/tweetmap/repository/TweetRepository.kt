package com.jsw.tweetmap.repository

import android.app.Application
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jsw.tweetmap.model.Tweet
import kotlinx.coroutines.*
import twitter4j.*
import twitter4j.conf.ConfigurationBuilder
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class TweetRepository(application: Application) {
    private var allTweets: MutableLiveData<ArrayList<Tweet>>
    private var context: Context
    private var date: String = ""
    private var doRefreshing: Boolean = false
    private val ioScope = CoroutineScope(Dispatchers.IO + Job())
    private var maxID: Long = 0
    private var twitter: Twitter

    init {
        //Initialize LiveData
        allTweets = MutableLiveData<ArrayList<Tweet>>()

        //Initialize vars
        context = application.applicationContext
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        date = sdf.format(Date(System.currentTimeMillis()))

        //Initialize twitter
        val cb = ConfigurationBuilder()
        cb.setDebugEnabled(true)
            .setOAuthConsumerKey("")
            .setOAuthConsumerSecret("")
            .setOAuthAccessToken("")
            .setOAuthAccessTokenSecret("")
            .setTweetModeExtended(true)
        val tf = TwitterFactory(cb.build())
        twitter = tf.instance
    }


    /**
     * This function retrieves tweets which contains a selected text.
     * Furthermore, the delay between attemps is user-defined.
     * The list is cleared before every sync request.
     * To avoid a huge amount of results, the first execution requests today's tweets.
     * Then we only retrieve tweets whose ID is bigger than current max.
     */
    fun getTweets(text: String, delay: Int): LiveData<ArrayList<Tweet>> {
        this.doRefreshing = true
        ioScope.launch {
            while (doRefreshing) {
                val query = Query(text)
                val values: ArrayList<Tweet> = ArrayList()
                allTweets.postValue(null)

                if (maxID == 0L)
                    query.since = date
                else
                    query.sinceId = maxID

                try {
                    val result = twitter.search(query)
                    maxID = result.maxId

                    for (status in result.tweets) {
                        if (status.geoLocation != null) {
                            values.add(createTweet(status, status.geoLocation))
                        } else if (!status.user.location.isEmpty()) {
                            val geoLocation = getLocationFromAddress(context, status.user.location)
                            if (geoLocation != null) {
                                values.add(createTweet(status, geoLocation))
                            }
                        }
                    }
                    //Notify
                    allTweets.value?.clear()
                    allTweets.postValue(values)
                    Thread.sleep(delay * 1000L)
                } catch (ex: Exception) {
                    Log.e("EXCEPTION", ex.message)
                }
            }
        }.start()

        return allTweets
    }

    /**
     * This function returns the geolocation for a location string.
     * It is executed if the geolocation of the tweet is null.
     */
    fun getLocationFromAddress(context: Context, strAddress: String?): GeoLocation? {
        val coder = Geocoder(context)
        val address: List<Address>
        var res: GeoLocation? = null

        try {
            address = coder.getFromLocationName(strAddress, 1)

            if (address.isNotEmpty())
                res = GeoLocation(address.get(0).latitude, address.get(0).longitude)
        } catch (ex: IOException) {
            Log.e("TweetRepository :: ", ex.message)
        }

        return res
    }

    /**
     * This function is called when the stop button is pressed.
     * It stops the automatic sync and clears the tweet list.
     */
    fun stopSearch() {
        this.doRefreshing = false
        this.allTweets.postValue(null)
    }

    /**
     * This function created a Tweet model object.
     */
    private fun createTweet(status: Status, geoLocation: GeoLocation): Tweet {
        return Tweet(
            status.id,
            status.user.originalProfileImageURLHttps,
            status.user.screenName,
            status.user.name,
            status.createdAt.toString(),
            status.retweetedStatus?.text ?: status.text,
            geoLocation,
            status.retweetedStatus?.favoriteCount ?: status.favoriteCount,
            status.retweetedStatus?.retweetCount ?: status.retweetCount
        )
    }
}