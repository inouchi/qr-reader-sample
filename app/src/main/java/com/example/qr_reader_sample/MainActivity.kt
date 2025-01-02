package com.example.qr_reader_sample

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qr_reader_sample.ui.theme.QrreadersampleTheme

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
                                .height(50.dp)
                                .padding(innerPadding) // Apply the innerPadding to the button
                        )
                    }
                }
            }
        }
    }

    private fun moveQrCodeScannerView() {
        val intent = Intent(this, QrCodeScannerActivity::class.java)
        startActivity(intent)  // QrCodeScannerActivityに遷移
    }

    @Composable
    fun QrCodeReader(modifier: Modifier = Modifier) {
        Button(
            onClick = { moveQrCodeScannerView() },
            modifier = modifier
        ) {
            Text("QRコード読み取り", fontSize = 20.sp)
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
