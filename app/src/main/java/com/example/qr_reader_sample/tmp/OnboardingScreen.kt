import android.annotation.SuppressLint
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.koin.koinScreenModel
import java.util.concurrent.Executors

class OnboardingScreen : Screen() {

    @Composable
    override fun Content() {
        // ViewModelの状態を取得
        val vm = koinScreenModel<OnboardingVM>()
        val state by vm.state.collectAsStateWithLifecycle()

        // カメラプレビューとQRコードスキャナーのセットアップ
        CameraPreviewView(
            onQrCodeDetected = { qrCode ->
                // QRコードが検出されたらViewModelに処理を通知
                vm.onEvent(OnboardingVM.Event.ProcessImage(qrCode))
            }
        )
    }

    @SuppressLint("RememberReturnType")
    @Composable
    fun CameraPreviewView(onQrCodeDetected: (String) -> Unit) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

        // プレビュー表示用のボックス
        Box(modifier = Modifier.fillMaxSize()) {
            val previewView = remember { PreviewView(context) }

            // カメラプロバイダーを設定
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener(
                {
                    val cameraProvider = cameraProviderFuture.get()

                    // プレビューのセットアップ
                    val preview =
                        Preview.Builder().build().apply {
                            setSurfaceProvider(previewView.surfaceProvider)
                        }

                    // QRコード検出用の画像解析を設定
                    val imageAnalysis =
                        ImageAnalysis.Builder().build().apply {
                            setAnalyzer(cameraExecutor, QrCodeScanner(onQrCodeDetected))
                        }

                    // カメラに対してプレビューと画像解析をバインド
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis,
                    )
                },
                ContextCompat.getMainExecutor(context),
            )

            // PreviewViewをUIに組み込む
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        }
    }
}
