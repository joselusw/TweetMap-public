package com.jsw.tweetmap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jsw.tweetmap.model.Tweet
import com.jsw.tweetmap.ui.detail.DetailFragment
import com.jsw.tweetmap.ui.main.MainFragment


class MainActivity : AppCompatActivity() {

    /*** OVERRIDE FUNCTIONS ***/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow()
        }
    }


    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }


    /*** SUPPORT FUNCTIONS ***/
    fun openDetails(tweet: Tweet?) {
        if (tweet != null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, DetailFragment(tweet))
                .addToBackStack(null)
                .commit()
        }
    }
}
