package com.mk.habittracker.feature.pairnfc

import androidx.compose.animation.core.DurationBasedAnimationSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt

@Suppress("ktlint:compose:vm-injection-check")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcPairingBottomSheet(
    habitId: String,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onTagPaired: (ByteArray) -> Unit = {},
) {
    val vm = hiltViewModel<NfcPairingViewModel, NfcPairingViewModel.Factory> { factory ->
        factory.create(habitId)
    }
    val pairingState by vm.pairingState.collectAsStateWithLifecycle()
    NfcPairingBottomSheet(
        pairingState = pairingState,
        sheetState = sheetState,
        onConfirmOverwrite = vm::confirmOverwrite,
        onTryAgain = vm::tryAgain,
        onDismiss = onDismiss,
        onTagPaired = onTagPaired,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NfcPairingBottomSheet(
    pairingState: PairNfcTagState,
    sheetState: SheetState,
    onConfirmOverwrite: () -> Unit,
    onTryAgain: () -> Unit,
    onDismiss: () -> Unit,
    onTagPaired: (ByteArray) -> Unit = {},
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        NfcPairingScreen(
            pairingState = pairingState,
            onConfirmOverwrite = onConfirmOverwrite,
            onTryAgain = onTryAgain,
            onDismiss = onDismiss,
            onTagPaired = onTagPaired,
        )
    }
}

@Composable
internal fun NfcPairingScreen(
    pairingState: PairNfcTagState,
    onConfirmOverwrite: () -> Unit,
    onTryAgain: () -> Unit,
    onDismiss: () -> Unit,
    onTagPaired: (ByteArray) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when (pairingState) {
            PairNfcTagState.ReadyToScan -> {
                val primary = MaterialTheme.colorScheme.primary
                Icon(
                    modifier = Modifier
                        .padding(32.dp)
                        .size(64.dp)
                        .doublePulseEffect(
                            initialScale = .6f,
                            targetScale = 2f,
                            durationMillis = 1800,
                            delay1 = .1f,
                            delay2 = .3f,
                        )
                        .gradientCircleBackground(primary)
                        .drawBehind {
                            drawCircle(
                                color = Color.White,
                                radius = size.minDimension / 2f * .74f
                            )
                        },
                    painter = painterResource(R.drawable.contactless),
                    contentDescription = "",
                )
                Text("Ready to pair", style = MaterialTheme.typography.displaySmall)
                Text("Hold your phone near the NFC tag")
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }

            is PairNfcTagState.ConfirmOverwrite -> {
                if (pairingState.confirmed) {
                    val primary = MaterialTheme.colorScheme.primary
                    Icon(
                        modifier = Modifier
                            .padding(32.dp)
                            .size(64.dp)
                            .doublePulseEffect(
                                initialScale = .6f,
                                targetScale = 2f,
                                durationMillis = 1800,
                                delay1 = .1f,
                                delay2 = .3f,
                            )
                            .gradientCircleBackground(primary)
                            .drawBehind {
                                drawCircle(
                                    color = Color.White,
                                    radius = size.minDimension / 2f * .74f
                                )
                            },
                        painter = painterResource(R.drawable.contactless),
                        contentDescription = "",
                    )
                    Text("Ready to overwrite", style = MaterialTheme.typography.displaySmall)
                    Text("Hold your phone near the NFC tag")
                } else {
                    Icon(
                        modifier = Modifier
                            .padding(32.dp)
                            .size(64.dp)
                            .gradientCircleBackground(MaterialTheme.colorScheme.error)
                            .drawBehind {
                                drawCircle(
                                    color = Color.White,
                                    radius = size.minDimension / 2f * .74f
                                )
                            },
                        painter = painterResource(R.drawable.contactless),
                        contentDescription = "",
                    )
                    Text("Already paired", style = MaterialTheme.typography.displaySmall)
                    Text(
                        text = "This tag is already paired with <Habit>. Do you want to overwrite it?",
                        textAlign = TextAlign.Center
                    )
                    TextButton(onConfirmOverwrite) {
                        Text("Overwrite")
                    }
                }

                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }

            is PairNfcTagState.Success -> {
                onTagPaired(pairingState.tagId)
                Icon(
                    modifier = Modifier
                        .padding(32.dp)
                        .size(64.dp)
                        .gradientCircleBackground(Color.Green)
                        .drawBehind {
                            drawCircle(
                                color = Color.White,
                                radius = size.minDimension / 2f * .74f
                            )
                        },
                    painter = painterResource(R.drawable.success_alt),
                    contentDescription = "",
                )
                Text("Success", style = MaterialTheme.typography.displaySmall)
                Text("You can now tap your phone to the tag to log this habit")
                TextButton(onClick = onDismiss) {
                    Text("Done")
                }
            }

            is PairNfcTagState.Error -> {
                Icon(
                    modifier = Modifier
                        .padding(32.dp)
                        .size(64.dp)
                        .gradientCircleBackground(Color.Red)
                        .drawBehind {
                            drawCircle(
                                color = Color.White,
                                radius = size.minDimension / 2f * .74f
                            )
                        },
                    painter = painterResource(R.drawable.contactless),
                    contentDescription = "",
                )
                Text("Couldn't read tag", style = MaterialTheme.typography.displaySmall)
                Text(
                    text = "Make sure to hold the tag still against the center of your phone",
                    textAlign = TextAlign.Center,
                )
                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Skip for now")
                    }
                    Spacer(Modifier.width(16.dp))
                    Button(onClick = onTryAgain) {
                        Text("Try again")
                    }
                }
            }
        }
    }
}

@Composable
fun Modifier.pulseEffect(
    targetScale: Float = 1.5f,
    initialScale: Float = 1f,
    brush: Brush = SolidColor(MaterialTheme.colorScheme.secondary),
    shape: Shape = CircleShape,
    animationSpec: DurationBasedAnimationSpec<Float> = tween(1500)
): Modifier {
    val pulseTransition = rememberInfiniteTransition(
        label = "PulseTransition"
    )
    val pulseScale by pulseTransition.animateFloat(
        initialValue = initialScale,
        targetValue = targetScale,
        animationSpec = infiniteRepeatable(animationSpec),
        label = "PulseScale"
    )
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(animationSpec),
    )
    return this
        .drawBehind {
            val outline = shape.createOutline(size, layoutDirection, this)
            scale(scale = pulseScale) {
                drawOutline(
                    outline = outline,
                    brush = brush,
                    alpha = pulseAlpha,
                )
            }
        }
}

@Composable
fun Modifier.doublePulseEffect(
    targetScale: Float = 1.5f,
    initialScale: Float = 1f,
    brush: Brush = SolidColor(MaterialTheme.colorScheme.secondary),
    shape: Shape = CircleShape,
    durationMillis: Int = 1500,
    delay1: Float = 0f,
    delay2: Float = .4f,
): Modifier {
    return this
        .pulseEffect(
            targetScale = targetScale,
            initialScale = initialScale,
            brush = brush,
            shape = shape,
            animationSpec = tween(
                durationMillis = (durationMillis * (1 - delay1)).roundToInt(),
                delayMillis = (durationMillis * delay1).roundToInt(),
            ),
        )
        .pulseEffect(
            targetScale = targetScale,
            initialScale = initialScale,
            brush = brush,
            shape = shape,
            animationSpec = tween(
                durationMillis = (durationMillis * (1 - delay2)).roundToInt(),
                delayMillis = (durationMillis * delay2).roundToInt(),
            ),
        )
}

private fun Modifier.gradientCircleBackground(color: Color): Modifier {
    return this.drawBehind {
        drawCircle(
            brush = Brush.radialGradient(
                0.0f to color.copy(alpha = .8f),
                0.9f to color.copy(alpha = .7f),
                1.35f to color.copy(alpha = .55f),
                1.5f to color.copy(alpha = .4f),
            ),
            radius = size.minDimension / 2f * 1.5f,
            alpha = .8f
        )
    }
}

private class PairingStatePreviewProvider : PreviewParameterProvider<PairNfcTagState> {
    override val values: Sequence<PairNfcTagState>
        get() = sequenceOf(
            PairNfcTagState.ReadyToScan,
            PairNfcTagState.Error("There was an error"),
            PairNfcTagState.Success(byteArrayOf(1, 2, 3)),
            PairNfcTagState.ConfirmOverwrite(false),
            PairNfcTagState.ConfirmOverwrite(true),
        )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun NfcPairingScreenPreview(
    @PreviewParameter(PairingStatePreviewProvider::class) pairingState: PairNfcTagState
) {
    NfcPairingScreen(
        pairingState = pairingState,
        onConfirmOverwrite = {},
        onTryAgain = {},
        onDismiss = {},
    )
}
