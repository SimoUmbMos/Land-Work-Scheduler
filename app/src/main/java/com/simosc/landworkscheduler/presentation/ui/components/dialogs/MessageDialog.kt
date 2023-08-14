package com.simosc.landworkscheduler.presentation.ui.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog


@Preview
@Composable
private fun PreviewMessageWithoutTitleAndButtonsDialog(){
    MessageDialog(
        message = "This is a mock dialog"
    )
}
@Preview
@Composable
private fun PreviewMessageWithoutMessageAndButtonsDialog(){
    MessageDialog(
        title = "Mock Dialog",
    )
}
@Preview
@Composable
private fun PreviewMessageWithoutButtonsDialog(){
    MessageDialog(
        title = "Mock Dialog",
        message = "This is a mock dialog"
    )
}
@Preview
@Composable
private fun PreviewMessageWithoutCancelDialog(){
    MessageDialog(
        title = "Mock Dialog",
        message = "This is a mock dialog",
        submitText = "Submit"
    )
}
@Preview
@Composable
private fun PreviewMessageWithoutSubmitDialog(){
    MessageDialog(
        title = "Mock Dialog",
        message = "This is a mock dialog",
        cancelText = "Cancel"
    )
}
@Preview
@Composable
private fun PreviewMessageDialog(){
    MessageDialog(
        title = "Mock Dialog",
        message = "This is a mock dialog",
        submitText = "Submit",
        cancelText = "Cancel"
    )
}

@Composable
fun MessageDialog(
    title: String? = null,
    message: String? = null,
    submitText: String? = null,
    cancelText: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.primary,
    messageColor: Color = MaterialTheme.colorScheme.onSurface,
    submitColor: Color = MaterialTheme.colorScheme.primary,
    cancelColor: Color = MaterialTheme.colorScheme.onSurface,
    onSubmit: () -> Unit = {},
    onCancel: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    if(title == null && message == null)
        return

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                title?.let{
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = title,
                        color = titleColor,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }
                if(title != null && message != null){
                    Spacer(modifier = Modifier.height(16.dp))
                }
                message?.let{
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = message,
                        color = messageColor,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
                if(submitText != null || cancelText != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        cancelText?.let{
                            TextButton(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    onDismiss()
                                    onCancel()
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = cancelColor
                                )
                            ) {
                                Text(text = cancelText)
                            }
                        }
                        if(submitText != null && cancelText != null) {
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                        submitText?.let{
                            TextButton(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    onDismiss()
                                    onSubmit()
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = submitColor
                                )
                            ) {
                                Text(text = submitText)
                            }
                        }
                    }
                }
            }
        }
    }
}