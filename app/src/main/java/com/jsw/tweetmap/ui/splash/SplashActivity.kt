package com.jsw.tweetmap.ui.splash

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.jsw.tweetmap.R
import android.content.Intent
import android.os.Handler
import com.jsw.tweetmap.MainActivity

class SplashActivity : AppCompatActivity() {
    /* -- VARS --*/
    private lateinit var myHandler : Handler
    private val splashTime = 2000L // 2 seconds

    /* -- FUNCTIONS --*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        myHandler = Handler()

        myHandler.postDelayed({
            goToMainActivity()
        },splashTime)
    }

    /**
     * Display the main activity and finish the current one
     */
    private fun goToMainActivity(){
        val mainActivityIntent = Intent(applicationContext, MainActivity::class.java)
        startActivity(mainActivityIntent)
        finish()
    }
}