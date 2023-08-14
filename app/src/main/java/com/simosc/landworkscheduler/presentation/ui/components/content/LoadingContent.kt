package com.simosc.landworkscheduler.presentation.ui.components.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
@Preview(
    showSystemUi = true,
    showBackground = true
)
private fun PreviewLoadingScreenContent(){
    LoadingContentComponent()
}

@Composable
fun LoadingContentComponent(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    size: Dp = 64.dp,
    strokeWidth: Dp = 6.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    space: Dp = 8.dp,
    text: String = "Loading Data...",
    textAlign: TextAlign = TextAlign.Center,
    textStyle: TextStyle = MaterialTheme.typography.titleSmall
) = Column(
    modifier = modifier,
    horizontalAlignment = horizontalAlignment,
    verticalArrangement = verticalArrangement
){
    CircularProgressIndicator(
        modifier = Modifier.size(size = size),
        color = color,
        strokeWidth = strokeWidth
    )
    Spacer(
        modifier = Modifier.height(space)
    )
    Text(
        text = text,
        textAlign = textAlign,
        style = textStyle
    )
}
