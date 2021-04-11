package com.pavel.voicedo.activities

import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.pavel.voicedo.R
import com.pavel.voicedo.activities.base.ListenableActivity
import com.pavel.voicedo.adapters.ProductAdapter
import com.pavel.voicedo.adapters.TodoAdapter
import com.pavel.voicedo.listeners.HideFabOnScrollListener
import com.pavel.voicedo.models.ShoppingList
import com.pavel.voicedo.voice.ActionParser

class ListActivity : ListenableActivity() {
    lateinit var list : ShoppingList

    @BindView(R.id.input_description)
    lateinit var input_description: TextView
    @BindView(R.id.recycler)
    lateinit var recycler: RecyclerView

    enum class eStatus {
        VIEW, EDIT, CREATE
    }

    var status : eStatus = eStatus.VIEW
    override fun getHelpText(): List<String> {
        val list : ArrayList<String> = arrayListOf<String>()
        when (status) {
            eStatus.VIEW -> {
                list.add(resources.getString(R.string.remove_list_help))
                list.add(resources.getString(R.string.edit_list_name_help))
                list.add(resources.getString(R.string.edit_list_products_help))
                list.add(resources.getString(R.string.edit_list_back_help))
            }
            else -> { }
        }
        return list
    }

    override fun onResult(action: ActionParser.Action) {
        TODO("Not yet implemented")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_list)
        ButterKnife.bind(this)

        list = getIntent().getSerializableExtra(MainActivity.PARAMS.LIST) as ShoppingList

        updateUI()
    }

    fun updateUI() {
        input_description.text = list.description

        recycler.addOnScrollListener(HideFabOnScrollListener(fab))
        recycler.adapter = ProductAdapter(list.getProducts())
    }
}