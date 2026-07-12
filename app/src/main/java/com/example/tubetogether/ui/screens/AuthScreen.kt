package com.example.tubetogether.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tubetogether.R

import androidx.lifecycle.viewmodel.compose.viewModel

enum class AuthMode { LOGIN, SIGNUP, FORGOT, FORGOT_SENT }

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()
    var mode by remember { mutableStateOf(AuthMode.LOGIN) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var resetEmail by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

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
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Logo
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_round),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(28))
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            Crossfade(targetState = mode, label = "Mode Switch") { currentMode ->
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    when (currentMode) {
                        AuthMode.FORGOT -> ForgotPasswordView(
                            email = resetEmail,
                            onEmailChange = { resetEmail = it },
                            onBack = { mode = AuthMode.LOGIN },
                            onSubmit = { if (resetEmail.isNotBlank()) mode = AuthMode.FORGOT_SENT },
                            primaryGradient = primaryGradient
                        )
                        AuthMode.FORGOT_SENT -> ForgotSentView(
                            email = resetEmail,
                            onBack = { mode = AuthMode.LOGIN; resetEmail = "" },
                            primaryGradient = primaryGradient
                        )
                        else -> {
                            // Tabs
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .padding(4.dp)
                            ) {
                                TabButton(
                                    text = "تسجيل الدخول",
                                    isSelected = currentMode == AuthMode.LOGIN,
                                    primaryGradient = primaryGradient,
                                    modifier = Modifier.weight(1f)
                                ) { mode = AuthMode.LOGIN }
                                TabButton(
                                    text = "إنشاء حساب",
                                    isSelected = currentMode == AuthMode.SIGNUP,
                                    primaryGradient = primaryGradient,
                                    modifier = Modifier.weight(1f)
                                ) { mode = AuthMode.SIGNUP }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))

                            if (currentMode == AuthMode.SIGNUP) {
                                InputField(
                                    label = "الاسم الكامل",
                                    value = name,
                                    onValueChange = { name = it },
                                    icon = Icons.Default.Person,
                                    placeholder = "مصفى ماجد"
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            InputField(
                                label = "البريد الإلكتروني",
                                value = email,
                                onValueChange = { email = it },
                                icon = Icons.Default.Email,
                                placeholder = "example@email.com",
                                keyboardType = KeyboardType.Email
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            InputField(
                                label = "كلمة المرور",
                                value = password,
                                onValueChange = { password = it },
                                icon = Icons.Default.Lock,
                                placeholder = "••••••••",
                                isPassword = true,
                                showPassword = showPassword,
                                onTogglePassword = { showPassword = !showPassword }
                            )

                            if (currentMode == AuthMode.SIGNUP) {
                                Spacer(modifier = Modifier.height(12.dp))
                                InputField(
                                    label = "تأكيد كلمة المرور",
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it },
                                    icon = Icons.Default.CheckCircle,
                                    placeholder = "••••••••",
                                    isPassword = true,
                                    showPassword = showConfirm,
                                    onTogglePassword = { showConfirm = !showConfirm }
                                )
                            }

                            if (currentMode == AuthMode.LOGIN) {
                                Text(
                                    text = "نسيت كلمة المرور؟",
                                    color = primaryRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) { mode = AuthMode.FORGOT },
                                    textAlign = TextAlign.End
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

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
                                    if (currentMode == AuthMode.LOGIN) {
                                        viewModel.login(email, password, onLoginSuccess)
                                    } else {
                                        viewModel.register(name, email, confirmPassword, onLoginSuccess)
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
                                        text = if (currentMode == AuthMode.LOGIN) "تسجيل الدخول" else "إنشاء الحساب",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Divider
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.08f))
                                Text("أو", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp))
                                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.08f))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Google Button
                            OutlinedButton(
                                onClick = onLoginSuccess,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color(0xFF1A1A1A),
                                    contentColor = Color(0xFFF5F5F5)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = "Google", tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("الاستمرار عبر Google", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ForgotPasswordView(
    email: String,
    onEmailChange: (String) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit,
    primaryGradient: Brush
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onBack() }.padding(bottom = 24.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Gray, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("رجوع", color = Color.Gray, fontSize = 14.sp)
        }

        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Color(0xFFE5132A).copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE5132A).copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFFE5132A), modifier = Modifier.size(28.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("نسيت كلمة المرور؟", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("أدخل بريدك الإلكتروني وسنرسل لك رابط لإعادة تعيين كلمة المرور.", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(24.dp))
        InputField(
            label = "البريد الإلكتروني",
            value = email,
            onValueChange = onEmailChange,
            icon = Icons.Default.Email,
            placeholder = "example@email.com",
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(if (email.isNotBlank()) primaryGradient else Brush.linearGradient(listOf(Color(0xFF2A2A2A), Color(0xFF2A2A2A))), RoundedCornerShape(12.dp))
                .shadow(if (email.isNotBlank()) 8.dp else 0.dp, spotColor = Color(0xFFE5132A).copy(alpha = 0.4f)),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("إرسال رابط الإعادة", color = if (email.isNotBlank()) Color.White else Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ForgotSentView(
    email: String,
    onBack: () -> Unit,
    primaryGradient: Brush
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFFE5132A).copy(alpha = 0.12f), CircleShape)
                .border(2.dp, Color(0xFFE5132A).copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFE5132A), modifier = Modifier.size(36.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("تم الإرسال!", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "أرسلنا رابط إعادة التعيين إلى\n$email\nتفقد بريدك الإلكتروني.",
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(primaryGradient, RoundedCornerShape(12.dp))
                .shadow(8.dp, spotColor = Color(0xFFE5132A).copy(alpha = 0.4f)),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("العودة لتسجيل الدخول", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    primaryGradient: Brush,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) primaryGradient else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (isSelected) Color.White else Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePassword: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray, fontSize = 14.sp) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp)) },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.Lock else Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
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
}
