package com.trynite.chefyarnnn.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trynite.chefyarnnn.BuildConfig
import com.trynite.chefyarnnn.data.repository.AppUpdateRepository
import com.trynite.chefyarnnn.data.repository.DownloadedAppUpdate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AppUpdateState {
    data object Checking : AppUpdateState
    data object UpToDate : AppUpdateState
    data class Downloading(val versionName: String, val progress: Int?) : AppUpdateState
    data class Ready(val update: DownloadedAppUpdate) : AppUpdateState
    data class Failed(val message: String) : AppUpdateState
    data object Dismissed : AppUpdateState
}

class AppUpdateViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppUpdateRepository(application.filesDir)
    private val _state = MutableStateFlow<AppUpdateState>(AppUpdateState.Checking)
    val state: StateFlow<AppUpdateState> = _state.asStateFlow()

    init {
        checkForUpdate()
    }

    fun checkForUpdate() {
        _state.value = AppUpdateState.Checking
        viewModelScope.launch {
            try {
                val update = repository.downloadLatestUpdate(BuildConfig.VERSION_NAME) { versionName, progress ->
                    if (_state.value != AppUpdateState.Dismissed) {
                        _state.value = AppUpdateState.Downloading(versionName, progress)
                    }
                }
                if (_state.value != AppUpdateState.Dismissed) {
                    _state.value = if (update == null) {
                        AppUpdateState.UpToDate
                    } else {
                        AppUpdateState.Ready(update)
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                if (_state.value != AppUpdateState.Dismissed) {
                    _state.value = AppUpdateState.Failed(
                        exception.message ?: "Hindi masuri o ma-download ang update."
                    )
                }
            }
        }
    }

    fun dismiss() {
        _state.value = AppUpdateState.Dismissed
    }
}
