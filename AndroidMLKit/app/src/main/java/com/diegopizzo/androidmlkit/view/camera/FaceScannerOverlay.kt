package com.diegopizzo.androidmlkit.view.camera

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.diegopizzo.androidmlkit.R
import com.diegopizzo.androidmlkit.databinding.FaceRecognitionOverlayBinding
import com.google.mlkit.vision.face.Face
import kotlin.math.roundToInt

class FaceScannerOverlay(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private val binding =
        FaceRecognitionOverlayBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        //set default value
        setFaceValues(null)
    }

    @SuppressLint("StringFormatMatches")
    fun setFaceValues(face: Face?) {
        binding.apply {
            tvHeadXRotation.text =
                context.getString(R.string.head_rotation_x, face?.headEulerAngleX ?: 0.0)
            tvHeadYRotation.text =
                context.getString(R.string.head_rotation_y, face?.headEulerAngleY ?: 0.0)
            tvHeadZRotation.text =
                context.getString(R.string.head_rotation_z, face?.headEulerAngleZ ?: 0.0)
            tvLeftEyeOpenProbability.text = context.getString(
                R.string.left_eye_opened_prob,
                face?.rightEyeOpenProbability?.times(100)?.roundToInt() ?: 0.0
            )
            tvRightEyeOpenProbability.text =
                context.getString(
                    R.string.right_eye_opened_prob,
                    face?.leftEyeOpenProbability?.times(100)?.roundToInt() ?: 0.0
                )
            tvSmilingProbability.text =
                context.getString(
                    R.string.smiling_probability,
                    face?.smilingProbability?.times(100)?.roundToInt() ?: 0.0
                )
        }
    }
}