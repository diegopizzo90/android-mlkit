package com.diegopizzo.androidmlkit.view

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import com.diegopizzo.androidmlkit.R
import com.diegopizzo.androidmlkit.databinding.FragmentBottomSheetBinding
import com.diegopizzo.androidmlkit.view.viewmodel.MainViewModel
import com.diegopizzo.androidmlkit.view.viewmodel.MainViewState
import com.diegopizzo.androidmlkit.view.viewmodel.ViewEvent.BottomDialogCancelButtonClicked
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.viewmodel.ext.android.sharedViewModel

class BottomDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBottomSheetBinding

    private val viewModel: MainViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        binding.tvDataScanned.text = arguments?.getString(DATA_SCANNED_KEY) ?: ""
        viewModel.viewStates().observe(viewLifecycleOwner, viewStateObserver)
        setCancelButton()
        setOptionButtonsClickListener()
    }

    private val viewStateObserver = Observer<MainViewState> {
        binding.imOpenLink.apply {
            visibility = if (it.isOpenLinkButtonVisible) View.VISIBLE else View.GONE
        }
    }

    private fun shareValue() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, binding.tvDataScanned.text)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, null))
    }

    private fun openLink() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(binding.tvDataScanned.text as String)
        }
        try {
            startActivity(sendIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                R.string.action_can_not_be_performed,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun copyValueToClipboard() {
        val clipboard = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("text", binding.tvDataScanned.text))
        Toast.makeText(context, getString(R.string.copied_to_clipboard), Toast.LENGTH_LONG).show()
    }

    private fun setCancelButton() {
        binding.ivCancel.setOnClickListener {
            dismiss()
            viewModel.process(BottomDialogCancelButtonClicked)
        }
    }

    private fun setOptionButtonsClickListener() {
        binding.apply {
            imOpenLink.setClickListener { openLink() }
            imCopy.setClickListener { copyValueToClipboard() }
            imShare.setClickListener { shareValue() }
        }
    }

    companion object {
        const val DATA_SCANNED_KEY = "DATA_SCANNED_KEY"

        @JvmStatic
        fun newInstance(bundle: Bundle): BottomDialogFragment {
            val fragment = BottomDialogFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}