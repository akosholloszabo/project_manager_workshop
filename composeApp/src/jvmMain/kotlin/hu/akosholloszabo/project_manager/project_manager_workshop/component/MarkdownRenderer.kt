package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableBody
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.node.Block
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.HtmlBlock
import org.commonmark.node.HtmlInline
import org.commonmark.node.Image
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.Text
import org.commonmark.node.ThematicBreak
import org.commonmark.parser.Parser
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RenderMarkdown(markdown: String) {
    val extensionsMutable = mutableListOf<org.commonmark.Extension>(TablesExtension.create())
    try {
        val cls = Class.forName("org.commonmark.ext.gfm.tasklist.TaskListExtension")
        val createMethod = cls.getMethod("create")
        val ext = createMethod.invoke(null) as org.commonmark.Extension
        extensionsMutable.add(ext)
    } catch (_: Throwable) {
        // tasklist extension not available; continue without it
    }
    // try loading strikethrough extension (optional)
    try {
        val cls = Class.forName("org.commonmark.ext.gfm.strikethrough.StrikethroughExtension")
        val createMethod = cls.getMethod("create")
        val ext = createMethod.invoke(null) as org.commonmark.Extension
        extensionsMutable.add(ext)
    } catch (_: Throwable) {
        // ignore
    }

    val parser = Parser.builder().extensions(extensionsMutable).build()
    val document = parser.parse(markdown)

    // Fallback: parse table alignment from source markdown separator row (e.g. | :--- | ---: | :---: |)
    fun parseTableAlignmentFromSource(source: String, header: List<String>): List<hu.akosholloszabo.project_manager.project_manager_workshop.component.ColumnAlignment>? {
        if (header.isEmpty()) return null
        val lines = source.lines()
        fun normalize(s: String) = s.trim().trim('|').split('|').map { it.trim().replace(Regex("\\s+"), " ").lowercase() }
        val normalizedHeader = header.map { it.trim().replace(Regex("\\s+"), " ").lowercase() }
        for (i in 0 until lines.size - 1) {
            val line = lines[i].trim()
            if (line.isEmpty()) continue
            val hdrCells = normalize(line)
            if (hdrCells.size != normalizedHeader.size) continue
            if (hdrCells == normalizedHeader) {
                // check next non-empty line for separator
                var j = i + 1
                while (j < lines.size && lines[j].trim().isEmpty()) j++
                if (j >= lines.size) break
                val sep = lines[j].trim()
                // separator must contain - and | (or at least dashes and optional colons)
                val sepCells = sep.trim().trim('|').split('|').map { it.trim() }
                if (sepCells.size != normalizedHeader.size) continue
                val aligns = mutableListOf<hu.akosholloszabo.project_manager.project_manager_workshop.component.ColumnAlignment>()
                var ok = true
                for (sc in sepCells) {
                    val s = sc
                    // valid pattern: :?-+:?
                    if (!s.matches(Regex(":?-+:?"))) { ok = false; break }
                    val left = s.startsWith(":")
                    val right = s.endsWith(":")
                    val align = when {
                        left && right -> hu.akosholloszabo.project_manager.project_manager_workshop.component.ColumnAlignment.CENTER
                        left -> hu.akosholloszabo.project_manager.project_manager_workshop.component.ColumnAlignment.LEFT
                        right -> hu.akosholloszabo.project_manager.project_manager_workshop.component.ColumnAlignment.RIGHT
                        else -> hu.akosholloszabo.project_manager.project_manager_workshop.component.ColumnAlignment.LEFT
                    }
                    aligns.add(align)
                }
                if (ok && aligns.size == normalizedHeader.size) return aligns
            }
        }
        return null
    }

    // Helper: render inline children of a Node by emitting the specialized inline components
    @Suppress("unused")
    @Composable
    fun InlineRenderer(node: Node?) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // iterate inline children only (do not walk sibling block nodes)
            var cur: Node? = node?.firstChild
             while (cur != null) {
                 when (cur) {
                    is Text -> {
                        MarkdownText(AnnotatedString(cur.literal))
                    }

                    is org.commonmark.node.Link -> {
                        val ann = buildAnnotatedFrom(cur, MaterialTheme.colorScheme.primary)
                        MarkdownLink(text = ann, url = cur.destination)
                    }

                    is Code -> {
                        MarkdownInlineCode(cur.literal)
                    }

                    is org.commonmark.node.Emphasis -> {
                        val ann = buildAnnotatedFrom(cur, MaterialTheme.colorScheme.primary)
                        MarkdownEmphasis(ann)
                    }

                    is org.commonmark.node.StrongEmphasis -> {
                        val ann = buildAnnotatedFrom(cur, MaterialTheme.colorScheme.primary)
                        MarkdownStrong(ann)
                    }

                    // Best-effort strikethrough detection (extension class name may vary)
                    else -> {
                        val clsName = cur::class.java.simpleName
                        when {
                            clsName.contains("Strikethrough", ignoreCase = true) -> {
                                val ann = buildAnnotatedFrom(cur, MaterialTheme.colorScheme.primary)
                                MarkdownStrikethrough(ann)
                            }

                            cur is SoftLineBreak -> {
                                MarkdownSoftLineBreak()
                            }

                            cur is HardLineBreak -> {
                                MarkdownHardLineBreak()
                            }

                            cur is HtmlInline -> {
                                MarkdownHtmlInline(cur.literal)
                            }

                            cur is Image -> {
                                // Image: destination and alt text (alt is the rendered inline children)
                                val alt = renderInlineText(cur)
                                MarkdownImage(src = cur.destination ?: "", alt = if (alt.isBlank()) null else alt)
                            }

                            else -> {
                                // fallback: render as annotated text
                                val ann = buildAnnotatedFrom(cur, MaterialTheme.colorScheme.primary)
                                if (ann.isNotEmpty()) MarkdownText(ann) else {
                                    val txt = renderInlineText(cur).trim()
                                    if (txt.isNotBlank()) MarkdownText(AnnotatedString(txt))
                                }
                            }
                        }
                    }
                }
                cur = cur.next
            }
        }
    }

    SelectionContainer {
        MarkdownDocument {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(4.dp)) {
                var node: Node? = document.firstChild
                while (node != null) {
                    when (node) {
                        is Heading -> {
                            // Use the heading component; render inline children inside it
                            val ann = buildAnnotatedFrom(node, MaterialTheme.colorScheme.primary)
                            MarkdownHeading(annotated = ann, modifier = Modifier.padding(vertical = 4.dp))
                        }

                        is FencedCodeBlock -> {
                            val code = node.literal ?: ""
                            MarkdownCodeBlock(code = code)
                        }

                        is IndentedCodeBlock -> {
                            val code = node.literal ?: ""
                            MarkdownCodeBlock(code = code)
                        }

                        is Paragraph -> {
                            // Render paragraph as a single AnnotatedString to preserve line wrapping
                            val ann = buildAnnotatedFrom(node, MaterialTheme.colorScheme.primary)
                            MarkdownParagraph(annotated = ann, modifier = Modifier.padding(vertical = 2.dp))
                        }

                        is BulletList -> {
                            // Maintain existing behavior: collect items and use bullet-list component
                            val items = mutableListOf<Pair<Boolean?, AnnotatedString>>()
                            var li: Node? = node.firstChild
                            while (li != null) {
                                val isTaskByExt = try {
                                    li::class.java.getMethod("isChecked"); true
                                } catch (_: Throwable) {
                                    false
                                }
                                if (isTaskByExt) {
                                    val checked = try {
                                        li::class.java.getMethod("isChecked").invoke(li) as Boolean
                                    } catch (_: Throwable) {
                                        false
                                    }
                                    val ann = buildAnnotatedFrom(li, MaterialTheme.colorScheme.primary)
                                    items.add(checked to ann)
                                } else if (li is ListItem) {
                                    val raw = renderInlineText(li).trimStart()
                                    val manualMatch = Regex("""^\[([ xX])]\s+(.*)""").find(raw)
                                    if (manualMatch != null) {
                                        val checked = manualMatch.groupValues[1].trim().equals("x", ignoreCase = true)
                                        val label = manualMatch.groupValues[2]
                                        val ann = buildAnnotatedString { append(label) }
                                        items.add(checked to ann)
                                    } else {
                                        val ann = buildAnnotatedFrom(li, MaterialTheme.colorScheme.primary)
                                        items.add(null to ann)
                                    }
                                }
                                li = li.next
                            }
                            MarkdownBulletList(items = items)
                        }

                        is OrderedList -> {
                            val items = mutableListOf<Pair<Boolean?, AnnotatedString>>()
                            var li: Node? = node.firstChild
                            val idx = node.startNumber
                            while (li != null) {
                                val isTaskByExt = try {
                                    li::class.java.getMethod("isChecked"); true
                                } catch (_: Throwable) {
                                    false
                                }
                                if (isTaskByExt) {
                                    val checked = try {
                                        li::class.java.getMethod("isChecked").invoke(li) as Boolean
                                    } catch (_: Throwable) {
                                        false
                                    }
                                    val ann = buildAnnotatedFrom(li, MaterialTheme.colorScheme.primary)
                                    items.add(checked to ann)
                                } else if (li is ListItem) {
                                    val raw = renderInlineText(li).trimStart()
                                    val manualMatch = Regex("""^\[([ xX])]\s+(.*)""").find(raw)
                                    if (manualMatch != null) {
                                        val checked = manualMatch.groupValues[1].trim().equals("x", ignoreCase = true)
                                        val label = manualMatch.groupValues[2]
                                        val ann = buildAnnotatedString { append(label) }
                                        items.add(checked to ann)
                                    } else {
                                        val ann = buildAnnotatedFrom(li, MaterialTheme.colorScheme.primary)
                                        items.add(null to ann)
                                    }
                                }
                                li = li.next
                            }
                            MarkdownOrderedList(items = items, startIndex = idx)
                        }

                        is TableBlock -> {
                            val headerRows = mutableListOf<List<String>>()
                            val bodyRows = mutableListOf<List<String>>()
                            val aligns = mutableListOf<hu.akosholloszabo.project_manager.project_manager_workshop.component.ColumnAlignment>()
                            var child: Node? = node.firstChild
                            while (child != null) {
                                when (child) {
                                    is TableHead -> {
                                        var tr: Node? = child.firstChild
                                        while (tr != null) {
                                            if (tr is TableRow) {
                                                val cells = mutableListOf<String>()
                                                var cellNode: Node? = tr.firstChild
                                                var colIndex = 0
                                                while (cellNode != null) {
                                                    if (cellNode is TableCell) {
                                                        cells.add(renderInlineText(cellNode))
                                                        // detect alignment via reflection if available (commonmark table cell alignment)
                                                        val alignment = try {
                                                            val meth = cellNode::class.java.getMethod("getAlignment")
                                                            val res = meth.invoke(cellNode)
                                                            val name = res?.toString()?.uppercase()
                                                            when (name) {
                                                                "CENTER" -> hu.akosholloszabo.project_manager.project_manager_workshop.component.ColumnAlignment.CENTER
                                                                "RIGHT" -> hu.akosholloszabo.project_manager.project_manager_workshop.component.ColumnAlignment.RIGHT
                                                                else -> hu.akosholloszabo.project_manager.project_manager_workshop.component.ColumnAlignment.LEFT
                                                            }
                                                        } catch (_: Throwable) {
                                                            hu.akosholloszabo.project_manager.project_manager_workshop.component.ColumnAlignment.LEFT
                                                        }
                                                        // ensure aligns list has enough entries
                                                        if (aligns.size <= colIndex) aligns.add(alignment) else if (aligns[colIndex] == hu.akosholloszabo.project_manager.project_manager_workshop.component.ColumnAlignment.LEFT) aligns[colIndex] = alignment
                                                        colIndex++
                                                    }
                                                    cellNode = cellNode.next
                                                }
                                                headerRows.add(cells)
                                            }
                                            tr = tr.next
                                        }
                                    }

                                    is TableBody -> {
                                        var tr: Node? = child.firstChild
                                        while (tr != null) {
                                            if (tr is TableRow) {
                                                val cells = mutableListOf<String>()
                                                var cellNode: Node? = tr.firstChild
                                                while (cellNode != null) {
                                                    if (cellNode is TableCell) cells.add(renderInlineText(cellNode))
                                                    cellNode = cellNode.next
                                                }
                                                bodyRows.add(cells)
                                            }
                                            tr = tr.next
                                        }
                                    }

                                    is TableRow -> {
                                        val cells = mutableListOf<String>()
                                        var cellNode: Node? = child.firstChild
                                        while (cellNode != null) {
                                            if (cellNode is TableCell) cells.add(renderInlineText(cellNode))
                                            cellNode = cellNode.next
                                        }
                                        bodyRows.add(cells)
                                    }
                                }
                                child = child.next
                            }
                            // fallback: parse alignment markers from source markdown (e.g. | :--- | ---: | :---: |)
                            val sourceAligns = parseTableAlignmentFromSource(markdown, headerRows.firstOrNull() ?: emptyList())
                            val finalAligns = if (sourceAligns != null) sourceAligns else aligns
                            MarkdownTable(headerRows = headerRows, bodyRows = bodyRows, columnAlignments = finalAligns)
                        }

                        is BlockQuote -> {
                            // collect paragraph children as annotated strings
                            val childTexts = mutableListOf<AnnotatedString>()
                            var child: Node? = node.firstChild
                            while (child != null) {
                                if (child is Paragraph) {
                                    // render inline content into an AnnotatedString for blockquote lines
                                    childTexts.add(buildAnnotatedFrom(child, MaterialTheme.colorScheme.primary))
                                } else {
                                    val txt = renderInlineText(child)
                                    if (txt.isNotBlank()) childTexts.add(buildAnnotatedString { append(txt) })
                                }
                                child = child.next
                            }
                            MarkdownBlockQuote(childTexts = childTexts)
                        }

                        is ThematicBreak -> {
                            MarkdownThematicBreak()
                        }

                        is HtmlBlock -> {
                            MarkdownHtmlBlock(node.literal ?: "")
                        }

                        is Block -> {
                            val ann = buildAnnotatedFrom(node, MaterialTheme.colorScheme.primary)
                            MarkdownParagraph(annotated = ann)
                        }

                        else -> {
                            val text = renderInlineText(node)
                            if (text.isNotBlank()) MarkdownFallback(text = text)
                        }
                    }
                    node = node.next
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
fun RenderMarkdownPreview() {
    val sample = Note(
        0, "Preview", """
+# Heading
+
+This is a [link](https://www.jetbrains.com).
+
+- [x] Done
+- [ ] Todo
+
+```
+print("hello")
+```
+"""
    )
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        RenderMarkdown(sample.content)
    }
}
