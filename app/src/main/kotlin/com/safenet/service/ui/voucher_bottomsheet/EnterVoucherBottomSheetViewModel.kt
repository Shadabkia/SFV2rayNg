package com.safenet.service.ui.voucher_bottomsheet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safenet.service.data.local.DataStoreManager
import com.safenet.service.data.local.DataStoreManager.PreferenceKeys.ACCESS_TOKEN
import com.safenet.service.data.local.DataStoreManager.PreferenceKeys.PUBLIC_S
import com.safenet.service.data.network.ModelState
import com.safenet.service.data.network.Result
import com.safenet.service.data.network.dto.VerifyResponse
import com.safenet.service.data.repository.VerificationRepository
import com.safenet.service.util.KeyManage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class EnterVoucherBottomSheetViewModel @Inject
constructor(
    savedStateHandle: SavedStateHandle,
    private val verificationRepository: VerificationRepository,
    private val dataStoreManager: DataStoreManager,
) :ViewModel() {

    private val enterVoucherEventChannel = Channel<EnterVoucherBottomSheetEvents>()
    val enterVoucherEvent = enterVoucherEventChannel.receiveAsFlow()

    fun fragmentCreated() = viewModelScope.launch {
        enterVoucherEventChannel.send(EnterVoucherBottomSheetEvents.InitViews)
    }

    var state = MutableStateFlow<ModelState<VerifyResponse>?>(null)
        private set


    fun onConfirmClicked(voucher: String) {
        Timber.tag("ConfigApi").d("verification getPublic : ${KeyManage.instance.getPublic()}")
        val publicS = KeyManage.instance.getPublic()
        verification(voucher.trim(), publicS.trim())

    }

    private fun verification(voucher: String, publicU: String) =
        viewModelScope.launch(Dispatchers.IO) {
            verificationRepository.verifyVoucher(
                voucher = voucher,
                publicIdU = publicU
            ).collectLatest { res ->
                when (res) {
                    is Result.Error -> {
                        state.value = ModelState(error = res.message ?: "")
                        enterVoucherEventChannel.send(EnterVoucherBottomSheetEvents.Error)
                        Timber.tag("ConfigApi").d("verification error")

                    }
                    is Result.Loading -> {
                        state.value = ModelState(isLoading = true)
                    }
                    is Result.Success -> {
                        Timber.tag("ConfigApi").d("verification success ${res.data}")
                        if(res.data?.status?.code == 0){
                            enterVoucherEventChannel.send(EnterVoucherBottomSheetEvents.Success)
                            setTokenAndPublicKeyToDataStore(res.data)
                        } else{
                            state.value = ModelState(error = res.data?.status?.massage ?: "")
                        }
                    }
                }
            }
        }

    private fun setTokenAndPublicKeyToDataStore(data: VerifyResponse) = viewModelScope.launch {

        val pair = KeyManage.instance.setToken(
            data.token,
            data.publicS
        )

        dataStoreManager.updateData(
            PUBLIC_S,
            pair.second
        )

        dataStoreManager.updateData(
            ACCESS_TOKEN,
            pair.first
        )

        Timber.tag(TAG).d("pair f : ${pair.first}")
        Timber.tag(TAG).d("pair s : ${pair.second}")

        Timber.tag(TAG).d("token decompiled : ${KeyManage.instance.getToken(
            pair.first,
            pair.second
        )}")

        state.value = ModelState(response = data)
    }


    companion object {
        const val TAG = "ConfigApi"
    }
}
    