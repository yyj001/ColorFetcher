package com.ish.colorfetcher

import android.R.color
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.select_image_btn).setOnClickListener {
            selectPhoto()
        }
    }

    private fun selectPhoto() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_PICK
        startActivityForResult(intent, 10080)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 10080) {
            // Uri 格式参考: content://media/external/images/media/123
            findViewById<RecyclerView>(R.id.list).adapter = null
            val uri = data?.data ?: return
            Glide.with(this)
                .asBitmap()
                .load(uri)
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        onResourceReady(resource, uri)
                    }
                })
        }
    }

    private fun onResourceReady(bitmap: Bitmap, uri: Uri) {
        findViewById<ImageView>(R.id.image).setImageBitmap(bitmap)
        Glide.with(this)
            .load(uri)
            .into(findViewById(R.id.image))

        val colorList = arrayListOf<Pair<String, Int>>()
        val paletteBuilder = Palette.from(bitmap)
        paletteBuilder.generate { palette ->
            var color = palette?.getDominantColor(Color.TRANSPARENT)?: Color.TRANSPARENT
            colorList.add("${converArgbToRgb(color)}-主色调" to color)
            color = palette?.getMutedColor(Color.TRANSPARENT)?: Color.TRANSPARENT
            colorList.add("${converArgbToRgb(color)}-柔和的颜色" to color)
            color = palette?.getDarkMutedColor(Color.TRANSPARENT)?: Color.TRANSPARENT
            colorList.add("${converArgbToRgb(color)}-柔和的暗色" to color)
            color = palette?.getLightMutedColor(Color.TRANSPARENT)?: Color.TRANSPARENT
            colorList.add("${converArgbToRgb(color)}-柔和的亮色" to color)
            color = palette?.getVibrantColor(Color.TRANSPARENT)?: Color.TRANSPARENT
            colorList.add("${converArgbToRgb(color)}-活力色" to color)
            color = palette?.getDarkVibrantColor(Color.TRANSPARENT)?: Color.TRANSPARENT
            colorList.add("${converArgbToRgb(color)}-活力暗色" to color)
            color = palette?.getLightVibrantColor(Color.TRANSPARENT)?: Color.TRANSPARENT
            colorList.add("${converArgbToRgb(color)}-活力亮色" to color)

            findViewById<RecyclerView>(R.id.list).let { recyclerView ->
                val adapter = ColorListAdapter()
                recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                recyclerView.adapter = adapter
                adapter.setColors(colorList)
            }
        }
    }

    fun converArgbToRgb(color: Int): String {
        if (color == Color.TRANSPARENT) {
            return "取色失败"
        }
        var red = (color and 0xff0000 shr 16).toString(16)
        var green = (color and 0x00ff00 shr 8).toString(16)
        var blue = (color and 0x0000ff).toString(16)
        if (red.length < 2) {
            red = "0$red"
        }
        if (green.length < 2) {
            green = "0$green"
        }
        if (blue.length < 2) {
            blue = "0$blue"
        }
        return "#$red$green$blue".toUpperCase()
    }
}

class ColorListAdapter: RecyclerView.Adapter<ColorListAdapter.ViewHolder>() {

    var list = arrayListOf<Pair<String, Int>>()

    fun setColors(l: ArrayList<Pair<String, Int>>) {
        this.list = l
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = View.inflate(parent.context, R.layout.layout_item_color, null)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.itemView.findViewById<View>(R.id.color_view).setBackgroundColor(data.second)
        holder.itemView.findViewById<TextView>(R.id.text).text = data.first
    }

    override fun getItemCount(): Int {
        return list.size
    }
}