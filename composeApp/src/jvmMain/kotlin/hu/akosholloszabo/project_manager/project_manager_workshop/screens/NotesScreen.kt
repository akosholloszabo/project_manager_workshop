package hu.akosholloszabo.project_manager.project_manager_workshop.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableBody
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.node.Block
import org.commonmark.node.BulletList
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.Heading
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.BlockQuote
import org.commonmark.parser.Parser
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.Desktop
import java.net.URI

// Helper: extract plain text from inline nodes (used for table cells)
private fun renderInlineText(node: Node): String {
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
private fun buildAnnotatedFrom(node: Node, linkColor: Color): AnnotatedString {
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
                                SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline),
                                start,
                                end
                            )
                        }
                    }

                    is org.commonmark.node.Emphasis -> walk(cur.firstChild)
                    is org.commonmark.node.StrongEmphasis -> {
                        val s = length
                        walk(cur.firstChild)
                        addStyle(SpanStyle(fontWeight = FontWeight.Bold), s, length)
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

// Renders an AnnotatedString using Text and detects taps to handle URL annotations.
@Composable
private fun ClickableAnnotatedText(
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

@Composable
fun NotesScreenContent() {
    val notesState = remember {
        mutableStateListOf(
            Note(
                1, "Meeting notes",
"""
# Project X — Sprint Planning Meeting
**Date:** 2025-12-15  
**Time:** 10:00 — 11:30 AM  
**Location:** Conference Room B / Zoom

---

## Attendees
- Alice Johnson (PM)
- Bob Lee (Backend)
- Carina Müller (Frontend)
- Daniel Kim (QA)
- Emma Rossi (Design)

## Agenda
1. Review last sprint's outcomes
2. Finalize scope for next sprint
3. Identify blockers & risks
4. Assign action items and owners
5. Confirm release checklist

---

## Summary / Decisions
- We will prioritize the new authentication flow (Epic: `AUTH-42`) and move the A/B testing work to the following sprint.
- Minimum viable scope for next sprint:
  - Implement OAuth2 login
  - Basic account settings screen (read-only)
  - Analytics event for login success/failure

> Decision rationale: authenticating users is a prerequisite for several downstream features and reduces manual QA overhead.

---

## Discussion Notes

### 1) Authentication
- Backend (Bob):
  - Will provide an OAuth2 token endpoint and refresh-token flow.
  - Estimated effort: ~5 dev-days.
- Frontend (Carina):
  - Implement client-side login screen + token storage (secure).
  - Will reuse `auth-storage` utility and update to handle refresh flow.

### 2) Account Settings
- Design (Emma): shared a mock — see link below.
- QA (Daniel): will add regression tests to verify account data is read-only in MVP.

### 3) Telemetry
- Add an analytics event:
  - `login_attempt` (properties: `method`, `success`, `response_time_ms`)
- Bob to add server-side logging for failed attempts (rate-limited).

---

## Action Items (ordered)
1. Bob: Implement OAuth2 token endpoint and document API by **2025-12-19**.  
2. Carina: Build the login screen and integrate with token endpoint by **2025-12-22**.  
3. Emma: Finalize account settings mockups and supply assets by **2025-12-17**.  
4. Daniel: Create automation for login/regression tests and add smoke test to CI by **2025-12-23**.  
5. Alice: Coordinate release window and communicate scope to stakeholders by **2025-12-18**.

---

## Blockers & Risks
- Backend capacity: two backend engineers are partially allocated to infra until 2025-12-18. (Risk: delayed OAuth delivery)
- Security review: credential storage approach must pass security review before release.
- External dependency: the identity provider may change certificate rotation schedule.

---

# Links
- [Design mockups](https://www.google.com)

---

# Embedded YouTube Video

<iframe width="560" height="315" src="https://www.youtube.com/embed/dQw4w9WgXcQ" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

---

# Code Snippet

```python
def hello_world():
    print("Hello, world!")
```

---

# Task List
- [x] Setup project repository
- [x] Configure CI/CD pipeline
- [ ] Develop authentication module
- [ ] Create user documentation
- [ ] Conduct security audit

---

# Version History
| Version | Date       | Author            | Description                     |
|---------|------------|-------------------|---------------------------------|
| 1.0     | 2025-12-15 | Alice Johnson      | Initial draft                   |
| 1.1     | 2025-12-16 | Bob Lee            | Added API endpoints              |
| 1.2     | 2025-12-17 | Carina Müller      | Updated design mockups          |
| 1.3     | 2025-12-18 | Daniel Kim         | Fixed typos and formatting      |
| 1.4     | 2025-12-19 | Emma Rossi         | Added release checklist          |

---

# Feedback
For any feedback or questions, please reach out to Alice Johnson at alice.johnson@email.com.

---

# End of Notes
"""
            ),
            Note(2, "Ideas", "# Ideas\n- New feature X..."),
            Note(3, "Draft", "# Draft\nThis is a draft note.")
        )
    }

    var selectedNoteId by rememberSaveable { mutableStateOf(if (notesState.isNotEmpty()) notesState[0].id else null) }
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var editableContent by rememberSaveable { mutableStateOf("") }

    val selectedNote = notesState.find { it.id == selectedNoteId }
    if (!isEditing && selectedNote != null && editableContent != selectedNote.content) {
        editableContent = selectedNote.content
    }

    fun saveNote(noteId: Int?) {
        if (noteId == null) return
        val idx = notesState.indexOfFirst { it.id == noteId }
        if (idx >= 0) {
            val old = notesState[idx]
            if (old.content != editableContent) notesState[idx] = old.copy(content = editableContent)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Notes", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxSize()) {
            // Left: titles only
            Column(modifier = Modifier.width(320.dp).fillMaxSize()) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(notesState) { _, note ->
                        val isSelected = note.id == selectedNoteId
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
                                .clickable {
                            // If we are editing, save changes for the current note (if any) before switching
                            if (isEditing && selectedNoteId != null) {
                                saveNote(selectedNoteId)
                            }
                            // Always leave edit mode when switching notes
                            isEditing = false

                            // Update selection and load the new note's content
                            selectedNoteId = note.id
                            editableContent = note.content
                        }
                                .padding(8.dp)
                        ) {
                            Text(
                                note.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            hu.akosholloszabo.project_manager.project_manager_workshop.SimpleDivider(
                                modifier = Modifier.padding(
                                    top = 8.dp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right: viewer / editor
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        selectedNote?.title ?: "No note selected",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (selectedNote != null) {
                        if (isEditing) {
                            Button(onClick = { saveNote(selectedNoteId); isEditing = false }) { Text("Save") }
                        } else {
                            Button(onClick = { isEditing = true }) { Text("Edit") }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (selectedNote == null) {
                    Text(
                        "Select a note to view or edit it.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                } else {
                    if (isEditing) {
                        TextField(
                            value = editableContent,
                            onValueChange = { editableContent = it },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        RenderMarkdown(selectedNote.content)
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderMarkdown(markdown: String) {
    val extensionsMutable = mutableListOf<org.commonmark.Extension>(TablesExtension.create())
    try {
        val cls = Class.forName("org.commonmark.ext.gfm.tasklist.TaskListExtension")
        val createMethod = cls.getMethod("create")
        val ext = createMethod.invoke(null) as org.commonmark.Extension
        extensionsMutable.add(ext)
    } catch (_: Throwable) {
        // tasklist extension not available; continue without it
    }
    val parser = Parser.builder().extensions(extensionsMutable).build()
    val document = parser.parse(markdown)

    SelectionContainer {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(4.dp)) {
            var node: Node? = document.firstChild
            while (node != null) {
                when (node) {
                    is Heading -> {
                        val annotated = buildAnnotatedFrom(node, MaterialTheme.colorScheme.primary)
                        ClickableAnnotatedText(
                            annotated = annotated,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    is FencedCodeBlock -> {
                        val code = node.literal ?: ""
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).border(1.dp, MaterialTheme.colorScheme.outline).background(MaterialTheme.colorScheme.surfaceVariant)) {
                            Text(
                                text = code.trimEnd('\n'),
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onBackground),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(8.dp)
                            )
                        }
                    }

                    is IndentedCodeBlock -> {
                        val code = node.literal ?: ""
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).border(1.dp, MaterialTheme.colorScheme.outline).background(MaterialTheme.colorScheme.surfaceVariant)) {
                            Text(
                                text = code.trimEnd('\n'),
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onBackground),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(8.dp)
                            )
                        }
                    }

                    is Paragraph -> {
                        val annotated = buildAnnotatedFrom(node, MaterialTheme.colorScheme.primary)
                        ClickableAnnotatedText(
                            annotated = annotated,
                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    is BulletList -> {
                        var li: Node? = node.firstChild
                        while (li != null) {
                            // detect task list item via reflection (isChecked method)
                            val isTaskByExt = try { li::class.java.getMethod("isChecked"); true } catch (_: Throwable) { false }
                            if (isTaskByExt) {
                                val checked = try { li::class.java.getMethod("isChecked").invoke(li) as Boolean } catch (_: Throwable) { false }
                                val annotated = buildAnnotatedFrom(li, MaterialTheme.colorScheme.primary)
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = checked, onCheckedChange = null, enabled = false)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    ClickableAnnotatedText(
                                        annotated = annotated,
                                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground)
                                    )
                                }
                            } else if (li is ListItem) {
                                // fallback: manual GFM task marker detection in the rendered inline text
                                val raw = renderInlineText(li).trimStart()
                                val manualMatch = Regex("""^\[([ xX])]\s+(.*)""" ).find(raw)
                                if (manualMatch != null) {
                                    val checked = manualMatch.groupValues[1].trim().equals("x", ignoreCase = true)
                                    val label = manualMatch.groupValues[2]
                                    val ann = buildAnnotatedString { append(label) }
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = checked, onCheckedChange = null, enabled = false)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        ClickableAnnotatedText(
                                            annotated = ann,
                                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground)
                                        )
                                    }
                                } else {
                                    val annotated = buildAnnotatedFrom(li, MaterialTheme.colorScheme.primary)
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            "• ",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        ClickableAnnotatedText(
                                            annotated = annotated,
                                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground)
                                        )
                                    }
                                }
                            }
                            li = li.next
                        }
                    }

                    is OrderedList -> {
                        var idx = node.startNumber
                        var li: Node? = node.firstChild
                        while (li != null) {
                            val isTaskByExt = try { li::class.java.getMethod("isChecked"); true } catch (_: Throwable) { false }
                            if (isTaskByExt) {
                                val checked = try { li::class.java.getMethod("isChecked").invoke(li) as Boolean } catch (_: Throwable) { false }
                                val annotated = buildAnnotatedFrom(li, MaterialTheme.colorScheme.primary)
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text("${idx}. ", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                                    Checkbox(checked = checked, onCheckedChange = null, enabled = false)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    ClickableAnnotatedText(
                                        annotated = annotated,
                                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground)
                                    )
                                }
                                idx++
                            } else if (li is ListItem) {
                                val raw = renderInlineText(li).trimStart()
                                val manualMatch = Regex("""^\[([ xX])]\s+(.*)""" ).find(raw)
                                if (manualMatch != null) {
                                    val checked = manualMatch.groupValues[1].trim().equals("x", ignoreCase = true)
                                    val label = manualMatch.groupValues[2]
                                    val ann = buildAnnotatedString { append(label) }
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text("${idx}. ", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                                        Checkbox(checked = checked, onCheckedChange = null, enabled = false)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        ClickableAnnotatedText(
                                            annotated = ann,
                                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground)
                                        )
                                    }
                                    idx++
                                } else {
                                    val text = renderInlineText(li)
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Text("${idx}. ", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                                        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                                    }
                                    idx++
                                }
                            }
                            li = li.next
                        }
                    }

                    is TableBlock -> {
                        val headerRows = mutableListOf<List<String>>()
                        val bodyRows = mutableListOf<List<String>>()
                        var child: Node? = node.firstChild
                        while (child != null) {
                            when (child) {
                                is TableHead -> {
                                    var tr: Node? = child.firstChild
                                    while (tr != null) {
                                        if (tr is TableRow) {
                                            val cells = mutableListOf<String>()
                                            var cellNode: Node? = tr.firstChild
                                            while (cellNode != null) {
                                                if (cellNode is TableCell) cells.add(renderInlineText(cellNode))
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

                        val colCount = (headerRows.firstOrNull()?.size ?: bodyRows.firstOrNull()?.size) ?: 0
                        if (colCount > 0) {
                            Column(modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline)) {
                                if (headerRows.isNotEmpty()) {
                                    for (hr in headerRows) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            for (c in 0 until colCount) {
                                                val txt = hr.getOrNull(c) ?: ""
                                                Box(modifier = Modifier.weight(1f)) {
                                                    val ann = buildAnnotatedString { append(txt) }
                                                    ClickableAnnotatedText(
                                                        annotated = ann,
                                                        style = MaterialTheme.typography.bodyLarge.copy(
                                                            color = MaterialTheme.colorScheme.onBackground,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
                                }
                                for (br in bodyRows) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        for (c in 0 until colCount) {
                                            val txt = br.getOrNull(c) ?: ""
                                            Box(modifier = Modifier.weight(1f)) {
                                                val ann = buildAnnotatedString { append(txt) }
                                                ClickableAnnotatedText(
                                                    annotated = ann,
                                                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is BlockQuote -> {
                        // Render blockquote with a leading vertical bar and italic text
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Box(modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.fillMaxWidth()) {
                                var child: Node? = node.firstChild
                                while (child != null) {
                                    if (child is Paragraph) {
                                        val annotated = buildAnnotatedFrom(child, MaterialTheme.colorScheme.primary)
                                        ClickableAnnotatedText(
                                            annotated = annotated,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontStyle = FontStyle.Italic,
                                                color = MaterialTheme.colorScheme.onBackground
                                            ),
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    } else {
                                        // fallback: render plain inline text for other child types
                                        val text = renderInlineText(child)
                                        if (text.isNotBlank()) Text(
                                            text,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onBackground)
                                        )
                                    }
                                    child = child.next
                                }
                            }
                        }
                    }

                    is Block -> {
                        val annotated = buildAnnotatedFrom(node, MaterialTheme.colorScheme.primary)
                        ClickableAnnotatedText(
                            annotated = annotated,
                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    else -> {
                        // fallback: render plain text of node
                        val text = renderInlineText(node)
                        if (text.isNotBlank()) Text(
                            text,
                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground)
                        )
                    }
                }
                node = node.next
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun NotesPreviewLight() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            NotesScreenContent()
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun NotesPreviewDark() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = true) {
        Surface(color = Color(0xFF121212), modifier = Modifier.fillMaxSize()) {
            NotesScreenContent()
        }
    }
}
