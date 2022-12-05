package com.safenet.service.ui.voucher_bottomsheet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safenet.service.data.network.ModelState
import com.safenet.service.data.network.dto.VerifyResponse
import com.safenet.service.data.repository.VerificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.safenet.service.data.network.Result
import com.safenet.service.util.KeyManage


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

    var state = MutableStateFlow<ModelState<VerifyResponse>?>(null)
        private set

    var isVpnActivated = MutableStateFlow(false)
        private set

    var config = MutableStateFlow("")
        private set

    fun onConfirmClicked(voucher: String) {
        verification(voucher, KeyManage.instance.getPublic())
    }

    private fun verification(voucher: String, publicU : String) = viewModelScope.launch(Dispatchers.IO) {
        verificationRepository.verifyVoucher(
            voucher = voucher,
            publicIdU = publicU
        ).collectLatest { res ->
            when(res){
                is Result.Error -> {
                    state.value = ModelState(error = res.message ?: "")
                }
                is Result.Loading -> {
                    state.value = ModelState(isLoading = true)
                }
                is Result.Success -> {
                    state.value = ModelState(response = res.data)
                    // save token

                    isVpnActivated.value = true
                    // get config
                    res.data?.let {
                        getConfig(it.token)
                    }

                }
            }

        }
    }

    private fun getConfig(token: String) = viewModelScope.launch(Dispatchers.IO) {
        verificationRepository.getConfig("//").collectLatest { res ->
             if(res is Result.Success){
                 config.value = res.data?.config ?: ""
             }
        }
    }
}
    