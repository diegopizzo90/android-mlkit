package com.diegopizzo.androidmlkit.view.camera

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.diegopizzo.androidmlkit.R
import com.diegopizzo.androidmlkit.databinding.FaceInfoOverlayBinding
import com.google.mlkit.vision.face.Face
import kotlin.math.roundToInt

class FaceInfoOverlay(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private val binding =
        FaceInfoOverlayBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        //set default value
        setFaceValues(null)
    }

    @SuppressLint("StringFormatMatches")
    fun setFaceValues(face: Face?, isImageFlipped: Boolean = true) {
        binding.apply {
            tvHeadXRotation.text =
                context.getString(R.string.head_rotation_x, face?.headEulerAngleX ?: 0.0)
            tvHeadYRotation.text =
                context.getString(R.string.head_rotation_y, face?.headEulerAngleY ?: 0.0)
            tvHeadZRotation.text =
                context.getString(R.string.head_rotation_z, face?.headEulerAngleZ ?: 0.0)

            val leftEyeOpenProbability = if (isImageFlipped) {
                //Swap eyes in case of using front camera
                face?.rightEyeOpenProbability ?: 0F
            } else face?.leftEyeOpenProbability ?: 0F

            val rightEyeOpenProbability = if (isImageFlipped) {
                //Swap eyes in case of using front camera
                face?.leftEyeOpenProbability ?: 0F
            } else face?.rightEyeOpenProbability ?: 0F

            tvLeftEyeOpenProbability.text = context.getString(
                R.string.left_eye_opened_prob,
                leftEyeOpenProbability.times(100).roundToInt()
            )
            tvRightEyeOpenProbability.text = context.getString(
                R.string.right_eye_opened_prob,
                rightEyeOpenProbability.times(100).roundToInt()
            )
            tvSmilingProbability.text =
                context.getString(
                    R.string.smiling_probability,
                    face?.smilingProbability?.times(100)?.roundToInt() ?: 0.0
                )
        }
    }
}