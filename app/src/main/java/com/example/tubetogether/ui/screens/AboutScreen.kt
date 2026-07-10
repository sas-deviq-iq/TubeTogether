package com.example.tubetogether.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "About",
            tint = Color(0xFFE50914),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Welcome to TubeTogether!",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Main description
        Text(
            text = buildAnnotatedString {
                append("This application was created by ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFFE50914))) {
                    append("SAS DEV")
                }
                append(", a team of university students from ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                    append("Iraq")
                }
                append(" who are passionate about software development and building useful digital products.\n\n")

                append("Our goal is to provide a simple, fast, and enjoyable movie streaming experience without intrusive advertisements or unnecessary restrictions. This project is ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                    append("non-profit")
                }
                append(" and was built as a learning experience and a contribution to the developer community.\n\n")

                append("We believe that technology should be accessible to everyone, and we hope this project offers an alternative experience that puts users first through simplicity, performance, and respect for their time.")
            },
            color = Color.LightGray,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Important Notice Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "⚠️ Important Notice",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                val notices = listOf(
                    "This project is non-profit.",
                    "We do not display annoying advertisements.",
                    "This project is intended for educational and portfolio purposes.",
                    "We respect intellectual property rights. Content ownership belongs to its respective owners."
                )

                notices.forEach { notice ->
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(text = "• ", color = Color(0xFFE50914), fontWeight = FontWeight.Bold)
                        Text(text = notice, color = Color.LightGray, fontSize = 15.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Divider(color = Color.DarkGray)
        Spacer(modifier = Modifier.height(32.dp))

        // About Us
        Text(
            text = "About Us",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFFE50914))) {
                    append("SAS DEV")
                }
                append(" is an independent team of university students from Iraq focused on developing modern mobile applications and software solutions.")
            },
            color = Color.LightGray,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text(text = "📍", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Iraq 🇮🇶", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.Start)
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/sas_dev.iq"))
                    context.startActivity(intent)
                }
                .padding(vertical = 8.dp)
        ) {
            Text(text = "📸", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Instagram: ",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "@sas_dev.iq",
                color = Color(0xFFE50914),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline
            )
        }

        Spacer(modifier = Modifier.height(48.dp)) // Bottom padding for navigation bar
    }
}
