package com.pavel.voicedo.models

import com.orm.dsl.Ignore

class ShoppingList : BaseTask {
    @Ignore
    private var products: List<Product>? = null

    fun getProducts() : ArrayList<Product> {
        if (products == null) {
            if (this.id == null) products = arrayListOf()
            else products = Product.getByList(this.id)
        }

        return (products as ArrayList<Product>?)!!
    }

    constructor() : super(eTypes.LIST) {
        this.products = null
    }

    constructor(name: String, products: List<Product>) : super(eTypes.LIST, name) {
        this.products = products
    }

    fun getProductsCount() : Int {
        return getProducts().count()
    }

    fun getStringProductsCount() : String {
        return "${getProductsCount()}\nITEMS"
    }

    override fun save(): Long {
        val id = super.save()

        if (getProducts().isNotEmpty()) {
            getProducts().forEach {
                it.list = id
                it.save()
            }
        }

        return id
    }

    override fun delete(): Boolean {
        super.delete()

        if (getProducts().isNotEmpty()) {
            getProducts().forEach { it.delete() }
        }

        return true
    }
}