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
import org.joda.time.DateTime

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        /*SugarRecord.deleteAll(Product::class.java)
        SugarRecord.deleteAll(Event::class.java)
        SugarRecord.deleteAll(ShoppingList::class.java)
        SugarRecord.deleteAll(Task::class.java)*/

        /*val products1 = arrayListOf(
            Product("Best product one", false),
            Product("Best product two", true),
            Product("Best product three", true),
            Product("Best product four", false),
            Product("Best product five", false)
        )

        val products2 = arrayListOf(
            Product("Best product six", true),
            Product("Best product seven", false),
            Product("Best product eight", true),
            Product("Best product nine", false),
            Product("Best product ", true)
        )

        Event("Event test 1", DateTime.now()).save()
        Task("Task test 1", Task.eTaskState.TODO).save()
        Task("Task test 2", Task.eTaskState.DOING).save()
        Task("Task test 3", Task.eTaskState.DONE).save()
        Event("Event test 2", DateTime.now()).save()
        ShoppingList("Caprabo", arrayListOf()).save()
        Task("Task test 4", Task.eTaskState.DOING).save()
        Task("Task test 5", Task.eTaskState.DONE).save()
        Event("Event test 3", DateTime.now()).save()
        ShoppingList("Butcher", products1).save()
        ShoppingList("University", products2).save()*/

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, SPLASH_TIME_OUT)
    }

    companion object {
        const val SPLASH_TIME_OUT: Long = 3000;
    }
}