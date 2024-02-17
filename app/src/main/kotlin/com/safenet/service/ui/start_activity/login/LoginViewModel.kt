package com.safenet.service.ui.start_activity.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safenet.service.data.local.DataStoreManager
import com.safenet.service.data.network.ModelState
import com.safenet.service.data.network.Result
import com.safenet.service.data.network.dto.RegisterResponse
import com.safenet.service.data.repository.VerificationRepository
import com.safenet.service.ui.start_activity.InputError
import com.safenet.service.ui.start_activity.StarterActivity
import com.safenet.service.util.ApiUrl
import com.safenet.service.util.KeyManage
import com.safenet.service.util.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val verificationRepository: VerificationRepository
) : ViewModel() {

    var uiState = MutableStateFlow<ModelState<RegisterResponse>?>(null)
        private set

    var usernameError = MutableStateFlow<InputError?>(null)
        private set

    var passwordError = MutableStateFlow<InputError?>(null)
        private set

    init {
        viewModelScope.launch {
            dataStoreManager.getData(DataStoreManager.PreferenceKeys.BASE_URL)
                .collectLatest { url ->
                    url?.let {
                        Timber.tag("baseurl").d("base : $url")
                        verificationRepository.setBaseUrl(it)
                    }
                }
        }
    }

    fun onLoginClicked(context : StarterActivity, username: String, password: String, force : Int) {
        if(!validateInput(username, password)){
            return

        } else {
            Timber.tag("ConfigApi").d("verification getPublic : ${KeyManage.instance.getPublic()}")
            val publicS = KeyManage.instance.getPublic()
            verification(context, username.trim(), password.trim(), publicS.trim(), force)
        }
    }

    private fun verification(context: Context, username: String, password: String, publicU: String, force: Int)  =
        viewModelScope.launch(Dispatchers.IO) {
            Timber.tag("osinfo").d("osinfo: ${Utils.getOsInfo(context)}")
            verificationRepository.login(
                username = username.trim(),
                password = password.trim(),
                publicIdU = publicU,
                Utils.getOsInfo(context),
                force
            ).collectLatest { res ->
                when (res) {
                    is Result.Error -> {
                        uiState.value = ModelState(error = res.message ?: "")
//                        enterVoucherEventChannel.send(EnterVoucherBottomSheetEvents.Error)
                        Timber.tag("ConfigApi").d("verification error")
                        ApiUrl.base_url_counter.value++

                    }

                    is Result.Loading -> {
                        uiState.value = ModelState(isLoading = true)
                    }

                    is Result.Success -> {
                        Timber.tag("ConfigApi").d("verification success ${res.data}")
                        ApiUrl.base_url_counter.value = 0
                        when (res.data?.status?.code) {
                            0 -> {
//                                enterVoucherEventChannel.send(EnterVoucherBottomSheetEvents.Success)
                                setTokenAndPublicKeyToDataStore(res.data)
                            }

                            -1 -> {
                                // wrong serial
                                uiState.value = ModelState(error = "Wrong code")
                            }

                            -2 -> {
                                // max user -- mitavani force verify konid --> call again in "yes" ignore in "no"
//                                enterVoucherEventChannel.send(EnterVoucherBottomSheetEvents.MaxUserDialog)
                                uiState.value = ModelState(isLoading = false)
                            }

                            -4 -> {
//                                enterVoucherEventChannel.send(
//                                    EnterVoucherBottomSheetEvents.MaxLoginDialog(
//                                        "max login"
//                                    )
//                                )
                                uiState.value = ModelState(error = "")
                            }

                            -3 -> {
                                // wrong public key
                                uiState.value = ModelState(error = res.data.status.message)
                            }

                            -7 -> {
                                // active tunnel problem
                                uiState.value =
                                    ModelState(error = "Technical Problem.Please Contact Support")
                            }

                            else -> {
                                uiState.value = ModelState(error = res.data?.status?.message ?: "")
                            }
                        }
                    }

                    is Result.Error -> TODO()
                    is Result.Loading -> TODO()
                    is Result.Success -> TODO()
                }
            }
    }

    private fun setTokenAndPublicKeyToDataStore(data: RegisterResponse) = viewModelScope.launch {

        val pair = KeyManage.instance.setToken(
            data.token,
            data.publicS
        )

        dataStoreManager.updateData(
            DataStoreManager.PreferenceKeys.PUBLIC_S,
            pair.second
        )

        dataStoreManager.updateData(
            DataStoreManager.PreferenceKeys.ACCESS_TOKEN,
            pair.first
        )

        uiState.value = ModelState(response = data)
    }

    private fun validateInput(username : String, password : String) : Boolean{
        var validate = true
        if(username.length < 5) {
            usernameError.value = InputError(0, "Limit: 5 char")
            validate = false
        } else {
            usernameError.value = usernameError.value?.copy(
                error = null
            )
        }
        if(password.length < 5) {
            passwordError.value = InputError(0, "Limit: 5 char")
            validate = false
        } else{
            passwordError.value = passwordError.value?.copy(
                error = null
            )
        }

        return validate
    }




}