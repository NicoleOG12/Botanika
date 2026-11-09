package com.example.botanika.ui.register

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.botanika.R
import com.example.botanika.ui.animation.revealed
import com.example.botanika.ui.components.AuthIcon
import com.example.botanika.ui.components.AuthIcons
import com.example.botanika.ui.components.AuthTextField
import com.example.botanika.ui.components.CloudTopShape
import com.example.botanika.ui.logo.BotanikaFlower
import com.example.botanika.ui.logo.BotanikaName
import com.example.botanika.ui.logo.FlowerRender
import com.example.botanika.ui.logo.sharedLogoModifier
import com.example.botanika.ui.theme.BotanikaGreen
import com.example.botanika.ui.theme.BotanikaPink
import com.example.botanika.ui.theme.BotanikaRose
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val CONTENT_DELAY_MILLIS = 500L
private const val CONTENT_STAGGER_MILLIS = 120L
private const val CONTENT_FADE_MILLIS = 800

private val RegisterFlowerWidth = 150.dp
private val HeaderMinHeight = 260.dp
private const val HEADER_FRACTION = 0.32f
private val CloudBumpHeight = 40.dp

@Composable
internal fun SharedTransitionScope.RegisterScreen(
    flower: FlowerRender,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onRegister: (name: String, email: String, password: String) -> Unit,
    onLogin: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }

    val logoName = remember { Animatable(0f) }
    val nameField = remember { Animatable(0f) }
    val emailField = remember { Animatable(0f) }
    val passwordField = remember { Animatable(0f) }
    val confirmPasswordField = remember { Animatable(0f) }
    val button = remember { Animatable(0f) }
    val footer = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(CONTENT_DELAY_MILLIS)
        val spec = tween<Float>(CONTENT_FADE_MILLIS, easing = EaseInOutSine)
        launch { logoName.animateTo(1f, spec) }
        launch { delay(CONTENT_STAGGER_MILLIS); nameField.animateTo(1f, spec) }
        launch { delay(CONTENT_STAGGER_MILLIS * 2); emailField.animateTo(1f, spec) }
        launch { delay(CONTENT_STAGGER_MILLIS * 3); passwordField.animateTo(1f, spec) }
        launch { delay(CONTENT_STAGGER_MILLIS * 4); confirmPasswordField.animateTo(1f, spec) }
        launch { delay(CONTENT_STAGGER_MILLIS * 5); button.animateTo(1f, spec) }
        launch { delay(CONTENT_STAGGER_MILLIS * 6); footer.animateTo(1f, spec) }
    }

    val focusManager = LocalFocusManager.current

    val nameError = when {
        !submitted -> null
        name.isBlank() -> stringResource(R.string.register_error_name_required)
        else -> null
    }

    val emailError = when {
        !submitted -> null
        email.isBlank() -> stringResource(R.string.login_error_email_required)
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() ->
            stringResource(R.string.login_error_email_invalid)
        else -> null
    }

    val passwordError = when {
        !submitted -> null
        password.isEmpty() -> stringResource(R.string.login_error_password_required)
        else -> null
    }

    val confirmPasswordError = when {
        !submitted -> null
        confirmPassword.isEmpty() -> stringResource(R.string.register_error_confirm_password_required)
        confirmPassword != password -> stringResource(R.string.register_error_password_mismatch)
        else -> null
    }

    fun submit() {
        submitted = true
        val validName = name.isNotBlank()
        val validEmail = email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
        val validPassword = password.isNotEmpty()
        val validConfirm = confirmPassword.isNotEmpty() && confirmPassword == password

        if (validName && validEmail && validPassword && validConfirm) {
            focusManager.clearFocus()
            onRegister(name.trim(), email.trim(), password)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(BotanikaPink)
    ) {
        val headerHeight = (maxHeight * HEADER_FRACTION).coerceAtLeast(HeaderMinHeight)
        val sheetMinHeight = (maxHeight - headerHeight).coerceAtLeast(0.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
                    .statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                BotanikaFlower(
                    flower = flower,
                    progress = { 1f },
                    modifier = sharedLogoModifier(animatedVisibilityScope),
                    width = RegisterFlowerWidth
                )
                BotanikaName(progress = { logoName.value }, modifier = Modifier.padding(top = 8.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = sheetMinHeight)
                    .background(Color.White, CloudTopShape(CloudBumpHeight))
                    .navigationBarsPadding()
                    .padding(start = 40.dp, end = 40.dp, top = CloudBumpHeight + 28.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AuthTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = stringResource(R.string.register_name),
                    leadingIcon = AuthIcons.Person,
                    errorMessage = nameError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.revealed(nameField)
                )

                Spacer(Modifier.height(16.dp))

                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = stringResource(R.string.login_email),
                    leadingIcon = AuthIcons.Email,
                    errorMessage = emailError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.revealed(emailField)
                )

                Spacer(Modifier.height(16.dp))

                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = stringResource(R.string.login_password),
                    leadingIcon = AuthIcons.Lock,
                    errorMessage = passwordError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailing = {
                        AuthIcon(
                            icon = if (passwordVisible) AuthIcons.VisibilityOff else AuthIcons.Visibility,
                            tint = BotanikaGreen,
                            contentDescription = stringResource(
                                if (passwordVisible) R.string.login_hide_password else R.string.login_show_password
                            ),
                            modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                        )
                    },
                    modifier = Modifier.revealed(passwordField)
                )

                Spacer(Modifier.height(16.dp))

                AuthTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = stringResource(R.string.register_confirm_password),
                    leadingIcon = AuthIcons.Lock,
                    errorMessage = confirmPasswordError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    visualTransformation = if (confirmPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailing = {
                        AuthIcon(
                            icon = if (confirmPasswordVisible) AuthIcons.VisibilityOff else AuthIcons.Visibility,
                            tint = BotanikaGreen,
                            contentDescription = stringResource(
                                if (confirmPasswordVisible) R.string.login_hide_password else R.string.login_show_password
                            ),
                            modifier = Modifier.clickable { confirmPasswordVisible = !confirmPasswordVisible }
                        )
                    },
                    modifier = Modifier.revealed(confirmPasswordField)
                )

                Spacer(Modifier.height(28.dp))

                Button(
                    onClick = ::submit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .revealed(button),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BotanikaGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = stringResource(R.string.register_button),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(20.dp))

                Column(
                    modifier = Modifier.revealed(footer),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.register_already_have_account),
                        color = BotanikaGreen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.register_sign_in),
                        color = BotanikaRose,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable(onClick = onLogin)
                            .padding(vertical = 6.dp, horizontal = 12.dp)
                    )
                }
            }
        }
    }
}
