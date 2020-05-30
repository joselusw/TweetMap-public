package com.jsw.tweetmap.model

import twitter4j.GeoLocation

data class Tweet(val id: Long, val profileImg: String, val user: String, val name: String, val created: String,
                 val text: String, val geoLocation: GeoLocation, val favorites: Int, val retweets: Int)