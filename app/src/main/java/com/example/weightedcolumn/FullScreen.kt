package com.example.weightedcolumn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.weightedcolumn.ui.theme.WeightedColumnTheme
import com.google.accompanist.insets.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController


@Composable
fun FullScreen(
    content: @Composable () -> Unit
) {
    WeightedColumnTheme {
        ProvideWindowInsets {
            val backgroundColor = MaterialTheme.colors.secondary
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = MaterialTheme.colors.isLight
            SideEffect {
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = useDarkIcons
                )
            }
            CompositionLocalProvider(
                LocalContentColor provides contentColorFor(backgroundColor)
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                ) {
                    Column(
                        Modifier
                            .padding(32.dp)
                            .fillMaxSize()
                            .statusBarsPadding(),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        // TODO: For some reason imePadding doesn't work properly when keyboard is dismissed
                        // without this seemingly pointless code
                        val imePadding = rememberInsetsPaddingValues(
                            insets = LocalWindowInsets.current.ime,
                            applyBottom = true
                        )
                        imePadding.calculateBottomPadding()
                        Column(
                            Modifier
                                .weight(1f)
                                .imePadding()
                        ) {
                            content()
                        }

                        Text(
                            text = "I'm in the corner",
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .navigationBarsPadding()
                                .align(Alignment.End),
                            style = MaterialTheme.typography.button.copy(textDecoration = TextDecoration.Underline)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun HeaderFooterProgressPreview() {
    FullScreen {
        Text("Hello world")
    }
}
