package com.pavel.voicedo.models

import com.orm.dsl.Ignore

class ShoppingList : BaseTask {
    @Ignore
    private var products: List<Product>? = null

    fun getProducts() : List<Product> {
        if (products == null) {
            products = Product.getByList(this.id)
        }

        return products!!
    }

    constructor() {
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