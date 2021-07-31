package com.diegopizzo.androidmlkit.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.getDrawableOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import com.diegopizzo.androidmlkit.R
import com.diegopizzo.androidmlkit.databinding.ItemMenuBinding

class ItemMenu(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private val binding = ItemMenuBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        val styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.ItemMenu)
        val textRes = styledAttributes.getResourceIdOrThrow(R.styleable.ItemMenu_itemMenuText)
        val iconDrawable =
            styledAttributes.getDrawableOrThrow(R.styleable.ItemMenu_itemMenuImageSrc)
        binding.tvText.text = context.getString(textRes)
        binding.ivIcon.setImageDrawable(iconDrawable)
        styledAttributes.recycle()
    }

    fun setClickListener(listener: () -> Unit) {
        binding.clMenuItem.setOnClickListener {
            listener.invoke()
        }
    }
}