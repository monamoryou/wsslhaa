package com.example.wasselha.seller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.R

// كلاس يمثل البيانات المؤقتة للمنتج
data class ProductModel(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUriString: String // سنخزن مسار الصورة كنص مؤقتاً
)

class ProductAdapter(private val productList: List<ProductModel>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivProductItemImage)
        val tvName: TextView = view.findViewById(R.id.tvProductItemName)
        val tvDesc: TextView = view.findViewById(R.id.tvProductItemDesc)
        val tvPrice: TextView = view.findViewById(R.id.tvProductItemPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.tvName.text = product.name
        holder.tvDesc.text = product.description
        holder.tvPrice.text = "${product.price} ج.م"

        // ملاحظة: لعرض الصور المحملة من الإنترنت لاحقاً بشكل احترافي وسريع،
        // سنستخدم مكتبة Glide أو Coil. حالياً سنتركها تعرض الصورة الافتراضية للتبسيط.
    }

    override fun getItemCount(): Int = productList.size

    companion object {
        // بيانات تجريبية لمحاكاة متجر في سمهود
        val dummyProducts = listOf(
            ProductModel("1", "وجبة كرسبي عائلية", "5 قطع دجاج مقرمش مع بطاطس وثومية", 150.0, ""),
            ProductModel("2", "كيلو دقيق فاخر", "دقيق متعدد الاستخدامات للمخبوزات", 25.0, ""),
            ProductModel("3", "علبة عصير طبيعي", "عصير مانجو طبيعي طازج 1 لتر", 40.0, "")
        )

        fun setupRecyclerView(recyclerView: RecyclerView, products: List<ProductModel> = dummyProducts): ProductAdapter {
            recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(recyclerView.context)
            val adapter = ProductAdapter(products)
            recyclerView.adapter = adapter
            return adapter
        }
    }
}
