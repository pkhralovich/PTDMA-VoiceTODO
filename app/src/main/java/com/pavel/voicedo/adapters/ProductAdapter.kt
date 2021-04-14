package com.pavel.voicedo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pavel.voicedo.R
import com.pavel.voicedo.models.Product

class ProductAdapter(_data: List<Product>) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
    private val data : List<Product> = _data

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.product_item, parent, false)
        return ProductViewHolder(v)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val p = data[position]

        holder.inputCheckbox.isChecked = p.bought
        holder.labelName.text = p.description
    }

    override fun getItemCount(): Int {
        return data.count()
    }

    class ProductViewHolder(_view: View) : RecyclerView.ViewHolder(_view) {
        var inputCheckbox : CheckBox = _view.findViewById(R.id.input_checked)
        var labelName : TextView = _view.findViewById(R.id.label_name)
    }
}