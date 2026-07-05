package com.example.rencar_pair.presentation.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Telefon numarası formatlayıcısı.
 * Girilen rakamları (maksimum 10 hane olarak varsayar: 5XX XXX XX XX)
 * anlık olarak boşluklu "5XX XXX XX XX" biçiminde gösterir.
 */
class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // En fazla 10 haneyi işleyelim
        val trimmed = if (text.text.length >= 10) text.text.substring(0, 10) else text.text
        
        val out = StringBuilder()
        for (i in trimmed.indices) {
            out.append(trimmed[i])
            if (i == 2 || i == 5 || i == 7) {
                out.append(" ")
            }
        }

        val numberOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 6) return offset + 1
                if (offset <= 8) return offset + 2
                if (offset <= 10) return offset + 3
                return 13
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 7) return offset - 1
                if (offset <= 10) return offset - 2
                if (offset <= 13) return offset - 3
                return 10
            }
        }

        return TransformedText(AnnotatedString(out.toString()), numberOffsetTranslator)
    }
}
