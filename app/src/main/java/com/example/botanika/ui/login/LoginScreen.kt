package com.example.botanika.ui.login

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.botanika.R
import com.example.botanika.ui.animation.revealed
import com.example.botanika.ui.components.AuthIcon
import com.example.botanika.ui.components.AuthIcons
import com.example.botanika.ui.components.AuthTextField
import com.example.botanika.ui.components.BotanikaError
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
private const val CONTENT_STAGGER_MILLIS = 150L
private const val CONTENT_FADE_MILLIS = 800

private val LoginFlowerWidth = 150.dp
private val HeaderMinHeight = 300.dp
private const val HEADER_FRACTION = 0.38f
private val CloudBumpHeight = 40.dp

@Composable
internal fun SharedTransitionScope.LoginScreen(
    flower: FlowerRender,
    animatedVisibilityScope: AnimatedVisibilityScope,
    initialEmail: String = "",
    initialRemember: Boolean = false,
    serverError: String? = null,
    onLogin: (email: String, password: String, remember: Boolean) -> Unit,
    onForgotPassword: (email: String) -> Unit,
    onSignUp: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf(initialEmail) }
    var password by rememberSaveable { mutableStateOf("") }
    var remember by rememberSaveable { mutableStateOf(initialRemember) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }

    val name = remember { Animatable(0f) }
    val emailField = remember { Animatable(0f) }
    val passwordField = remember { Animatable(0f) }
    val options = remember { Animatable(0f) }
    val button = remember { Animatable(0f) }
    val footer = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(CONTENT_DELAY_MILLIS)
        val spec = tween<Float>(CONTENT_FADE_MILLIS, easing = EaseInOutSine)
        launch { name.animateTo(1f, spec) }
        launch { delay(CONTENT_STAGGER_MILLIS); emailField.animateTo(1f, spec) }
        launch { delay(CONTENT_STAGGER_MILLIS * 2); passwordField.animateTo(1f, spec) }
        launch { delay(CONTENT_STAGGER_MILLIS * 3); options.animateTo(1f, spec) }
        launch { delay(CONTENT_STAGGER_MILLIS * 4); button.animateTo(1f, spec) }
        launch { delay(CONTENT_STAGGER_MILLIS * 5); footer.animateTo(1f, spec) }
    }

    val focusManager = LocalFocusManager.current
    val emailError = when {
        !submitted -> null
        email.isBlank() -> stringResource(R.string.login_error_email_required)
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() ->
            stringResource(R.string.login_error_email_invalid)
        else -> null
    }
    val passwordError =
        if (submitted && password.isEmpty()) stringResource(R.string.login_error_password_required) else null

    fun submit() {
        submitted = true
        if (email.isNotBlank() &&
            android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() &&
            password.isNotEmpty()
        ) {
            focusManager.clearFocus()
            onLogin(email.trim(), password, remember)
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
                    width = LoginFlowerWidth
                )
                BotanikaName(progress = { name.value }, modifier = Modifier.padding(top = 8.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = sheetMinHeight)
                    .background(Color.White, CloudTopShape(CloudBumpHeight))
                    .navigationBarsPadding()
                    .padding(start = 40.dp, end = 40.dp, top = CloudBumpHeight + 36.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
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

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp)
                        .revealed(options),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RememberMe(checked = remember, onCheckedChange = { remember = it })
                    Text(
                        text = stringResource(R.string.login_forgot),
                        color = BotanikaGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onForgotPassword(email.trim()) }
                            .padding(vertical = 8.dp)
                    )
                }

                if (serverError != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = serverError,
                        color = BotanikaError,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                } else {
                    Spacer(Modifier.height(36.dp))
                }

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
                        text = stringResource(R.string.login_button),
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
                        text = stringResource(R.string.login_no_account),
                        color = BotanikaGreen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.login_sign_up),
                        color = BotanikaRose,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable(onClick = onSignUp)
                            .padding(vertical = 6.dp, horizontal = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RememberMe(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(if (checked) BotanikaRose else BotanikaRose.copy(alpha = 0.35f), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                AuthIcon(icon = AuthIcons.Check, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.login_remember),
            color = BotanikaRose,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
