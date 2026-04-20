package gym.management.presentation.common

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

fun applyTimeMask(input: String): String =
    input.filter { it.isDigit() }.take(4)

fun formatTimeDigits(digits: String): String {
    val d = digits.filter { it.isDigit() }.take(4)
    return if (d.length == 4) "${d.substring(0, 2)}:${d.substring(2, 4)}" else d
}

class TimeVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val out = buildString {
            digits.forEachIndexed { i, c ->
                if (i == 2) append(':')
                append(c)
            }
        }
        val offsetMap = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                if (offset <= 2) offset else offset + 1
            override fun transformedToOriginal(offset: Int): Int =
                if (offset <= 2) offset else offset - 1
        }
        return TransformedText(AnnotatedString(out), offsetMap)
    }
}
