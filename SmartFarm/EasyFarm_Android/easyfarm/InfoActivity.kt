package kr.puze.easyfarm

import android.graphics.fonts.FontFamily
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class InfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android Compose 를 이용한 View 구성
        setContent {
            // 화면을 최대로, 배경색상을 #FFFFFF, #3399FF 의 그라데이션으로 설정
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(android.graphics.Color.parseColor("#B2EBF4")),
                                Color(android.graphics.Color.parseColor("#FFFFFF"))
                            )
                        )
                    )
                    .fillMaxSize()
            ){
                // Column 안의 View 를 Column 의 중심으로 설정
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ActionBar 구성
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(android.graphics.Color.parseColor("#B2EBF4")),)
                            .padding(16.dp)
                    ){
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = "Easy Farm",
                            color = Color(android.graphics.Color.parseColor("#FFFFFF")),
                            fontSize = 36.sp
                        )
                    }

                    // 식물 관리 텍스트
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = "식물별 적정 온도 및 조도",
                        color = Color(android.graphics.Color.parseColor("#56767C")),
                        fontSize = 20.sp
                    )
                    // 식물 관리에 관한 표 이미지
                    Image(
                        modifier = Modifier
                            .weight(1f, true)
                            .padding(10.dp)
                            .fillMaxSize(),
                        painter = painterResource(R.drawable.info_chart),
                        contentDescription = "Contact profile picture",
                        // Clip image to be shaped as a circle
                    )

                    // 다육식물 텍스트
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = "계절별 평균습도",
                        color = Color(android.graphics.Color.parseColor("#56767C")),
                        fontSize = 20.sp
                    )

                    Image(
                        modifier = Modifier
                            .weight(1f, true)
                            .padding(16.dp)
                            .fillMaxSize(),
                        painter = painterResource(R.drawable.info_plant),
                        contentDescription = "Contact profile picture",
                        // Clip image to be shaped as a circle
                    )

                    // 다육식물 텍스트
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = "식물별 적정습도",
                        color = Color(android.graphics.Color.parseColor("#56767C")),
                        fontSize = 20.sp
                    )

                    Image(
                        modifier = Modifier
                            .weight(1f, true)
                            .padding(8.dp)
                            .fillMaxSize(),
                        painter = painterResource(R.drawable.info_hum),
                        contentDescription = "Contact profile picture",
                        // Clip image to be shaped as a circle
                    )


                }
            }
        }
    }
}