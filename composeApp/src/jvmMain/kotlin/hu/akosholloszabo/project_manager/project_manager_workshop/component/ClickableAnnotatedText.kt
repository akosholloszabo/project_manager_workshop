package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.Desktop
import java.net.URI

/**
 * Renders an AnnotatedString and detects taps on annotations with tag "URL". When a URL is tapped,
 * `onOpenLink` is invoked with the URL destination.
 */
@Composable
fun ClickableAnnotatedText(
    annotated: AnnotatedString,
    style: TextStyle,
    modifier: Modifier = Modifier,
    onOpenLink: (String) -> Unit = { url ->
        try {
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(URI(url))
        } catch (_: Exception) {
        }
    }
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = annotated,
        style = style,
        modifier = modifier.pointerInput(annotated) {
            detectTapGestures { pos ->
                val layout = textLayoutResult ?: return@detectTapGestures
                val offset = layout.getOffsetForPosition(pos)
                val anns = annotated.getStringAnnotations(tag = "URL", start = offset, end = offset)
                if (anns.isNotEmpty()) onOpenLink(anns[0].item)
            }
        },
        onTextLayout = { textLayoutResult = it }
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 160)
@Composable
fun ClickableAnnotatedTextPreviewLight() {
    val ann = buildAnnotatedString {
        append("Open ")
        val start = length
        append("Compose website")
        val end = length
        addStringAnnotation(tag = "URL", annotation = "https://www.google.com", start = start, end = end)
        addStyle(SpanStyle(color = Color(0xFF1E88E5), textDecoration = TextDecoration.Underline), start, end)
    }

    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            ClickableAnnotatedText(
                annotated = ann,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 160)
@Composable
fun ClickableAnnotatedTextPreviewDark() {
    val ann = buildAnnotatedString {
        append("Open ")
        val start = length
        append("Compose website")
        val end = length
        addStringAnnotation(tag = "URL", annotation = "https://www.google.com", start = start, end = end)
        addStyle(SpanStyle(color = Color(0xFF90CAF9), textDecoration = TextDecoration.Underline), start, end)
    }

    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = true) {
        Surface(color = Color(0xFF121212), modifier = Modifier.fillMaxSize()) {
            ClickableAnnotatedText(
                annotated = ann,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
