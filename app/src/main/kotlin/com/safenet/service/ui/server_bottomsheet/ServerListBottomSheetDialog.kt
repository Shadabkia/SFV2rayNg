package com.safenet.service.ui.server_bottomsheet

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.safenet.service.R
import com.safenet.service.data.network.dto.Server
import com.safenet.service.databinding.BottomsheetServerlistBinding
import com.safenet.service.extension.toast
import com.safenet.service.ui.server_bottomsheet.adapter.ServerListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class ServerListBottomSheetDialog : BottomSheetDialogFragment(), ServerListListener {

    private val viewModel by activityViewModels<ServerListBottomSheetViewModel>()

    private var _binding: BottomsheetServerlistBinding? = null
    private val binding get() = _binding!!

    private val serverListAdapter = ServerListAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetServerlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.serverListEvent.collect {
                when (it) {
                    ServerListBottomSheetEvents.InitViews -> initViews()
                    is ServerListBottomSheetEvents.NavigateToEnterCode -> TODO()
                    ServerListBottomSheetEvents.Success -> {
                        requireContext().toast("You can connect now!")
                        this@ServerListBottomSheetDialog.dismiss()
                    }
                    is ServerListBottomSheetEvents.Error -> {
                        requireContext().toast(it.message)
                        this@ServerListBottomSheetDialog.dismiss()
                    }
                }
            }
        }

        viewModel.fragmentCreated()

    }


    private fun initViews() {
        initListeners()

        binding.rvServerList.apply {
            adapter = serverListAdapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.serverList.collectLatest {
                Timber.tag("servers").d(""+it.size )
                serverListAdapter.submitList(it)
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

    override fun onItemClicked(serverId: Int) {
        viewModel.serverSelected(serverId)
        dismiss()
    }
}