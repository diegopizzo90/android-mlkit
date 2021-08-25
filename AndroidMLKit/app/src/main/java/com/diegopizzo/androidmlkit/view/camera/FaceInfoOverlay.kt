package com.diegopizzo.androidmlkit.view.camera

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.diegopizzo.androidmlkit.R
import com.diegopizzo.androidmlkit.databinding.FaceInfoOverlayBinding
import com.google.mlkit.vision.face.Face
import java.lang.ClassCastException
import kotlin.math.roundToInt

class FaceInfoOverlay(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private val binding =
        FaceInfoOverlayBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        //set default value
        setFaceValues(listOf(null))
    }

    fun setFaceValues(faces: List<Any?>, isImageFlipped: Boolean = true) {
        if (faces.isEmpty()) return
        try {
            val facesFormatted = faces as List<Face?>
            facesFormatted.map { face ->
                setHeadRotation(face)
                setLandmarksProbability(face, isImageFlipped)
            }
        } catch (e : ClassCastException) {
            return
        }
    }

    private fun setHeadRotation(face: Face?) {
        binding.apply {
            tvHeadXRotation.text =
                context.getString(R.string.head_rotation_x, face?.headEulerAngleX ?: 0F)
            tvHeadYRotation.text =
                context.getString(R.string.head_rotation_y, face?.headEulerAngleY ?: 0F)
            tvHeadZRotation.text =
                context.getString(R.string.head_rotation_z, face?.headEulerAngleZ ?: 0F)
        }
    }

    private fun setLandmarksProbability(face: Face?, isImageFlipped: Boolean) {
        val leftEyeOpenProbability = if (isImageFlipped) {
            //Swap eyes in case of using front camera
            face?.rightEyeOpenProbability ?: 0F
        } else face?.leftEyeOpenProbability ?: 0F

        val rightEyeOpenProbability = if (isImageFlipped) {
            //Swap eyes in case of using front camera
            face?.leftEyeOpenProbability ?: 0F
        } else face?.rightEyeOpenProbability ?: 0F

        binding.apply {
            tvLeftEyeOpenProbability.text = context.getString(
                R.string.left_eye_opened_prob,
                leftEyeOpenProbability.times(100).roundToInt().toString()
            )
            tvRightEyeOpenProbability.text = context.getString(
                R.string.right_eye_opened_prob,
                rightEyeOpenProbability.times(100).roundToInt().toString()
            )
            tvSmilingProbability.text = context.getString(
                R.string.smiling_probability,
                face?.smilingProbability?.times(100)?.roundToInt() ?: 0.0
            )
        }
    }
}