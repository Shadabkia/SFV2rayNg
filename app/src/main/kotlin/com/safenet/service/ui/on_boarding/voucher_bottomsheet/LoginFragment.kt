package com.safenet.service.ui.on_boarding.voucher_bottomsheet

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.safenet.service.R
import com.safenet.service.databinding.BottomsheetEnterVoucherBinding
import com.safenet.service.extension.toast
import com.safenet.service.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val viewModel by activityViewModels<EnterVoucherBottomSheetViewModel>()

    private var _binding: BottomsheetEnterVoucherBinding? = null
    private val binding get() = _binding!!

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
                    EnterVoucherBottomSheetEvents.Success -> {
                        requireContext().toast("You can connect now!")
                        startMainActivity()
                    }
                    EnterVoucherBottomSheetEvents.Error -> {
                        requireContext().toast("Error")
                    }
                    EnterVoucherBottomSheetEvents.MaxUserDialog -> showMaxUserDialog()
                    is EnterVoucherBottomSheetEvents.MaxLoginDialog -> showMaxLoginDialog()
                }
            }
        }

        viewModel.fragmentCreated()

    }

    private fun startMainActivity(){
        val intent = Intent(requireActivity(), MainActivity::class.java)
        requireContext().startActivity(intent)
    }

    private fun showMaxLoginDialog() {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setMessage("The account has reached its maximum Login for today! Try again tomorrow")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                //bottomsheet dismiss
            }
            .show()

    }

    private fun showMaxUserDialog() {

        val dialog = AlertDialog.Builder(requireContext())
        dialog.setMessage("Apparently, your account is full. If you Continue, another user will be disconnected")
            .setPositiveButton(R.string.continue_verify) { _, _ ->
                viewModel.onConfirmClicked(requireContext() ,binding.etUsername.text.toString(),binding.etVoucher.text.toString(), force = 1)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->

            }
            .show()

    }


    private fun initViews() {
        initListeners()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                binding.pbVerification.isVisible = state?.isLoading ?: false
                state?.let {
                    if (state.error.isNotBlank()) {
                        activity?.toast(state.error)
                        viewModel.state.value = null
                    }
                }
            }
        }
    }

    private fun initListeners() {
        binding.apply {
            btConfirm.setOnClickListener {
                if (etVoucher.text.toString().isNotEmpty()) {
                    viewModel.onConfirmClicked(requireContext(), etUsername.text.toString(), etVoucher.text.toString(), force = 0)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}