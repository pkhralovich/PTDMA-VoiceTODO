 package com.pavel.voicedo.activities

import android.annotation.SuppressLint
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
    companion object {
        enum class EnumStatus {
            VIEW, SAY_NAME, EDIT_LIST, WAITING_CONFIRMATION
        }
    }

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.input_description)
    lateinit var inputDescription: TextView

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler)
    lateinit var recycler: RecyclerView

    lateinit var list : ShoppingList
    private var status : EnumStatus = EnumStatus.VIEW

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        ButterKnife.bind(this)

        if (intent.hasExtra(MainActivity.PARAMS.LIST)) {
            this.list = intent.getSerializableExtra(MainActivity.PARAMS.LIST) as ShoppingList
            this.list.id = intent.getLongExtra(MainActivity.PARAMS.LIST_ID, -1)

            this.status = EnumStatus.VIEW
        } else {
            this.list = ShoppingList()
            this.status = EnumStatus.SAY_NAME
        }
    }

    override fun onInit(status: Int) {
        if (Speaker.onInit(status)) {
            updateUI()
        }
    }

    override fun getHelpText(): List<String> {
        val list : ArrayList<String> = arrayListOf()
        when (status) {
            EnumStatus.VIEW, EnumStatus.EDIT_LIST -> {
                list.add(resources.getString(R.string.remove_list_help))
                list.add(resources.getString(R.string.edit_list_name_help))
                list.add(resources.getString(R.string.edit_list_products_help))
                list.add(resources.getString(R.string.edit_list_back_help))
            }
            EnumStatus.WAITING_CONFIRMATION -> {
                list.add(resources.getString(R.string.confirmation_help))
                list.add(resources.getString(R.string.cancelation_help))
            }
            else -> list.add(resources.getString(R.string.say_list_name_help))
        }
        return list
    }

    override fun onNoOrderFound() {
        if (this.isWaitingInput() && list.description.isNotEmpty()) {
            if (list.getProducts().isEmpty()) this.status = EnumStatus.EDIT_LIST
            else this.status = EnumStatus.VIEW
        }

        super.onNoOrderFound()
    }

    override fun onResult(action: ActionParser.Action) {
        if (this.status == EnumStatus.WAITING_CONFIRMATION) {
            when (action.action) {
                ActionParser.Action.ActionType.CONFIRMATION -> this.finish()
                else -> {
                    if (list.getProducts().isEmpty()) this.status = EnumStatus.EDIT_LIST
                    else this.status = EnumStatus.VIEW
                    updateUI()
                }
            }
        } else {
            when (action.action) {
                ActionParser.Action.ActionType.INPUT -> {
                    if (this.status == EnumStatus.SAY_NAME) {
                        val lists = SugarRecord.listAll(ShoppingList::class.java)
                        val aux = BaseTask.getList(lists, action.param!!)

                        if (aux == null) {
                            list.description = action.param

                            if (list.id != null) list.save()
                            if (list.getProducts().isEmpty()) this.status = EnumStatus.EDIT_LIST
                            else this.status = EnumStatus.VIEW

                            updateUI()
                        } else Speaker.speak(R.string.list_already_exists, listenerLabel, true)
                    } else onInvalidAction()
                }
                ActionParser.Action.ActionType.CHANGE_LIST_NAME -> {
                    this.status = EnumStatus.SAY_NAME
                    this.updateUI()
                }
                ActionParser.Action.ActionType.ADD_PRODUCT -> addProduct(action)
                ActionParser.Action.ActionType.REMOVE_PRODUCT -> removeProduct(action)
                ActionParser.Action.ActionType.CHECK_PRODUCT -> checkProduct(action)
                ActionParser.Action.ActionType.FINISH_EDITION -> {
                    this.list.save()
                    this.status = EnumStatus.VIEW
                    Speaker.speak(R.string.list_saved, null, false)
                    hideListenable()
                    updateUI()
                }
                ActionParser.Action.ActionType.BACK,
                ActionParser.Action.ActionType.CANCELLATION -> {
                    if (isWaitingInput() && list.description.isNotEmpty()) {
                        if (list.getProducts().isEmpty()) this.status = EnumStatus.EDIT_LIST
                        else this.status = EnumStatus.VIEW
                        updateUI()
                    } else this.onBackPressed()
                }
                else -> onInvalidAction()
            }
        }
    }

    override fun onBackPressed() {
        if (list.id == null && list.description.isNotEmpty() && this.status != EnumStatus.WAITING_CONFIRMATION) {
            this.status = EnumStatus.WAITING_CONFIRMATION
            showListenable(false)
            Speaker.speak(R.string.response_confirm_exit, listenerLabel, true)
        } else super.onBackPressed()
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
                Speaker.speak(R.string.response_product_repeated, listenerLabel, false)
                hideListenable()
            }
        } else Speaker.speak(R.string.response_say_a_name_product, listenerLabel, true)
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
        } else Speaker.speak(R.string.response_product_not_found, listenerLabel, false)
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
        } else Speaker.speak(R.string.response_product_not_found, listenerLabel, false)
    }

    override fun updateUI() {
        updateTitle()
        inputDescription.text = list.description

        updateList()
        when (this.status) {
            EnumStatus.SAY_NAME -> {
                showListenable(false)
                Speaker.speak(R.string.ask_list_name, listenerLabel)
            }
            EnumStatus.EDIT_LIST -> {
                showListenable(false)
                if (list.getProducts().isEmpty())
                    Speaker.speak(R.string.ask_products, listenerLabel)
                else
                    Speaker.speak(R.string.response_how_can_help, listenerLabel)
            }
            else -> {}
        }
    }

    private fun updateList() {
        recycler.addOnScrollListener(HideFabOnScrollListener(fab))
        recycler.adapter = ProductAdapter(list.getProducts())
    }

    override fun onClickListen() {
        showListenable(true)

        when (this.status) {
            EnumStatus.SAY_NAME -> Speaker.speak(R.string.ask_list_name, listenerLabel)
            EnumStatus.EDIT_LIST -> {
                if (list.getProducts().isEmpty()) Speaker.speak(R.string.ask_products, listenerLabel)
                else Speaker.speak(R.string.response_how_can_help, listenerLabel)
            }
            EnumStatus.WAITING_CONFIRMATION -> Speaker.speak(R.string.response_confirm_exit, listenerLabel, true)
            else -> Speaker.speak(R.string.response_how_can_help, listenerLabel, true)
        }
    }

    override fun isWaitingInput(): Boolean {
        return this.status == EnumStatus.SAY_NAME && status != EnumStatus.WAITING_CONFIRMATION
    }

    override fun hasCustomTitle() : Boolean {
        return true
    }

    override fun getTitleResource() : Int {
        return if (list.id != null) R.string.view_list
        else R.string.create_list
    }
}