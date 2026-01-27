package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TwoPaneLayout(
    master: @Composable ColumnScope.() -> Unit,
    detail: (@Composable ColumnScope.() -> Unit)? = null,
    modifier: Modifier = Modifier,
    //TODO Integer goes to resources
    masterWeight: Float = 0.35f,
    //TODO Integer goes to resources
    detailWeight: Float = 1f,
    //TODO Integer goes to resources
    gap: Dp = 16.dp,
    //TODO Integer goes to resources
    detailPadding: Dp = 0.dp
) {
    // TODO If you get a modifier from outside, you should not override
    Row(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(if (detail != null) masterWeight else 1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(gap)
        ) {
            master()
        }
        // TODO takeIf or takeUnless could be used
        if (detail != null) {
            Spacer(Modifier.width(gap))
            Column(
                modifier = Modifier
                    .weight(detailWeight)
                    .fillMaxHeight()
                    .padding(detailPadding),
                verticalArrangement = Arrangement.spacedBy(gap)
            ) {
                detail()
            }
        }
    }
}

// TODO Preview
