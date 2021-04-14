package com.pavel.voicedo.models

import com.orm.dsl.Ignore

class ShoppingList : BaseTask(EnumTypes.LIST) {
    @Ignore
    private var products: List<Product>? = null

    fun getProducts() : ArrayList<Product> {
        if (products == null) {
            products = if (this.id == null) arrayListOf()
                       else Product.getByList(this.id)
        }

        return (products as ArrayList<Product>?)!!
    }

    private fun getProductsCount() : Int {
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