package com.pavel.voicedo.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.orm.SugarRecord
import com.pavel.voicedo.R
import com.pavel.voicedo.models.Event
import com.pavel.voicedo.models.Product
import com.pavel.voicedo.models.ShoppingList
import com.pavel.voicedo.models.Task

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        SugarRecord.deleteAll(Product::class.java)
        SugarRecord.deleteAll(Event::class.java)
        SugarRecord.deleteAll(ShoppingList::class.java)
        SugarRecord.deleteAll(Task::class.java)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, SPLASH_TIME_OUT)
    }

    companion object {
        const val SPLASH_TIME_OUT: Long = 3000
    }
}