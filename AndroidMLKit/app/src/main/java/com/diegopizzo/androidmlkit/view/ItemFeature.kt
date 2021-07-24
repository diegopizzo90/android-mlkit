package com.diegopizzo.androidmlkit.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.getDrawableOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import com.diegopizzo.androidmlkit.R
import com.diegopizzo.androidmlkit.databinding.ItemFeatureBinding

class ItemFeature(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private val binding = ItemFeatureBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        val styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.ItemFeature)
        val textResource = styledAttributes.getResourceIdOrThrow(R.styleable.ItemFeature_text)
        val imageSrcDrawable = styledAttributes.getDrawableOrThrow(R.styleable.ItemFeature_imageSrc)
        val backgroundColor = styledAttributes.getColor(
            R.styleable.ItemFeature_backgroundColor,
            context.getColor(R.color.barcode_color)
        )
        binding.clItem.setBackgroundColor(backgroundColor)
        binding.tvLabel.text = context.getString(textResource)
        binding.ivIcon.setImageDrawable(imageSrcDrawable)
        styledAttributes.recycle()
    }

    fun setClickListener(listener: () -> Unit) {
        binding.clItem.setOnClickListener {
            listener.invoke()
        }
    }
}