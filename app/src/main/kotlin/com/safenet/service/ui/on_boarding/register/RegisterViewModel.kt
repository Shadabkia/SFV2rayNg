package com.safenet.service.ui.on_boarding.register

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safenet.service.data.local.DataStoreManager
import com.safenet.service.data.local.DataStoreManager.PreferenceKeys.ACCESS_TOKEN
import com.safenet.service.data.local.DataStoreManager.PreferenceKeys.PUBLIC_S
import com.safenet.service.data.network.ModelState
import com.safenet.service.data.network.Result
import com.safenet.service.data.network.dto.LoginResponse
import com.safenet.service.data.repository.VerificationRepository
import com.safenet.service.util.ApiUrl.base_url_counter
import com.safenet.service.util.KeyManage
import com.safenet.service.util.Utils
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
class RegisterViewModel @Inject
constructor(
    savedStateHandle: SavedStateHandle,
    private val verificationRepository: VerificationRepository,
    private val dataStoreManager: DataStoreManager,
) : ViewModel() {

    private val registerEventChannel = Channel<RegisterEvents>()
    val registerEvent = registerEventChannel.receiveAsFlow()

    var uiState = MutableStateFlow<ModelState<LoginResponse>?>(null)
        private set

    var usernameError = MutableStateFlow<String?>(null)
        private set

    var passwordError = MutableStateFlow<String?>(null)
        private set
    var passConfirmError = MutableStateFlow<String?>(null)
        private set
    var emailError = MutableStateFlow<String?>(null)
        private set
    var referralError = MutableStateFlow<String?>(null)
        private set

    fun fragmentCreated() = viewModelScope.launch {
        registerEventChannel.send(RegisterEvents.InitViews)
    }

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


    fun onRegisterClicked(
        context: Context, username: String, password: String, confirmPass: String,
        email: String, referral: String
    ) {
        if (!validateInput(username, password, confirmPass, email, referral)) {
            return

        } else {
            Timber.tag("ConfigApi").d("verification getPublic : ${KeyManage.instance.getPublic()}")
            val publicS = KeyManage.instance.getPublic().trim()
            verification(
                context,
                username.trim(),
                password.trim(),
                email.trim(),
                referral.trim(),
                publicS,
            )
        }
    }

    private fun verification(
        context: Context,
        username: String,
        password: String,
        email: String,
        referral: String,
        publicU: String
    ) =
        viewModelScope.launch(Dispatchers.IO) {
            Timber.tag("osinfo").d("osinfo: ${Utils.getOsInfo(context)}")
            verificationRepository.register(
                username = username,
                password = password,
                email = email,
                referral = referral,
                publicIdU = publicU,
                Utils.getOsInfo(context),
            ).collectLatest { res ->
                when (res) {
                    is Result.Error -> {
                        uiState.value = ModelState(error = res.message ?: "Error")
                        Timber.tag("ConfigApi").d("verification error")
                        base_url_counter.value++

                    }

                    is Result.Loading -> {
                        uiState.value = ModelState(isLoading = true)
                    }

                    is Result.Success -> {
                        Timber.tag("ConfigApi").d("verification success ${res.data}")
                        base_url_counter.value = 0
                        when (res.data?.status?.code) {
                            0 -> {
                                registerEventChannel.send(RegisterEvents.Success)
                                setTokenAndPublicKeyToDataStore(res.data)
                            }

                            -1 -> {
                                // wrong serial
                                uiState.value = ModelState(error =res.message?:"Error"  )
                            }

                            -2 -> {
                                // max user -- mitavani force verify konid --> call again in "yes" ignore in "no"
                                uiState.value = ModelState( error =res.message?:"Error"  )
                            }

                            -4 -> {
                                uiState.value = ModelState(error = res.message ?: "max login")
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
                                uiState.value = ModelState(error = res.data?.status?.message ?: "Error")
                            }
                        }
                    }
                }
            }
        }

    private fun setTokenAndPublicKeyToDataStore(data: LoginResponse) = viewModelScope.launch {

        val pair = KeyManage.instance.setToken(
            data.token,
            data.publicS
        )
        dataStoreManager.updateData(
            ACCESS_TOKEN,
            pair.first
        )
        dataStoreManager.updateData(
            PUBLIC_S,
            pair.second
        )



//        Timber.tag(RegisterBottomSheetViewModel.TAG).d("pair f : ${pair.first}")
//        Timber.tag(RegisterBottomSheetViewModel.TAG).d("pair s : ${pair.second}")

//        Timber.tag(RegisterBottomSheetViewModel.TAG).d(
//            "token decompiled : ${
//                KeyManage.instance.getToken(
//                    pair.first,
//                    pair.second
//                )
//            }"
//        )

        uiState.value = ModelState(response = data)
    }

    private fun validateInput(
        username: String, password: String, confirmPassword: String,
        email: String, referral: String
    ): Boolean {
        var validate = true

        if (username.length < 5) {
            usernameError.value = "Limit: 5 char"
            validate = false
        } else {
            usernameError.value = null
        }
        if (password.length < 5) {
            passwordError.value = "Limit: 5 char"
            validate = false
        } else {
            passwordError.value = null
        }
        if (password != confirmPassword) {
            passConfirmError.value = "Confirm password"
            validate = false
        } else {
            passConfirmError.value = null
        }

        if (email.isNotEmpty() && !isValidEmail(email)) {
            emailError.value = "Write correct email"
            validate = false
        } else {
            emailError.value = null
        }

        if (referral.isNotEmpty() && referral.length != 4) {
            referralError.value = "referral is 4 char"
            validate = false
        } else {
            referralError.value = null
        }

        return validate
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
        return email.matches(emailRegex.toRegex())
    }

    companion object {
        const val TAG = "ConfigApi"
    }
}
    