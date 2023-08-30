package com.simosc.landworkscheduler.presentation.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import com.simosc.landworkscheduler.core.config.DefaultDialogColor
import com.simosc.landworkscheduler.core.config.DefaultDialogColors
import com.simosc.landworkscheduler.domain.extension.readability

@Preview
@Composable
private fun PreviewSimpleColorPickerDialog(){
    ColorPickerDialog{}
}

@Composable
fun ColorPickerDialog(
    initValue: Color = DefaultDialogColor,
    colorValues: List<Color> = DefaultDialogColors,
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
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .padding(bottom = 8.dp),
                    color = selectedColor,
                ){}

                LazyVerticalGrid (
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    columns = GridCells.FixedSize(54.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalArrangement = Arrangement.SpaceEvenly
                ){
                    items(colorValues){ color ->
                        Box(
                            modifier = Modifier.size(54.dp),
                            contentAlignment = Alignment.Center
                        ){
                            IconButton(
                                enabled = selectedColor != color,
                                onClick = { selectedColor = color },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = color,
                                    contentColor = color,
                                    disabledContainerColor = color,
                                    disabledContentColor = color.readability(),
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.TwoTone.Check,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
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