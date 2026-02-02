package com.naze.parkingfee.presentation.ui.appupdate

/**
 * 앱 업데이트 체크 UI의 MVI Contract
 */
object AppUpdateContract {
    sealed class Intent {
        data class CheckOnLaunch(val currentVersionCode: Long) : Intent()
        object ClickUpdate : Intent()
        object ClickLater : Intent()
    }

    data class State(
        val isChecking: Boolean = false,
        val showForceUpdateDialog: Boolean = false,
        val showOptionalUpdateDialog: Boolean = false,
        val message: String = "",
        val storeUrl: String? = null,
        val latestVersionCode: Long = 0L
    )

    sealed class Effect {
        data class OpenStore(val storeUrl: String?) : Effect()
    }
}

