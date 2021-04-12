package com.pavel.voicedo.activities

import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.orm.SugarRecord
import com.pavel.voicedo.R
import com.pavel.voicedo.activities.base.ListenableActivity
import com.pavel.voicedo.adapters.ProductAdapter
import com.pavel.voicedo.listeners.HideFabOnScrollListener
import com.pavel.voicedo.models.BaseTask
import com.pavel.voicedo.models.Product
import com.pavel.voicedo.models.ShoppingList
import com.pavel.voicedo.voice.ActionParser
import com.pavel.voicedo.voice.Speaker

class ListActivity : ListenableActivity() {
    lateinit var list : ShoppingList

    @BindView(R.id.input_description)
    lateinit var input_description: TextView
    @BindView(R.id.recycler)
    lateinit var recycler: RecyclerView

    enum class eStatus {
        VIEW, SAY_NAME, EDIT_LIST
    }

    private var status : eStatus = eStatus.VIEW

    override fun getHelpText(): List<String> {
        val list : ArrayList<String> = arrayListOf()
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
        when (action.action) {
            ActionParser.Action.eActionType.INPUT -> {
                if (this.status == eStatus.SAY_NAME) {
                    val lists = SugarRecord.listAll(ShoppingList::class.java)
                    val aux = BaseTask.getList(lists, action.param!!)

                    if (aux == null) {
                        list.description = action.param

                        if (list.getProducts().isEmpty()) this.status = eStatus.EDIT_LIST
                        else this.status = eStatus.VIEW

                        updateUI()
                    } else Speaker.speak(R.string.list_already_exists, listener_label, true)
                }
                else onInvalidAction()
            }
            ActionParser.Action.eActionType.CHANGE_LIST_NAME -> {
                this.status = eStatus.SAY_NAME
                this.updateUI()
            }
            ActionParser.Action.eActionType.ADD_PRODUCT -> addProduct(action)
            ActionParser.Action.eActionType.REMOVE_PRODUCT -> removeProduct(action)
            ActionParser.Action.eActionType.CHECK_PRODUCT -> checkProduct(action)
            ActionParser.Action.eActionType.FINISH_EDITION -> {
                this.list.save()
                this.status = eStatus.VIEW
                Speaker.speak(R.string.list_saved, null, false)
                hideListenable()
                updateUI()
            }
            ActionParser.Action.eActionType.BACK -> this.finish()
            else -> onInvalidAction()
        }
    }

    private fun addProduct(action: ActionParser.Action) {
        if (action.param != null && action.param.isNotEmpty()) {
            val product : Product? = Product.find(list.getProducts(), action.param)
            if (product == null) {
                list.getProducts().add(Product(action.param, false))
                if (list.id != null)
                    list.save()

                updateList()
                stopListening()
                hideListenable()
            } else {
                Speaker.speak(R.string.response_product_repeated, listener_label, false)
                hideListenable()
            }
        } else Speaker.speak(R.string.response_say_a_name_product, listener_label, true)
    }

    private fun removeProduct(action: ActionParser.Action) {
        val product : Product? = Product.find(list.getProducts(), action.param!!)
        if (product != null) {
            list.getProducts().remove(product)
            if (list.id != null)
                list.save()

            updateList()

            stopListening()
            hideListenable()
        } else Speaker.speak(R.string.response_product_not_found, listener_label, false)
    }

    private fun checkProduct(action: ActionParser.Action) {
        val product : Product? = Product.find(list.getProducts(), action.param!!)
        if (product != null) {
            product.bought = !product.bought
            if (list.id != null)
                list.save()

            updateList()

            stopListening()
            hideListenable()
        } else Speaker.speak(R.string.response_product_not_found, listener_label, false)
    }

    private fun onInvalidAction() {
        Speaker.speak(R.string.response_not_unserstand, listener_label)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        ButterKnife.bind(this)

        if (intent.hasExtra(MainActivity.PARAMS.LIST)) {
            this.list = intent.getSerializableExtra(MainActivity.PARAMS.LIST) as ShoppingList
            this.list.id = intent.getLongExtra(MainActivity.PARAMS.LIST_ID, -1)

            this.status = eStatus.VIEW
        } else {
            this.list = ShoppingList()
            this.status = eStatus.SAY_NAME
        }
    }

    override fun onInit(status: Int) {
        if (Speaker.onInit(status)) {
            updateUI()
        }
    }

    private fun updateUI() {
        input_description.text = list.description

        updateList()
        when (this.status) {
            eStatus.SAY_NAME -> {
                showListenable(false)
                Speaker.speak(R.string.ask_list_name, listener_label)
            }
            eStatus.EDIT_LIST -> {
                showListenable(false)
                if (list.getProducts().isEmpty())
                    Speaker.speak(R.string.ask_products, listener_label)
                else
                    Speaker.speak(R.string.response_how_can_help, listener_label)
            }
            eStatus.VIEW -> {}
        }
    }

    private fun updateList() {
        recycler.addOnScrollListener(HideFabOnScrollListener(fab))
        recycler.adapter = ProductAdapter(list.getProducts())
    }

    override fun onClickListen() {
        when (this.status) {
            eStatus.SAY_NAME -> {
                showListenable(true)
                Speaker.speak(R.string.ask_list_name, listener_label)
            }
            eStatus.EDIT_LIST -> {
                showListenable(true)
                if (list.getProducts().isEmpty()) Speaker.speak(R.string.ask_products, listener_label)
                else Speaker.speak(R.string.response_how_can_help, listener_label)
            }
            else -> {
                showListenable(true)
                Speaker.speak(R.string.response_how_can_help, listener_label, true)
            }
        }
    }

    override fun isWaitingInput(): Boolean {
        return this.status == eStatus.SAY_NAME
    }
}