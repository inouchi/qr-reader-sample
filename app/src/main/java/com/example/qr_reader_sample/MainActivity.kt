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
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.qr_reader_sample.ui.theme.QrreadersampleTheme
import com.google.zxing.integration.android.IntentIntegrator

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    // カメラの権限を確認してスキャナーを起動
    private fun checkAndRequestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // 権限が許可されている場合、スキャナーを起動
                startQrCodeScanner()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                // 権限が拒否されている場合、説明を表示
                Toast.makeText(this, "カメラの権限が必要です", Toast.LENGTH_SHORT).show()
            }

            else -> {
                // 権限をリクエスト
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // 権限リクエストのコールバック
    private val requestPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted ->
        if (isGranted) {
            startQrCodeScanner()
        } else {
            Toast.makeText(this, "カメラの権限が必要です", Toast.LENGTH_SHORT).show()
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

    @Composable
    fun QrCodeReader(modifier: Modifier = Modifier) {
        Button(
            onClick = { checkAndRequestCameraPermission() },
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
