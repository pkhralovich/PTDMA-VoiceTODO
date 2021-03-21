package com.pavel.voicedo.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import com.pavel.voicedo.R
import com.pavel.voicedo.models.ShoppingList

class ListActivity : AppCompatActivity() {
    lateinit var list : ShoppingList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_list)
        ButterKnife.bind(this)

        list = getIntent().getSerializableExtra(MainActivity.PARAMS.LIST) as ShoppingList

        updateUI()
    }

    fun updateUI() {
        //TODO: Load task data
    }
}