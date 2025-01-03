package com.example.qr_reader_sample

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.common.Barcode

/**
 * QRコードスキャン画面のアクティビティ
 */
class QrCodeScannerActivity : ComponentActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var codeScanner: QrCodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestCameraPermission()
        initializeUI()
        initializeScanner()
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
     * UIを初期化
     */
    @SuppressLint("SetTextI18n")
    private fun initializeUI() {
        // 親のリニアレイアウトを作成
        val linearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        // 上部のテキストビュー (高さ200px)
        val textView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                200
            )
            text = "QRコードを\n読み取ってください。"
            textSize = 20f
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            gravity = Gravity.CENTER // TextView内の文字を上下中央に配置
        }

        // 下部のPreviewView (残りのスペース)
        previewView = PreviewView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f // 残りのスペースを占める
            )
        }

        // リニアレイアウトにビューを追加
        linearLayout.addView(textView)
        linearLayout.addView(previewView)

        // レイアウトをアクティビティに設定
        setContentView(linearLayout)
    }

    /**
     * スキャナーの初期化
     */
    private fun initializeScanner() {
        codeScanner = QrCodeScanner(this, previewView, ::onDetectCode)
        codeScanner.start()
    }

    /**
     * QRコードが検出された際に呼ばれるコールバック
     *
     * @param codes 検出されたQRコードのリスト
     */
    private fun onDetectCode(codes: List<Barcode>) {
        if (codes.isEmpty()) {
            return
        }

        // QRコードスキャンを一時停止
        codeScanner.stop()

        // 読み取り音を鳴らす
        playSound()

        // リストの最初の要素を処理
        val firstCode = codes.first()
        Log.d("QRCode", "スキャン結果: ${firstCode.rawValue}")

        // メッセージボックス（AlertDialog）を表示
        AlertDialog.Builder(this)
            .setTitle("読み取り結果")
            .setMessage(firstCode.rawValue)
            .setPositiveButton("OK") { _, _ ->
                // メッセージボックス確認後、QRコードスキャンを再開
                codeScanner.start()
            }
            .setCancelable(false) // ユーザーが外側をタップして閉じないようにする
            .show()
    }

    /**
     * 音を鳴らす
     */
    private fun playSound() {
        val mediaPlayer = MediaPlayer.create(this, R.raw.scan) // `beep_sound.mp3` を res/raw に配置
        mediaPlayer.setOnCompletionListener { it.release() } // 再生完了後リソースを解放
        mediaPlayer.start()
    }

    companion object {
        // カメラ権限リクエスト用のリクエストコード
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }
}
