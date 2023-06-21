package kr.puze.easyfarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : AppCompatActivity() {
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
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ActionBar 구성
                    Box(
                        modifier = Modifier.fillMaxWidth().background(Color(android.graphics.Color.parseColor("#B2EBF4"))).padding(16.dp)
                    ){
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = "Easy Farm",
                            color = Color(android.graphics.Color.parseColor("#FFFFFF")),
                            fontSize = 36.sp
                        )
                    }

                    // 가로 세로 1:1 비율의 이미지 세팅
                    Box(
                        modifier = Modifier
                            .padding(30.dp)
                            .weight(2f, true)
                            .clip(CircleShape)
                            .fillMaxSize()
                            .aspectRatio(1f)
                            .border(
                                width = 7.dp,
                                color = Color(android.graphics.Color.parseColor("#FFFFFF")),
                                shape = CircleShape
                            )
                    ){
                        Image(
                            modifier = Modifier.align(Alignment.Center),
                            painter = painterResource(R.drawable.easyfarm),
                            contentDescription = "Contact profile picture",
                            // Clip image to be shaped as a circle
                        )
                    }

                    // 위 1:1 비율의 이미지를 제외한 모든 영역을 감싸는 Row 구성
                    Row(modifier = Modifier
                        .weight(1f, true)
                        .background(Color.White)
                    ) {
                        // "내 식물" 과 "식물 정보" 버튼을 1:1 로 구성
                        Box(
                            modifier = Modifier.fillMaxWidth().background(Color(android.graphics.Color.parseColor("#F1FFFF")))
                                .padding(16.dp)
                                .weight(1f, true)
                                .clip(CircleShape)
                                .fillMaxSize()
                                .aspectRatio(1f)
                                .border(
                                  width = 5.dp,
                                  color = Color(android.graphics.Color.parseColor("#3DB7CC")),
                                  shape = CircleShape
                                )
                                .clickable {
                                    goActivity(PlantActivity())
                                }
                        ) {
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                text = "내 식물",
                                color = Color(android.graphics.Color.parseColor("#3DB7CC")),
                                fontSize = 28.sp
                            )
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth().background(Color(android.graphics.Color.parseColor("#F1FFFF")))
                                .padding(16.dp)
                                .weight(1f, true)
                                .clip(CircleShape)
                                .fillMaxSize()
                                .aspectRatio(1f)
                                .border(
                                    width = 5.dp,
                                    color = Color(android.graphics.Color.parseColor("#3DB7CC")),
                                    shape = CircleShape
                                )
                                .clickable {
                                    goActivity(InfoActivity())
                                }
                        ) {
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                text = "식물 정보",
                                color = Color(android.graphics.Color.parseColor("#3DB7CC")),
                                fontSize = 28.sp
                            )
                        }
                    }
                }
            }
        }
    }

    fun goActivity(activity: AppCompatActivity){

        val intent = Intent(this@MainActivity, activity::class.java)
        startActivity(intent)
    }
}