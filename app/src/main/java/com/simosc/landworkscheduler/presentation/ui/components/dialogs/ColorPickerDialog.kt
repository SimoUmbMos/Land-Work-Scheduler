package com.simosc.landworkscheduler.presentation.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.simosc.landworkscheduler.R

@Preview
@Composable
private fun PreviewSimpleColorPickerDialog(){
    ColorPickerDialog{}
}

@Composable
fun ColorPickerDialog(
    initValue: Color = Color(0xFFD32F2F),
    runDismissOnSubmit: Boolean = false,
    onDismissDialog: () -> Unit = {},
    onSubmitPress: (Color) -> Unit,
) {
    var selectedColor by remember(initValue){
        mutableStateOf(initValue)
    }
    Dialog(
        onDismissRequest = onDismissDialog
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = selectedColor,
                    content = {}
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Color(0xFFD32F2F).let{ color ->
                        IconButton(
                            colors = IconButtonDefaults
                                .iconButtonColors(containerColor = color),
                            onClick = {
                                selectedColor = color
                            },
                            content = {}
                        )
                    }
                    Color(0xFFE91E63).let{ color ->
                        IconButton(
                            colors = IconButtonDefaults
                                .iconButtonColors(containerColor = color),
                            onClick = {
                                selectedColor = color
                            },
                            content = {}
                        )
                    }
                    Color(0xFF9C27B0).let{ color ->
                        IconButton(
                            colors = IconButtonDefaults
                                .iconButtonColors(containerColor = color),
                            onClick = {
                                selectedColor = color
                            },
                            content = {}
                        )
                    }
                    Color(0xFF673AB7).let{ color ->
                        IconButton(
                            colors = IconButtonDefaults
                                .iconButtonColors(containerColor = color),
                            onClick = {
                                selectedColor = color
                            },
                            content = {}
                        )
                    }
                    Color(0xFF3F51B5).let{ color ->
                        IconButton(
                            colors = IconButtonDefaults
                                .iconButtonColors(containerColor = color),
                            onClick = {
                                selectedColor = color
                            },
                            content = {}
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Color(0xFF2196F3).let{ color ->
                        IconButton(
                            colors = IconButtonDefaults
                                .iconButtonColors(containerColor = color),
                            onClick = {
                                selectedColor = color
                            },
                            content = {}
                        )
                    }
                    Color(0xFF00BCD4).let{ color ->
                        IconButton(
                            colors = IconButtonDefaults
                                .iconButtonColors(containerColor = color),
                            onClick = {
                                selectedColor = color
                            },
                            content = {}
                        )
                    }
                    Color(0xFF009688).let{ color ->
                        IconButton(
                            colors = IconButtonDefaults
                                .iconButtonColors(containerColor = color),
                            onClick = {
                                selectedColor = color
                            },
                            content = {}
                        )
                    }
                    Color(0xFF4CAF50).let{ color ->
                        IconButton(
                            colors = IconButtonDefaults
                                .iconButtonColors(containerColor = color),
                            onClick = {
                                selectedColor = color
                            },
                            content = {}
                        )
                    }
                    Color(0xFF8BC34A).let{ color ->
                        IconButton(
                            colors = IconButtonDefaults
                                .iconButtonColors(containerColor = color),
                            onClick = {
                                selectedColor = color
                            },
                            content = {}
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Color(0xFFCDDC39).let{ color ->
                        IconButton(
                            colors = IconButtonDefaults
                                .iconButtonColors(containerColor = color),
                            onClick = {
                                selectedColor = color
                            },
                            content = {}
                        )
                    }
                    Color(0xFFFFEB3B).let{ color ->
                        IconButton(
                            colors = IconButtonDefaults
                                .iconButtonColors(containerColor = color),
                            onClick = {
                                selectedColor = color
                            },
                            content = {}
                        )
                    }
                    Color(0xFFFFC107).let{ color ->
                        IconButton(
                            colors = IconButtonDefaults
                                .iconButtonColors(containerColor = color),
                            onClick = {
                                selectedColor = color
                            },
                            content = {}
                        )
                    }
                    Color(0xFFFF9800).let{ color ->
                        IconButton(
                            colors = IconButtonDefaults
                                .iconButtonColors(containerColor = color),
                            onClick = {
                                selectedColor = color
                            },
                            content = {}
                        )
                    }
                    Color(0xFFFF5722).let{ color ->
                        IconButton(
                            colors = IconButtonDefaults
                                .iconButtonColors(containerColor = color),
                            onClick = {
                                selectedColor = color
                            },
                            content = {}
                        )
                    }
                }
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    onClick = {
                        onSubmitPress(selectedColor)
                        if(runDismissOnSubmit) onDismissDialog()
                    }
                ) {
                    Text(text = stringResource(R.string.done_label))
                }
            }
        }
    }
}