package com.simosc.landworkscheduler.presentation.ui.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Preview
@Composable
private fun PreviewTextEditorDialog(){
    DualTextEditorDialog(
        textLabel1 = "Title1",
        initialText1 = "Mock Text",
        textLabel2 = "Title2",
        initialText2 = "Mock Text",
        onSubmit = {_,_ ->},
        onDismiss = {}
    )
}

@Composable
fun DualTextEditorDialog(
    initialText1: String = "",
    initialText2: String = "",
    textLabel1: String? = null,
    textLabel2: String? = null,
    textPlaceHolder1: String? = null,
    textPlaceHolder2: String? = null,
    properties: DialogProperties = DialogProperties(),
    onSubmit: (String, String) -> Unit,
    onDismiss: () -> Unit,
    runDismissOnSubmit: Boolean = false,
    submitButtonText: String = "Submit"
){
    var text1 by remember(initialText1){
        mutableStateOf(initialText1)
    }
    var text2 by remember(initialText2){
        mutableStateOf(initialText2)
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = properties
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
                    value = text1,
                    onValueChange = { text1 = it },
                    singleLine = true,
                    label = {
                        if(!textLabel1.isNullOrBlank())
                            Text(
                                text = textLabel1,
                                style = MaterialTheme.typography.labelLarge
                            )
                    },
                    placeholder = {
                        if(!textPlaceHolder1.isNullOrBlank())
                            Text(
                                text = textPlaceHolder1,
                                style = MaterialTheme.typography.labelSmall
                            )
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = text2,
                    onValueChange = { text2 = it },
                    singleLine = true,
                    label = {
                        if(!textLabel2.isNullOrBlank())
                            Text(
                                text = textLabel2,
                                style = MaterialTheme.typography.labelLarge
                            )
                    },
                    placeholder = {
                        if(!textPlaceHolder2.isNullOrBlank())
                            Text(
                                text = textPlaceHolder2,
                                style = MaterialTheme.typography.labelSmall
                            )
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        onSubmit(text1, text2)
                        if(runDismissOnSubmit) onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(text = submitButtonText.ifBlank { "Submit" })
                }
            }
        }
    }
}