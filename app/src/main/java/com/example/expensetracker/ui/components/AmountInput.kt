package com.example.expensetracker.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun AmountInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Only allow digits and at most one decimal point
            val filtered = newValue.filter { it.isDigit() || it == '.' }
            val dotCount = filtered.count { it == '.' }
            if (dotCount <= 1) {
                // Prevent more than 2 decimal places
                val parts = filtered.split(".")
                if (parts.size == 2 && parts[1].length > 2) return@OutlinedTextField
                onValueChange(filtered)
            }
        },
        modifier = modifier,
        prefix = { Text("¥ ", style = MaterialTheme.typography.headlineMedium) },
        placeholder = { Text("0.00", style = MaterialTheme.typography.headlineMedium) },
        textStyle = MaterialTheme.typography.headlineMedium,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        isError = isError,
        supportingText = if (isError) {{ Text("请输入金额") }} else null
    )
}
