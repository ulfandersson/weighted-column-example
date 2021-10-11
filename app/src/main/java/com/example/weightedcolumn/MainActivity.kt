package com.example.weightedcolumn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            FullScreen {
                var selectedElement by remember { mutableStateOf<String?>(null) }
                Example(selectedElement, onSelected = { selectedElement = it })
            }
        }
    }
}


@Composable
fun Example(selectedElement: String? = null, onSelected: (String) -> Unit = {}) {
    WeightedColumn(
        Modifier.fillMaxSize()
    ) {
        Box(
            Modifier.weight(1f, allowedToShrink = true),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                "A heading in the middle which is pretty long",
                Modifier.padding(8.dp),
                style = MaterialTheme.typography.h4
            )
        }
        Crossfade(
            targetState = selectedElement,
            Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
        ) { selected ->
            if (selected == null) {
                ListOfThings(onSelected)
            } else {
                EnterPassword(selected)
            }
        }
    }
}

@Composable
fun ExampleWithColumn(selectedElement: String? = null, onSelected: (String?) -> Unit = {}) {
    Column(
        Modifier.fillMaxSize()
    ) {
        Box(
            Modifier.weight(1f),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                "A heading in the middle which is pretty long",
                Modifier.padding(8.dp),
                style = MaterialTheme.typography.h4
            )
        }
        Crossfade(
            targetState = selectedElement,
            Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
        ) { selected ->
            if (selected == null) {
                ListOfThings(onSelected)
            } else {
                EnterPassword(selected, onPassword = {
                    if (it == null) {
                        onSelected(null)
                    }
                })
            }
        }
    }
}

private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

@Composable
private fun ListOfThings(onSelected: (String) -> Unit) {
    LazyColumn(
        Modifier
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(30) {
            item {
                Surface(
                    Modifier
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    val randomString = (5..10)
                        .map { kotlin.random.Random.nextInt(0, charPool.size) }
                        .map(charPool::get)
                        .joinToString("");
                    Row(
                        Modifier
                            .padding(4.dp)
                            .clickable {
                                onSelected(randomString)
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(randomString, style = MaterialTheme.typography.body1)
                    }
                }
            }
        }
    }
}

@Composable
private fun EnterPassword(
    selected: String,
    onPassword: (password: String?) -> Unit = { }
) {
    var password by remember { mutableStateOf("") }
    // var passwordVisibility by remember { mutableStateOf(false) }
    val passwordVisibility = false
    Surface(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 8.dp,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colors.secondaryVariant
    ) {
        val focusManager = LocalFocusManager.current
        Column(
            Modifier
                .padding(16.dp)
        ) {
            Text(
                text = "You selected $selected",
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.body1
            )
            Text(
                text = "Enter password",
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.body1
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = password,
                placeholder = {
                    Text(
                        text = "Enter password",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                onValueChange = { value: String ->
                    password = value
                },
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    if (password.isNotEmpty()) {
                        onPassword(password)
                    }
                }),
                // Skip for now, see if https://issuetracker.google.com/issues/199656714 is solved
                /*
                trailingIcon = {
                    val image = if (passwordVisibility)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    IconButton(onClick = {
                        passwordVisibility = !passwordVisibility
                    }) {
                        Icon(imageVector = image, "")
                    }
                },
                 */
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = MaterialTheme.colors.surface,
                    textColor = MaterialTheme.colors.onSurface
                )
            )

            Row(
                Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.End)
            ) {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onPassword(null)
                    },
                    Modifier.padding(end = 8.dp)
                ) {
                    Text(text = "Cancel")
                }
                Button(
                    enabled = password.isNotEmpty(),
                    onClick = {
                        focusManager.clearFocus()
                        onPassword(password)
                    }
                ) {
                    Text(text = "Ok")
                }
            }
        }
    }
}

@Preview(group = "weightedColumn")
@Composable
fun DefaultPreview() {
    FullScreen {
        var selectedElement by remember { mutableStateOf<String?>(null) }
        Example(selectedElement, onSelected = { selectedElement = it })
    }
}

@Preview(group = "weightedColumn")
@Composable
fun DefaultPreviewWithSelected() {
    FullScreen {
        Example("Hello")
    }
}


@Preview(group = "weightedColumn", heightDp = 480)
@Composable
fun DefaultPreviewWithSelectedSmall() {
    FullScreen {
        Example("Hello")
    }
}

@Preview(group = "normalColumn")
@Composable
fun DefaultPreview2() {
    FullScreen {
        var selectedElement by remember { mutableStateOf<String?>(null) }
        ExampleWithColumn(selectedElement, onSelected = { selectedElement = it })
    }
}

@Preview(group = "normalColumn")
@Composable
fun DefaultPreviewWithSelected2() {
    FullScreen {
        ExampleWithColumn("Hello")
    }
}


@Preview(group = "normalColumn", heightDp = 480)
@Composable
fun DefaultPreviewWithSelectedSmall2() {
    FullScreen {
        ExampleWithColumn("Hello")
    }
}