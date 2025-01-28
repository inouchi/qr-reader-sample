import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch

class OnboardingVM() : StateScreenModel<RequestState<Unit>>(RequestState.Idle) {

    // イベントを表すsealed interface
    sealed interface Event {
        // QRコードを処理するイベント
        data class ProcessImage(val qrCode: String) : Event
    }

    // イベントを受け取る関数
    fun onEvent(event: Event) {
        screenModelScope.launch {
            // イベントに応じた処理を行う
            when (event) {
                is Event.ProcessImage -> handleQrCode(event.qrCode) // QRコード処理
            }
        }
    }

    // QRコードを処理する関数
    private fun handleQrCode(qrCode: String) {
        // QRコードが検出されたことをコンソールに出力
        println("QR Code detected: $qrCode")
    }
}
