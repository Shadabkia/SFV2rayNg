package com.safenet.service.ui.voucher_bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.safenet.service.R
import com.safenet.service.databinding.BottomsheetEnterVoucherBinding
import com.safenet.service.extension.toast
import com.safenet.service.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EnterVoucherBottomSheetDialog : BottomSheetDialogFragment() {

    private val viewModel by activityViewModels<EnterVoucherBottomSheetViewModel>()

    private var _binding: BottomsheetEnterVoucherBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetEnterVoucherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.enterVoucherEvent.collect {
                when (it) {
                    EnterVoucherBottomSheetEvents.InitViews -> initViews()
                    is EnterVoucherBottomSheetEvents.NavigateToEnterCode -> TODO()
                    EnterVoucherBottomSheetEvents.Success ->{
                        requireContext().toast("You can connect now!")
                        this@EnterVoucherBottomSheetDialog.dismiss()
                    }
                    EnterVoucherBottomSheetEvents.Error ->{
                        requireContext().toast("Error")
                        this@EnterVoucherBottomSheetDialog.dismiss()
                    }
                }
            }
        }

        viewModel.fragmentCreated()

    }


    private fun initViews() {
        initListeners()
        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.state.collectLatest { state ->
                binding.pbVerification.isVisible = state?.isLoading ?: false
                state?.let {
                    if(state.error.isNotBlank()) {
                        activity?.toast(state.error)
                        viewModel.state.value = null
                    }
                }
            }
        }

    }

    private fun initListeners() {
        binding.apply {
            btConfirm.setOnClickListener{
                if(etVoucher.text.toString().isNotEmpty()){
                    viewModel.onConfirmClicked(etVoucher.text.toString())
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}