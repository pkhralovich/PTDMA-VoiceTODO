package com.pavel.voicedo.models

class ShoppingList : BaseTask {
    var products: List<Product>

    constructor(id: Int, name: String, products: List<Product>) : super(id, eTypes.LIST, name) {
        this.products = products
    }

    public fun getProductsCount() : Int {
        return products.count()
    }

    public fun getStringProductsCount() : String {
        return "${getProductsCount()}\nITEMS"
    }
}