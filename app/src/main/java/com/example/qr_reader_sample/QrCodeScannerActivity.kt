package com.example.qr_reader_sample

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.qr_reader_sample.databinding.ActivityQrCodeScannerBinding
import com.google.mlkit.vision.barcode.common.Barcode

/**
 * QRコードスキャン画面のアクティビティ
 */
class QrCodeScannerActivity : ComponentActivity() {
    private lateinit var binding: ActivityQrCodeScannerBinding
    private lateinit var codeScanner: QrCodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestCameraPermission()
        initialize()
    }

    /**
     * カメラ権限の確認とリクエスト
     */
    private fun checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // 権限が許可されていない場合、リクエストを実行
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    /**
     * 権限リクエストの結果を処理
     *
     * @param requestCode リクエストコード
     * @param permissions リクエストした権限
     * @param grantResults 権限の許可結果
     */
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 権限が許可された場合、何もしない
            } else {
                // 権限が拒否された場合、警告ダイアログを表示
                AlertDialog.Builder(this)
                    .setTitle("情報")
                    .setMessage("カメラの権限が必須です。権限を付与してください。")
                    .setCancelable(false) // ユーザーがダイアログを閉じられないようにする
                    .setPositiveButton("OK") { _, _ ->
                        finish() // アクティビティを終了
                    }
                    .show()
            }
        }
    }

    /**
     * アクティビティの初期化処理
     */
    private fun initialize() {
        // 画面の初期化
        binding = ActivityQrCodeScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // スキャナーの初期化とスキャン開始
        codeScanner = QrCodeScanner(this, binding.previewView, ::onDetectCode)
        codeScanner.start()
    }

    /**
     * QRコードが検出された際に呼ばれるコールバック
     *
     * @param codes 検出されたQRコードのリスト
     */
    private fun onDetectCode(codes: List<Barcode>) {
        codes.forEach {
            Log.d("QRCode", "スキャン結果: ${it.rawValue}")
        }
    }

    companion object {
        // カメラ権限リクエスト用のリクエストコード
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }
}