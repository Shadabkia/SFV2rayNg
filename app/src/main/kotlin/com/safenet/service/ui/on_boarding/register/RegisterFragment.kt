package com.safenet.service.ui.on_boarding.register

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.safenet.service.R
import com.safenet.service.databinding.FragmentRegisterBinding
import com.safenet.service.extension.toast
import com.safenet.service.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private val viewModel by activityViewModels<RegisterViewModel>()

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.enterVoucherEvent.collect {
                when (it) {
                    RegisterEvents.InitViews -> initViews()
                    is RegisterEvents.NavigateToEnterCode -> TODO()
                    RegisterEvents.Success -> {
                        requireContext().toast("You can connect now!")
                        startMainActivity()
                    }

                    RegisterEvents.Error -> {
                        requireContext().toast("Error")
                    }

                }
            }
        }
        viewModel.fragmentCreated()
    }

    private fun startMainActivity() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        requireContext().startActivity(intent)
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
                if (etPassword.text.toString().isNotEmpty()) {
                    viewModel.onRegisterClicked(
                        context =requireContext(),
                        username = etUsername.text.toString(),
                        password = etPassword.text.toString(),
                        confirmPass = etPasswordConfirm.text.toString(),
                        email = etEmail.text.toString(),
                        referral = etReferral.text.toString()
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}