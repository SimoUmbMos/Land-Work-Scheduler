package com.simosc.landworkscheduler.presentation.ui.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.simosc.landworkscheduler.R

@Preview
@Composable
private fun PreviewTextEditorDialogDefault(){
    TextEditorDialog(
        onSubmit = {},
        onDismiss = {}
    )
}

@Preview
@Composable
private fun PreviewTextEditorDialogWithData(){
    TextEditorDialog(
        textLabel = "Title",
        initialText = "Mock Text",
        onSubmit = {},
        onDismiss = {}
    )
}

@Composable
fun TextEditorDialog(
    initialText: String = "",
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
    runDismissOnSubmit: Boolean = false,
    textLabel: String? = null,
    textPlaceHolder: String? = null,
    submitButtonText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        imeAction = ImeAction.Done
    )
){
    var text by remember(initialText){
        mutableStateOf(initialText)
    }
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
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    label = {
                        if(!textLabel.isNullOrBlank())
                            Text(
                                text = textLabel,
                                style = MaterialTheme.typography.labelLarge
                            )
                    },
                    placeholder = {
                        if(!textPlaceHolder.isNullOrBlank())
                            Text(
                                text = textPlaceHolder,
                                style = MaterialTheme.typography.labelSmall
                            )
                    },
                    keyboardOptions = keyboardOptions,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onSubmit(text)
                            if(runDismissOnSubmit) onDismiss()
                        },
                        onGo = {
                            onSubmit(text)
                            if(runDismissOnSubmit) onDismiss()
                        },
                        onNext = {
                            onSubmit(text)
                            if(runDismissOnSubmit) onDismiss()
                        },
                        onPrevious = {
                            onSubmit(text)
                            if(runDismissOnSubmit) onDismiss()
                        },
                        onSearch = {
                            onSubmit(text)
                            if(runDismissOnSubmit) onDismiss()
                        },
                        onSend = {
                            onSubmit(text)
                            if(runDismissOnSubmit) onDismiss()
                        }
                    )
                )
                Button(
                    onClick = {
                        onSubmit(text)
                        if(runDismissOnSubmit) onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = submitButtonText?.ifBlank {
                            stringResource(R.string.submit_label)
                        }?: stringResource(R.string.submit_label)
                    )
                }
            }
        }
    }
}