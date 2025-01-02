package com.example.qr_reader_sample

import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * QRコードをスキャンするためのクラス。
 *
 * このクラスは、カメラのプレビューを表示し、画像を解析してQRコードを検出します。
 * QRコードが検出されると、指定されたコールバック関数が呼ばれます。
 * カメラの設定、画像解析、コードスキャンの処理を管理します。
 *
 * @param activity 使用するアクティビティ
 * @param previewView QRコードのプレビューを表示するビュー
 * @param callback QRコードを検出したときに呼ばれるコールバック関数
 */
class QrCodeScanner(
    private val activity: ComponentActivity,
    private val previewView: PreviewView,
    callback: (List<Barcode>) -> Unit
) {
    private val workerExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val scanner: BarcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)  // QRコード専用
            .build()
    )
    private val analyzer: CodeAnalyzer = CodeAnalyzer(scanner, callback)
    private var cameraProvider: ProcessCameraProvider? = null

    init {
        // アクティビティのライフサイクルに基づいてリソースを管理
        activity.lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_DESTROY) {
                    cleanUp()
                }
            }
        )
    }

    /**
     * QRコードのスキャンを開始
     */
    fun start() {
        val future = ProcessCameraProvider.getInstance(activity)
        future.addListener({
            cameraProvider = future.get()
            setUpCamera(cameraProvider!!)
        }, ContextCompat.getMainExecutor(activity))
    }

    /**
     * QRコードのスキャンを停止
     */
    fun stop() {
        cameraProvider?.unbindAll()
    }

    /**
     * カメラ設定を行い、カメラのバインドを行う
     *
     * @param provider カメラプロバイダ（ProcessCameraProvider）
     *
     * @return カメラの設定とバインド操作の成功/失敗を示す結果（成功時はUnit、失敗時は例外が発生）
     */
    private fun setUpCamera(provider: ProcessCameraProvider) {
        val resolutionSelector = createResolutionSelector()
        val preview = createPreview(resolutionSelector)
        val analysis = createImageAnalysis(resolutionSelector)
        bindCamera(provider, preview, analysis)
    }

    /**
     * 解像度の選択設定を作成
     *
     * @return 解像度選択に関する設定オブジェクト（ResolutionSelector）
     */
    private fun createResolutionSelector(): ResolutionSelector {
        return ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
            .build()
    }

    /**
     * プレビュー設定を作成
     *
     * @param resolutionSelector 解像度選択の設定オブジェクト
     * @return カメラのプレビュー設定オブジェクト（Preview）
     */
    private fun createPreview(resolutionSelector: ResolutionSelector): Preview {
        return Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .build()
            .apply {
                surfaceProvider = previewView.surfaceProvider
            }
    }

    /**
     * 画像解析設定を作成
     *
     * @param resolutionSelector 解像度選択の設定オブジェクト
     * @return 画像解析の設定オブジェクト（ImageAnalysis）
     */
    private fun createImageAnalysis(resolutionSelector: ResolutionSelector): ImageAnalysis {
        return ImageAnalysis.Builder()
            .setResolutionSelector(resolutionSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(workerExecutor, analyzer)
            }
    }

    /**
     * カメラをバインド
     *
     * @param provider カメラプロバイダ（ProcessCameraProvider）
     * @param preview プレビュー設定オブジェクト（Preview）
     * @param analysis 画像解析設定オブジェクト（ImageAnalysis）
     *
     * @return バインド操作の成功/失敗を示す結果（成功時はUnit、失敗時は例外が発生）
     */
    private fun bindCamera(provider: ProcessCameraProvider, preview: Preview, analysis: ImageAnalysis) {
        runCatching {
            provider.unbindAll() // 既存のバインディングを解除
            provider.bindToLifecycle(
                activity, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis
            )
        }.onFailure { exception ->
            // エラーハンドリング
            exception.printStackTrace()
        }
    }

    /**
     * 使用後のリソースを解放
     */
    private fun cleanUp() {
        workerExecutor.shutdown()
        scanner.close()
    }
}
