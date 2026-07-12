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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tubetogether.api.ImageProxy
import com.example.tubetogether.api.auth.UserProfileDto
import java.util.Calendar

private val countries = listOf(
    "العراق", "السعودية", "مصر", "الإمارات", "الكويت", "قطر", "البحرين", "عمان",
    "الأردن", "سوريا", "لبنان", "فلسطين", "المغرب", "الجزائر", "تونس", "ليبيا",
    "السودان", "اليمن", "أخرى"
)

private val bgColor = Color(0xFF0F0F0F)
private val cardColor = Color(0xFF141414)
private val primaryRed = Color(0xFFE5132A)
private val primaryGradient = Brush.linearGradient(colors = listOf(Color(0xFFC1001F), Color(0xFFE5132A), Color(0xFFFF1744)))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val context = LocalContext.current

    var isEditing by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-60).dp)
                .size(300.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryRed.copy(alpha = 0.18f), Color.Transparent),
                        radius = 400f
                    ),
                    shape = CircleShape
                )
        )

        when (val current = state) {
            is ProfileState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryRed)
                }
            }
            is ProfileState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(current.message, color = Color.White, fontSize = 16.sp)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadProfile() },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryRed)
                        ) {
                            Text("إعادة المحاولة")
                        }
                    }
                }
            }
            is ProfileState.Success -> {
                ProfileContent(
                    profile = current.profile,
                    isEditing = isEditing,
                    isSaving = isSaving,
                    onEditToggle = { isEditing = !isEditing },
                    onSave = { name, username, dob, country ->
                        viewModel.updateProfile(
                            name, username, dob, country,
                            onSuccess = {
                                isEditing = false
                                Toast.makeText(context, "تم حفظ التغييرات", Toast.LENGTH_SHORT).show()
                            },
                            onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
                        )
                    },
                    onLogoutRequest = { showLogoutConfirm = true }
                )
            }
        }

        if (showLogoutConfirm) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirm = false },
                containerColor = cardColor,
                title = { Text("تسجيل الخروج", color = Color.White) },
                text = { Text("هل أنت متأكد أنك تريد تسجيل الخروج؟", color = Color.LightGray) },
                confirmButton = {
                    TextButton(onClick = {
                        showLogoutConfirm = false
                        viewModel.logout()
                        onLogout()
                    }) {
                        Text("تسجيل الخروج", color = primaryRed, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutConfirm = false }) {
                        Text("إلغاء", color = Color.Gray)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    profile: UserProfileDto,
    isEditing: Boolean,
    isSaving: Boolean,
    onEditToggle: () -> Unit,
    onSave: (name: String, username: String, dob: String, country: String) -> Unit,
    onLogoutRequest: () -> Unit
) {
    var name by remember(profile, isEditing) { mutableStateOf(profile.name ?: "") }
    var username by remember(profile, isEditing) { mutableStateOf(profile.username ?: "") }
    var dob by remember(profile, isEditing) { mutableStateOf(profile.dob ?: "") }
    var country by remember(profile, isEditing) { mutableStateOf(profile.country ?: "") }
    var expandedCountryMenu by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onEditToggle) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                    contentDescription = if (isEditing) "إلغاء" else "تعديل",
                    tint = Color.White
                )
            }
        }

        // Avatar
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(primaryGradient),
            contentAlignment = Alignment.Center
        ) {
            if (!profile.avatar.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageProxy.getProxiedUrl(profile.avatar),
                    contentDescription = "الصورة الشخصية",
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                val initial = (profile.name ?: profile.email).firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                Text(initial, color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
            }
        }

        Spacer(Modifier.height(16.dp))

        if (!isEditing) {
            Text(profile.name ?: "", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            if (!profile.username.isNullOrEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text("@${profile.username}", color = Color.Gray, fontSize = 14.sp)
            }

            Spacer(Modifier.height(32.dp))

            ProfileInfoCard(profile)
        } else {
            Spacer(Modifier.height(16.dp))

            InputField(
                label = "الاسم الكامل",
                value = name,
                onValueChange = { name = it },
                icon = Icons.Default.Person,
                placeholder = "الاسم"
            )
            Spacer(Modifier.height(16.dp))

            InputField(
                label = "اسم المستخدم (Username)",
                value = username,
                onValueChange = { input ->
                    username = input.filter { it.isLetterOrDigit() || it == '_' }.lowercase()
                },
                icon = Icons.Default.Person,
                placeholder = "مثال: ali_1990"
            )
            Spacer(Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("تاريخ الميلاد", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
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
                                { _, year, month, day -> dob = "$year-${month + 1}-$day" },
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
            Spacer(Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = expandedCountryMenu,
                onExpandedChange = { expandedCountryMenu = !expandedCountryMenu }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("البلد", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = country,
                        onValueChange = { },
                        readOnly = true,
                        placeholder = { Text("اختر بلدك", color = Color.Gray, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp)) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1A1A1A),
                            unfocusedContainerColor = Color(0xFF1A1A1A),
                            focusedBorderColor = primaryRed.copy(alpha = 0.5f),
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
                    countries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = Color.White) },
                            onClick = {
                                country = option
                                expandedCountryMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isBlank() || username.isBlank() || dob.isBlank() || country.isBlank()) {
                        Toast.makeText(context, "الرجاء تعبئة جميع الحقول", Toast.LENGTH_SHORT).show()
                    } else {
                        onSave(name, username, dob, country)
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(primaryGradient, RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("حفظ التغييرات", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(32.dp))
        }

        if (!isEditing) {
            Spacer(Modifier.weight(1f, fill = false))
            Spacer(Modifier.height(32.dp))

            OutlinedButton(
                onClick = onLogoutRequest,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryRed.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryRed)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("تسجيل الخروج", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun ProfileInfoCard(profile: UserProfileDto) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            ProfileInfoRow(Icons.Default.Email, "البريد الإلكتروني", profile.email)
            ProfileInfoDivider()
            ProfileInfoRow(Icons.Default.Person, "اسم المستخدم", profile.username?.let { "@$it" } ?: "غير محدد")
            ProfileInfoDivider()
            ProfileInfoRow(Icons.Default.DateRange, "تاريخ الميلاد", profile.dob ?: "غير محدد")
            ProfileInfoDivider()
            ProfileInfoRow(Icons.Default.LocationOn, "البلد", profile.country ?: "غير محدد")
        }
    }
}

@Composable
private fun ProfileInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = primaryRed, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, color = Color.Gray, fontSize = 12.sp)
            Spacer(Modifier.height(2.dp))
            Text(value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ProfileInfoDivider() {
    Divider(color = Color.White.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 20.dp))
}
