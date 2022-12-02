package com.safenet.service.ui.voucher_bottomsheet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safenet.service.data.repository.VerificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnterVoucherBottomSheetViewModel @Inject
constructor(
    savedStateHandle: SavedStateHandle,
    private val verificationRepository: VerificationRepository
) : ViewModel() {


    private val enterVoucherEventChannel = Channel<EnterVoucherBottomSheetEvents>()
    val enterVoucherEvent = enterVoucherEventChannel.receiveAsFlow()

    fun fragmentCreated() = viewModelScope.launch {
        enterVoucherEventChannel.send(EnterVoucherBottomSheetEvents.InitViews)
    }

    fun onConfirmClicked(voucher: String) {

    }
}
    