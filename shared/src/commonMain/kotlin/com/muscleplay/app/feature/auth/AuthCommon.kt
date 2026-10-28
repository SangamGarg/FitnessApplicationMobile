package com.muscleplay.app.feature.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

private val OrangePrimary = Color(0xFFFF5722)
internal val AuthErrorRed = Color(0xFFFF5252)
internal val AuthSuccessGreen = Color(0xFF4CAF50)

/** One line of the live password checklist. */
internal data class PasswordRule(val label: String, val isMet: Boolean)

/**
 * All validation rules for the Sign In / Sign Up screens live here so both screens
 * (and any future unit tests) share exactly the same logic.
 */
internal object AuthValidator {
    const val MAX_EMAIL_LENGTH = 254
    const val MAX_NAME_LENGTH = 50
    const val MIN_PASSWORD_LENGTH = 8
    const val MAX_PASSWORD_LENGTH = 64

    private val EMAIL_REGEX = Regex(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$"
    )

    // Symbols allowed inside a name besides letters: space - ' ’ .
    private val NAME_ALLOWED_SYMBOLS = setOf(' ', '-', '\'', '\u2019', '.')

    // ---------- Email ----------

    fun emailError(raw: String): String? {
        val email = raw.trim()
        if (email.isEmpty()) return "Email is required"
        if (email.length > MAX_EMAIL_LENGTH) return "Email is too long"
        if (!email.contains('@')) return "Email must contain an '@'"

        val local = email.substringBefore('@')
        val invalidLocal = local.length > 64 ||
                local.startsWith('.') ||
                local.endsWith('.') ||
                local.contains("..")
        if (invalidLocal || !EMAIL_REGEX.matches(email)) {
            return "Please enter a valid email address (e.g. name@example.com)"
        }
        return null
    }

    // ---------- Name ----------

    /** Trims and collapses repeated spaces. Use this value when you actually save the name. */
    fun normalizeName(raw: String): String = raw.trim().replace(Regex("\\s+"), " ")

    fun nameError(raw: String): String? {
        val name = normalizeName(raw)
        if (name.isEmpty()) return "Name is required"
        if (name.any { it.isDigit() }) return "Name cannot contain numbers"
        if (name.any { !(it.isLetter() || it in NAME_ALLOWED_SYMBOLS) }) {
            return "Use only letters, spaces, hyphens, apostrophes and periods"
        }
        if (!name.first().isLetter()) return "Name must start with a letter"
        if (name.count { it.isLetter() } < 2) return "Name must contain at least 2 letters"
        if (name.length > MAX_NAME_LENGTH) return "Name must be at most $MAX_NAME_LENGTH characters"
        return null
    }

    // ---------- Password ----------

    fun passwordRules(password: String): List<PasswordRule> = listOf(
        PasswordRule(
            "At least $MIN_PASSWORD_LENGTH characters",
            password.length >= MIN_PASSWORD_LENGTH
        ),
        PasswordRule("One uppercase letter (A-Z)", password.any { it in 'A'..'Z' }),
        PasswordRule("One lowercase letter (a-z)", password.any { it in 'a'..'z' }),
        PasswordRule("One number (0-9)", password.any { it in '0'..'9' }),
        PasswordRule(
            "One special character (!@#\$%^&*)",
            password.any { !it.isLetterOrDigit() && !it.isWhitespace() }
        )
    )

    fun isPasswordValid(password: String): Boolean =
        password.length <= MAX_PASSWORD_LENGTH && passwordRules(password).all { it.isMet }

    fun confirmPasswordError(password: String, confirm: String): String? = when {
        confirm.isEmpty() -> "Please confirm your password"
        confirm != password -> "Passwords do not match"
        else -> null
    }
}

/**
 * Holds the text of a field and whether the user has "touched" it (left the field at least once).
 * Errors are only shown after a field is touched, so people aren't yelled at while still typing.
 */
@Stable
internal class FieldState(initial: String = "") {
    var text by mutableStateOf(initial)
    var touched by mutableStateOf(false)
    private var hadFocus = false

    fun onFocusChanged(focused: Boolean) {
        if (focused) hadFocus = true else if (hadFocus) touched = true
    }
}

/** Styled text field used by both auth screens, with built-in error styling + message. */
@Composable
internal fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    errorText: String?,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    onFocusChanged: (Boolean) -> Unit = {},
) {
    val isError = errorText != null
    val supporting: (@Composable () -> Unit)? = if (errorText != null) {
        { Text(text = errorText) }
    } else null

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = if (isError) AuthErrorRed else OrangePrimary) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = if (isError) AuthErrorRed else OrangePrimary
            )
        },
        trailingIcon = trailingIcon,
        isError = isError,
        supportingText = supporting,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { onFocusChanged(it.isFocused) },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = OrangePrimary,
            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
            cursorColor = OrangePrimary,
            errorTextColor = Color.White,
            errorBorderColor = AuthErrorRed,
            errorCursorColor = AuthErrorRed,
            errorSupportingTextColor = AuthErrorRed,
            errorLabelColor = AuthErrorRed,
            errorLeadingIconColor = AuthErrorRed,
            errorTrailingIconColor = AuthErrorRed
        ),
        singleLine = true
    )
}