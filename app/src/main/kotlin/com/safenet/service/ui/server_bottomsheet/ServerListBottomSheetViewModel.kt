package com.safenet.service.ui.server_bottomsheet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safenet.service.data.local.DataStoreManager
import com.safenet.service.data.local.DataStoreManager.PreferenceKeys.ACCESS_TOKEN
import com.safenet.service.data.local.DataStoreManager.PreferenceKeys.PUBLIC_S
import com.safenet.service.data.network.ModelState
import com.safenet.service.data.network.Result
import com.safenet.service.data.network.dto.Server
import com.safenet.service.data.network.dto.RegisterResponse
import com.safenet.service.data.repository.VerificationRepository
import com.safenet.service.ui.on_boarding.login.LoginViewModel
import com.safenet.service.util.KeyManage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class ServerListBottomSheetViewModel @Inject
constructor(
    savedStateHandle: SavedStateHandle,
    private val verificationRepository: VerificationRepository,
    private val dataStoreManager: DataStoreManager,
) : ViewModel() {

    private val serverListEventChannel = Channel<ServerListBottomSheetEvents>()
    val serverListEvent = serverListEventChannel.receiveAsFlow()

    fun fragmentCreated() = viewModelScope.launch {
        serverListEventChannel.send(ServerListBottomSheetEvents.InitViews)
        listenToken()
    }

    var state = MutableStateFlow<ModelState<RegisterResponse>?>(null)
        private set

    private val _serverList = MutableStateFlow<List<Server>>(listOf())
    val serverList: StateFlow<List<Server>> get() = _serverList
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

    fun listenToken() = viewModelScope.launch {
        Timber.tag(LoginViewModel.TAG).d("listenToken")
        val token = dataStoreManager.getData(ACCESS_TOKEN).first()
        if (token != null) {
            try {
                val publicS = dataStoreManager.getData(PUBLIC_S).first()
                val tokenE = KeyManage.instance.getToken(
                    token,
                    publicS ?: ""
                )
                getServerList(tokenE)
            } catch (e: Exception) {
                serverListEventChannel.send(ServerListBottomSheetEvents.Error)
            }
        } else {
            getServerList("0")
        }
    }

    private fun getServerList(token : String) = viewModelScope.launch{
        verificationRepository.getServerList(token).collectLatest {result->
            when(result){
                is Result.Error -> {
                    serverListEventChannel.send(ServerListBottomSheetEvents.Error)

                }
                is Result.Loading -> {

                }
                is Result.Success -> {
                    if(result.data?.status?.code == 0){
                        _serverList.value = result.data.list
                    }
                }
            }
        }
    }

    fun serverSelected(serverId: Int) = viewModelScope.launch{
        dataStoreManager.updateData(DataStoreManager.PreferenceKeys.SERVER_ID, value = serverId)
    }


}
    