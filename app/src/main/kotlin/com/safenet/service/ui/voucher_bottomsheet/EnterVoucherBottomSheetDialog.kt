package com.safenet.service.ui.voucher_bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.safenet.service.R
import com.safenet.service.databinding.BottomsheetEnterVoucherBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EnterVoucherBottomSheetDialog : BottomSheetDialogFragment() {

    private val viewModel by viewModels<EnterVoucherBottomSheetViewModel>()

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
                }
            }
        }

        viewModel.fragmentCreated()

    }


    private fun initViews() {
        initListeners()
        binding.apply {
            btConfirm.setOnClickListener{
                if(!etVoucher.text.isNullOrEmpty()){
                    viewModel.onConfirmClicked(etVoucher.text.toString())
                }
            }
        }

    }

    private fun initListeners() {
        binding.apply {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}