package com.example.qr_reader_sample

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * バーコードを解析するためのクラス。
 *
 * このクラスは、カメラフレームのデータを解析し、バーコードの結果を
 * 指定されたコールバック関数に渡します。
 *
 * @param scanner バーコードを解析するためのBarcodeScannerインスタンス
 * @param callback 検出したバーコードリストを受け取るコールバック関数
 */
class CodeAnalyzer(
    private val scanner: BarcodeScanner,
    private val callback: (List<Barcode>) -> Unit
) : ImageAnalysis.Analyzer {

    /**
     * QRコードの解析を行う
     *
     * @param imageProxy カメラフレームのデータを保持するImageProxyオブジェクト
     *
     * @return 処理結果は直接返さず、成功時にはコールバック関数で検出されたコードを返す
     */
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        // 画像データの取得
        val image = imageProxy.image

        // 画像が取得できない場合はプロキシを閉じる
        if (image == null) {
            imageProxy.close()
            return
        }

        // 取得した画像データをML Kitに渡すためのInputImageオブジェクトに変換
        val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)

        scanner.process(inputImage)
            // コードが検出された場合、検出されたコードをコールバックで返す
            .addOnSuccessListener { callback(it) }
            // 処理が完了したらImageProxyを閉じてリソースを解放
            .addOnCompleteListener { imageProxy.close() }
    }
}
