package com.example.expensetracker.util

import java.text.DecimalFormat

private val formatter = DecimalFormat("#,##0.00")

fun Double.formatMoney(): String = "¥${formatter.format(this)}"

fun Double.formatMoneyShort(): String {
    return when {
        this >= 10_000 -> "¥${(this / 10_000).let { "%.2f".format(it) }}万"
        else -> formatMoney()
    }
}
