package kr.puze.easyfarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android Compose 를 이용한 View 구성
        setContent {
            // 화면을 최대로, 배경색상을 #FFFFFF, #3399FF 의 그라데이션으로 설정
            Box(modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(android.graphics.Color.parseColor("#FFFFFF")),
                            Color(android.graphics.Color.parseColor("#B2EBF4"))
                        )
                    )
                )
            ){
                // 화면의 중심부에 Row 를 구성
                Row(modifier = Modifier
                    // Set image size to 40 dp
                    .align(Alignment.Center)) {
                    // Spacer, Column, Spacer 를 1:1:1 비율로 화면에 구성
                    Spacer(modifier = Modifier.weight(1f, true))
                    Column(
                        modifier = Modifier.align(Alignment.CenterVertically).weight(1f, true),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Column 안에 앱 이름과 이미지 구성
                        Text(text = "Easy Farm", color = Color(android.graphics.Color.parseColor("#000000")))
                        Image(
                            painter = painterResource(R.drawable.easyfarm),
                            contentDescription = "Contact profile picture",
                            // Clip image to be shaped as a circle
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f, true))
                }
            }
        }

        // 2초 뒤에 스플래시 화면에서 메인 화면으로 넘어가도록함
        Handler().postDelayed({
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
        }, 2000)
    }
}