package Presentation.Home
import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegAuthScreen(
    viewModel: RegAuthViewModel = viewModel(),
    onAuthSuccess: () -> Unit
) {
    val name=viewModel.name.collectAsState()
    val email=viewModel.email.collectAsState()
    val password=viewModel.password.collectAsState()
    val isReg=viewModel.isReg.collectAsState()
    val successAuth = viewModel.successAuth.collectAsState()

    LaunchedEffect(successAuth.value) {
        if (successAuth.value == RegStates.OK) {
            onAuthSuccess()
        }
    }

    Scaffold(containerColor = Color(0xFF1C1B1B)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 22.dp,
                    top = 60.dp,
                    end = 22.dp,
                    bottom = 32.dp
                )
                .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.clickable(onClick = { viewModel.changeRegState() }),
                text = if (isReg.value) "Регистрация" else {
                    "Вход"
                },
                color = Color.White
            )
            Spacer(modifier = Modifier.height(170.dp))
            if (isReg.value) {
                OutlinedTextField(
                    value = name.value,
                    onValueChange = { viewModel.onSubmitName(it) },
                    label = { Text("Имя") },
                    placeholder = { Text("Введите имя...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2FA5E5),
                        focusedLabelColor = Color.White,
                        cursorColor = Color(0xFF2FA5E5),
                        focusedTextColor = Color.White,
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email.value,
                onValueChange = { viewModel.onSubmitEmail(it) },
                label = { Text("Email") },
                placeholder = { Text("Введите email...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2FA5E5),
                    focusedLabelColor = Color.White,
                    cursorColor = Color(0xFF2FA5E5),
                    focusedTextColor = Color.White,
                   )
                )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password.value,
                onValueChange = { viewModel.onSubmitPassword(it) },
                label = { Text("Пароль") },
                placeholder = { Text("Введите пароль...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2FA5E5),
                    focusedLabelColor = Color.White,
                    cursorColor = Color(0xFF2FA5E5),
                    focusedTextColor = Color.White,
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                modifier = Modifier.clickable(onClick = { viewModel.changeRegState() }),
                text = if (isReg.value) "Уже есть аккаунт?" else {
                    "Еще нет аккаунта?"
                },
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.auth()
                },
                modifier = Modifier.width(250.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1C92D2)
                )
            ) {
                Text(if (isReg.value) "Зарегистрироваться" else "Войти")
            }

            Text(
                color = Color.White,
                text = when (successAuth.value) {
                    RegStates.NONE -> ""
                    RegStates.WRONGPASSWORD -> "Неправильный пароль пользователя"
                    RegStates.USERNOTEXIST -> "Пользователь не существует"
                    RegStates.SERVERERROR -> "Ошибка на сервере"
                    RegStates.USEREXISTS -> "Пользователь уже существует"
                    else -> ""
                }
            )

        }
    }
}