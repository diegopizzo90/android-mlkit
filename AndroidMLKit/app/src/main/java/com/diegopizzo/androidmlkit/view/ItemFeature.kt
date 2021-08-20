package com.diegopizzo.androidmlkit.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.getDrawableOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import com.diegopizzo.androidmlkit.R
import com.diegopizzo.androidmlkit.databinding.ItemFeatureBinding
import com.diegopizzo.androidmlkit.view.navigation.ScanningType

class ItemFeature(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private val binding = ItemFeatureBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        val styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.ItemFeature)
        val textResource = styledAttributes.getResourceId(R.styleable.ItemFeature_text, R.string.app_name)
        val imageSrcDrawable = styledAttributes.getDrawable(R.styleable.ItemFeature_imageSrc)
        val backgroundColor = styledAttributes.getResourceId(
            R.styleable.ItemFeature_backgroundColor,
            R.color.barcode_color
        )
        setData(backgroundColor, textResource, imageSrcDrawable)
        styledAttributes.recycle()
    }

    fun setItemFeatureData(data: ItemFeatureData) {
        data.apply {
            binding.apply {
                setData(backgroundColor, label, getDrawable(context, icon))
            }
        }
    }

    private fun setData(backgroundColor: Int, textResource: Int, imageSrcDrawable: Drawable?) {
        binding.apply {
            clItem.setBackgroundColor(context.getColor(backgroundColor))
            binding.tvLabel.text = context.getString(textResource)
            binding.ivIcon.setImageDrawable(imageSrcDrawable)
        }
    }

    data class ItemFeatureData(
        @ColorRes val backgroundColor: Int,
        @StringRes val label: Int,
        @DrawableRes val icon: Int,
        val scannerType: ScanningType
    )
}