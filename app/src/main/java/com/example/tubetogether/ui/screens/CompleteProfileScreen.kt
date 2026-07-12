package com.example.tubetogether.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tubetogether.auth.AuthManager
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteProfileScreen(
    onCompleteSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    var name by remember { mutableStateOf(AuthManager.getUserName()) }
    var username by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var expandedCountryMenu by remember { mutableStateOf(false) }

    val countries = listOf("العراق", "السعودية", "مصر", "الإمارات", "الكويت", "قطر", "البحرين", "عمان", "الأردن", "سوريا", "لبنان", "فلسطين", "المغرب", "الجزائر", "تونس", "ليبيا", "السودان", "اليمن", "أخرى")

    val bgColor = Color(0xFF0F0F0F)
    val cardColor = Color(0xFF141414)
    val primaryRed = Color(0xFFE5132A)
    val primaryGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFC1001F), Color(0xFFE5132A), Color(0xFFFF1744))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        // Top Glow
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-60).dp)
                .size(300.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryRed.copy(alpha = 0.22f), Color.Transparent),
                        radius = 400f
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(40.dp))
                .background(cardColor)
                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(40.dp))
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "خطوة أخيرة!",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "أكمل ملفك الشخصي لنقدم لك أفضل تجربة مخصصة لك.",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            InputField(
                label = "الاسم الكامل",
                value = name,
                onValueChange = { name = it },
                icon = Icons.Default.Person,
                placeholder = "الاسم"
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            InputField(
                label = "اسم المستخدم (Username)",
                value = username,
                onValueChange = { 
                    // Allow only english letters, numbers, and underscores
                    val filtered = it.filter { char -> char.isLetterOrDigit() || char == '_' }
                    username = filtered.lowercase() 
                },
                icon = Icons.Default.Person,
                placeholder = "مثال: ali_1990"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date of Birth Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("تاريخ الميلاد", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = dob,
                    onValueChange = { },
                    readOnly = true,
                    placeholder = { Text("اختر تاريخ ميلادك", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val calendar = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    dob = "$year-${month + 1}-$day"
                                },
                                calendar.get(Calendar.YEAR) - 18,
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledContainerColor = Color(0xFF1A1A1A),
                        disabledBorderColor = Color.White.copy(alpha = 0.08f),
                        disabledTextColor = Color.White
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Country Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedCountryMenu,
                onExpandedChange = { expandedCountryMenu = !expandedCountryMenu }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("البلد", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = country,
                        onValueChange = { },
                        readOnly = true,
                        placeholder = { Text("اختر بلدك", color = Color.Gray, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp)) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1A1A1A),
                            unfocusedContainerColor = Color(0xFF1A1A1A),
                            focusedBorderColor = Color(0xFFE5132A).copy(alpha = 0.5f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }
                
                ExposedDropdownMenu(
                    expanded = expandedCountryMenu,
                    onDismissRequest = { expandedCountryMenu = false },
                    modifier = Modifier.background(Color(0xFF1A1A1A))
                ) {
                    countries.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption, color = Color.White) },
                            onClick = {
                                country = selectionOption
                                expandedCountryMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (authState is AuthState.Error) {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Submit Button
            Button(
                onClick = {
                    if (name.isBlank() || username.isBlank() || dob.isBlank() || country.isBlank()) {
                        Toast.makeText(context, "الرجاء تعبئة جميع الحقول", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.completeProfile(name, username, dob, country, onCompleteSuccess)
                    }
                },
                enabled = authState !is AuthState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(primaryGradient, RoundedCornerShape(12.dp))
                    .shadow(8.dp, spotColor = primaryRed.copy(alpha = 0.4f)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "حفظ ومتابعة",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
