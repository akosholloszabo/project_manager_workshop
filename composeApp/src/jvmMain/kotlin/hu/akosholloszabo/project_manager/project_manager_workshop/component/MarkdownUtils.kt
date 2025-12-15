package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString

import org.commonmark.node.Node

// Helper: extract plain text from inline nodes (used for table cells)
fun renderInlineText(node: Node): String {
    val sb = StringBuilder()
    fun walk(n: Node?) {
        var cur = n
        while (cur != null) {
            when (cur) {
                is org.commonmark.node.Text -> sb.append(cur.literal)
                is org.commonmark.node.Emphasis -> walk(cur.firstChild)
                is org.commonmark.node.StrongEmphasis -> walk(cur.firstChild)
                is org.commonmark.node.Code -> sb.append(cur.literal)
                is org.commonmark.node.HardLineBreak -> sb.append('\n')
                else -> walk(cur.firstChild)
            }
            cur = cur.next
        }
    }
    walk(node.firstChild)
    return sb.toString()
}

// Build an AnnotatedString from inline nodes, marking links with "URL" annotations and styling them
fun buildAnnotatedFrom(node: Node, linkColor: Color): AnnotatedString {
    return buildAnnotatedString {
        fun walk(n: Node?) {
            var cur = n
            while (cur != null) {
                when (cur) {
                    is org.commonmark.node.Text -> append(cur.literal)
                    is org.commonmark.node.Link -> {
                        val start = length
                        walk(cur.firstChild)
                        val end = length
                        if (start < end) {
                            addStringAnnotation(tag = "URL", annotation = cur.destination, start = start, end = end)
                            addStyle(
                                androidx.compose.ui.text.SpanStyle(
                                    color = linkColor,
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                                ),
                                start,
                                end
                            )
                        }
                    }

                    is org.commonmark.node.Emphasis -> walk(cur.firstChild)
                    is org.commonmark.node.StrongEmphasis -> {
                        val s = length
                        walk(cur.firstChild)
                        addStyle(
                            androidx.compose.ui.text.SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                            s,
                            length
                        )
                    }

                    is org.commonmark.node.Code -> append(cur.literal)
                    is org.commonmark.node.HardLineBreak -> append('\n')
                    else -> walk(cur.firstChild)
                }
                cur = cur.next
            }
        }
        walk(node.firstChild)
    }
}

