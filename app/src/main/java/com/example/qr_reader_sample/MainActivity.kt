package com.example.qr_reader_sample

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.qr_reader_sample.ui.theme.QrreadersampleTheme
import com.google.zxing.integration.android.IntentIntegrator

class MainActivity : ComponentActivity() {
    private val CAMERA_PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // カメラの権限が許可されているか確認
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//            != PackageManager.PERMISSION_GRANTED) {
//
//            // 許可されていなければ、許可をリクエスト
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE
//            )
//        }

        setContent {
            QrreadersampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        QrCodeReader(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(innerPadding) // Apply the innerPadding to the button
                        )
                    }
                }
            }
        }
    }

    // QRコードスキャナーを起動
    private fun startQrCodeScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setOrientationLocked(true)                         // 縦画面に固定
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE) // QRコードのみスキャン対象
        integrator.setPrompt("QRコードを読み取ってください。")             // プロンプトメッセージ
        integrator.setCameraId(0)                                     // 背面カメラ
        integrator.setBeepEnabled(true)                               // ビープ音を有効化
        integrator.setBarcodeImageEnabled(false)                      // スキャン結果画像を保存しない
        integrator.initiateScan()                                     // スキャナーを起動
    }

    // 権限リクエストの結果を処理
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 許可された場合
                Toast.makeText(this, "カメラの権限が許可されました", Toast.LENGTH_SHORT).show()
            } else {
                // メッセージボックス
                AlertDialog.Builder(this)
                    .setTitle("情報")
                    .setMessage("本アプリはカメラの権限が必須です。権限を付与してください。")
                    .setCancelable(false) // ユーザーがダイアログを閉じられないようにする
                    .setPositiveButton("OK") { _, _ ->
                        finish() // アクティビティを終了
                    }
                    .show()
            }
        }
    }

    // QRコードスキャン結果を受け取る
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Log.d("QRCode", "スキャンがキャンセルされました")
            } else {
                Log.d("QRCode", "スキャン結果: ${result.contents}")
                // メッセージボックス（AlertDialog）を表示
                AlertDialog.Builder(this)
                    .setTitle("QRコード読み取り結果")
                    .setMessage(result.contents)
                    .setPositiveButton("OK", null)
                    .show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun moveQrCodeScannerView() {
        val intent = Intent(this, QrCodeScannerActivity::class.java)
        startActivity(intent)  // QrCodeCaptureActivityに遷移
    }

    @Composable
    fun QrCodeReader(modifier: Modifier = Modifier) {
        Button(
            onClick = { moveQrCodeScannerView() },
            modifier = modifier
        ) {
            Text("QRコード読み取り")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QrCodeReaderPreview() {
    QrreadersampleTheme {
        Text("QRコード読み取り")
    }
}
