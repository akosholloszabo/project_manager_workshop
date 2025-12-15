package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

/** Simple cell alignment enum used by the table renderer. */
enum class ColumnAlignment { LEFT, CENTER, RIGHT }

@Composable
fun MarkdownTable(
    headerRows: List<List<String>>,
    bodyRows: List<List<String>>,
    columnAlignments: List<ColumnAlignment> = emptyList(),
    modifier: Modifier = Modifier
) {
    val colCount = (headerRows.firstOrNull()?.size ?: bodyRows.firstOrNull()?.size) ?: 0
    if (colCount <= 0) return
    // Normalize alignments: pad/truncate to colCount
    val aligns = (0 until colCount).map { idx -> columnAlignments.getOrNull(idx) ?: ColumnAlignment.LEFT }

    Column(modifier = modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline)) {
        if (headerRows.isNotEmpty()) {
            for (hr in headerRows) {
                Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant)) {
                    for (c in 0 until colCount) {
                        val txt = hr.getOrNull(c) ?: ""
                        val alignment = aligns.getOrNull(c) ?: ColumnAlignment.LEFT
                        val contentAlignment = when (alignment) {
                            ColumnAlignment.LEFT -> Alignment.CenterStart
                            ColumnAlignment.CENTER -> Alignment.Center
                            ColumnAlignment.RIGHT -> Alignment.CenterEnd
                        }
                        val textAlign = when (alignment) {
                            ColumnAlignment.LEFT -> TextAlign.Start
                            ColumnAlignment.CENTER -> TextAlign.Center
                            ColumnAlignment.RIGHT -> TextAlign.End
                        }
                        Box(modifier = Modifier.weight(1f).padding(8.dp), contentAlignment = contentAlignment) {
                            val ann = buildAnnotatedString { append(txt) }
                            ClickableAnnotatedText(
                                annotated = ann,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    textAlign = textAlign
                                )
                            )
                        }
                    }
                }
            }
            // divider
            Box(modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline)) {}
        }
        for (br in bodyRows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (c in 0 until colCount) {
                    val txt = br.getOrNull(c) ?: ""
                    val alignment = aligns.getOrNull(c) ?: ColumnAlignment.LEFT
                    val contentAlignment = when (alignment) {
                        ColumnAlignment.LEFT -> Alignment.CenterStart
                        ColumnAlignment.CENTER -> Alignment.Center
                        ColumnAlignment.RIGHT -> Alignment.CenterEnd
                    }
                    val textAlign = when (alignment) {
                        ColumnAlignment.LEFT -> TextAlign.Start
                        ColumnAlignment.CENTER -> TextAlign.Center
                        ColumnAlignment.RIGHT -> TextAlign.End
                    }
                    Box(modifier = Modifier.weight(1f).padding(8.dp), contentAlignment = contentAlignment) {
                        val ann = buildAnnotatedString { append(txt) }
                        ClickableAnnotatedText(
                            annotated = ann,
                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground, textAlign = textAlign)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MarkdownTablePreviewLight() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = false) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            MarkdownTable(listOf(listOf("H1", "H2")), listOf(listOf("a", "b")), listOf(ColumnAlignment.LEFT, ColumnAlignment.CENTER))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MarkdownTablePreviewDark() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize(), color = androidx.compose.ui.graphics.Color(0xFF121212)) {
            MarkdownTable(listOf(listOf("H1", "H2")), listOf(listOf("a", "b")), listOf(ColumnAlignment.LEFT, ColumnAlignment.CENTER))
        }
    }
}
