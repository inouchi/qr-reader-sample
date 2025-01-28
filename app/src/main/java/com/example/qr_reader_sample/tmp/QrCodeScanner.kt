import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * QRコードを解析するためのクラス。
 * このクラスは、CameraXのImageAnalysis.Analyzerインターフェースを実装しており、カメラから取得した画像を解析してQRコードを検出します。
 * QRコードが検出されると、指定されたコールバック関数(onQrCodeDetected)が呼ばれ、QRコードの内容を返します。
 *
 * @param onQrCodeDetected QRコードが検出されたときに呼ばれるコールバック関数。
 */
class QrCodeScanner(private val onQrCodeDetected: (String) -> Unit) : ImageAnalysis.Analyzer {

    // QRコードを検出するためのスキャナーを作成
    private val scanner =
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE) // QRコードのみを検出対象に設定
                .build()
        )

    @OptIn(ExperimentalGetImage::class) // 画像を取得するためにOptInを指定
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.image?.let { mediaImage -> // 画像が存在する場合のみ処理を実行
            // InputImageに変換
            val inputImage =
                InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            // QRコードのスキャン処理
            scanner
                .process(inputImage)
                .addOnSuccessListener { barcodes -> // QRコードが検出された場合
                    barcodes.firstOrNull()?.rawValue?.let { qrCode ->
                        // QRコードを検出した場合、コールバックを呼び出す
                        onQrCodeDetected(qrCode)
                    }
                }
                .addOnFailureListener {
                    // エラーハンドリング（ここでは何もしていないが、必要に応じて処理を追加）
                }
                .addOnCompleteListener {
                    // 処理が完了したらimageProxyを閉じる
                    imageProxy.close()
                }
        } ?: imageProxy.close() // 画像がnullの場合はimageProxyを閉じる
    }
}
