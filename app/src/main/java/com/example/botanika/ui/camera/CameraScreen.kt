package com.example.botanika.ui.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.botanika.R
import com.example.botanika.ui.components.DoodleIcon
import com.example.botanika.ui.components.DoodleIconType
import com.example.botanika.ui.theme.BotanikaGreen
import com.example.botanika.ui.theme.BotanikaPink
import java.io.File

private val FlashYellow = Color(0xFFFFC928)
private val CaptureButtonColor = Color(0xFFE3E8E3)

private fun pathOf(d: String): Path = PathParser().parsePathString(d).toPath()

private val BackArrow = pathOf("M20.5,12 L4,12 M11,4.8 L3.8,12 L11,19.2")
private val Bolt = pathOf("M13.4,1.8 L5,13.4 L10.6,13.4 L9.6,22.2 L19,9.8 L13.2,9.8 Z")
private val Hills = pathOf(
    "M2.4,15.4 C5.4,11.6 8.8,11.8 11.8,15 C14,12.8 17.2,12.4 21.6,15.6 " +
        "C21,19.4 17,22 12,22 C7,22 3,19.6 2.4,15.4 Z"
)
private val Sparkle = pathOf(
    "M19.6,1.8 C19.9,4 20.6,4.8 22.8,5.2 C20.6,5.6 19.9,6.4 19.6,8.6 " +
        "C19.3,6.4 18.6,5.6 16.4,5.2 C18.6,4.8 19.3,4 19.6,1.8 Z"
)

@Composable
internal fun CameraScreen(
    onBack: () -> Unit,
    onPhoto: (Uri) -> Unit = {}
) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(hasCameraPermission(context)) }
    var asked by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted = it }

    LaunchedEffect(Unit) {
        if (!granted && !asked) {
            asked = true
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { granted = hasCameraPermission(context) }

    BackHandler(onBack = onBack)

    if (granted) {
        CameraContent(onBack = onBack, onPhoto = onPhoto)
    } else {
        PermissionContent(
            onBack = onBack,
            onAllow = {
                val activity = context.findActivity()
                val canAskAgain = activity != null &&
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)
                if (canAskAgain || !asked) {
                    asked = true
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                } else {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData(Uri.fromParts("package", context.packageName, null))
                    )
                }
            }
        )
    }
}

@Composable
private fun CameraContent(onBack: () -> Unit, onPhoto: (Uri) -> Unit) {
    val context = LocalContext.current
    var useFront by rememberSaveable { mutableStateOf(false) }
    var torchOn by rememberSaveable { mutableStateOf(false) }
    var hasFlash by remember { mutableStateOf(false) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) onPhoto(uri) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        CameraPreview(
            useFront = useFront,
            torchOn = torchOn,
            imageCapture = imageCapture,
            onFlashAvailable = {
                hasFlash = it
                if (!it) torchOn = false
            },
            onUnavailableLens = { useFront = !useFront },
            modifier = Modifier.fillMaxSize()
        )

        ScanFrame(
            Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.65f)
                .aspectRatio(1f)
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CameraIconButton(stringResource(R.string.camera_back), onBack) {
                drawPath(BackArrow, Color.White, style = IconPen(2.4f))
            }
            if (hasFlash) {
                CameraIconButton(stringResource(R.string.camera_flash), { torchOn = !torchOn }) {
                    if (torchOn) {
                        drawPath(Bolt, FlashYellow)
                        drawPath(Bolt, FlashYellow, style = IconPen(1.2f))
                    } else {
                        drawPath(Bolt, Color.White, style = IconPen(1.8f))
                    }
                }
            } else {
                Spacer(Modifier.size(52.dp))
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 32.dp, end = 32.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CameraIconButton(stringResource(R.string.camera_gallery), {
                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) {
                drawCircle(Color.White, radius = 10.4f, center = Offset(12f, 12f), style = IconPen(1.8f))
                drawPath(Hills, Color.White.copy(alpha = 0.85f))
                drawCircle(Color.White, radius = 1.7f, center = Offset(15.6f, 8.2f))
            }

            CaptureButton(onClick = { takePhoto(context, imageCapture, onPhoto) })

            CameraIconButton(stringResource(R.string.camera_flip), { useFront = !useFront }) {
                drawFlipIcon()
            }
        }
    }
}

@Composable
private fun CameraPreview(
    useFront: Boolean,
    torchOn: Boolean,
    imageCapture: ImageCapture,
    onFlashAvailable: (Boolean) -> Unit,
    onUnavailableLens: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }
    var camera by remember { mutableStateOf<Camera?>(null) }

    DisposableEffect(useFront, lifecycleOwner) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null
        providerFuture.addListener({
            val cameraProvider = providerFuture.get()
            provider = cameraProvider
            val selector = if (useFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            if (!cameraProvider.hasCamera(selector)) {
                onUnavailableLens()
                return@addListener
            }
            val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture).also {
                onFlashAvailable(it.cameraInfo.hasFlashUnit())
            }
        }, ContextCompat.getMainExecutor(context))
        onDispose {
            camera = null
            provider?.unbindAll()
        }
    }

    LaunchedEffect(camera, torchOn) {
        camera?.takeIf { it.cameraInfo.hasFlashUnit() }?.cameraControl?.enableTorch(torchOn)
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}

private fun takePhoto(context: Context, imageCapture: ImageCapture, onPhoto: (Uri) -> Unit) {
    val dir = File(context.cacheDir, "captures").apply { mkdirs() }
    val file = File(dir, "botanika_${System.currentTimeMillis()}.jpg")
    imageCapture.takePicture(
        ImageCapture.OutputFileOptions.Builder(file).build(),
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onPhoto(Uri.fromFile(file))
            }

            override fun onError(exception: ImageCaptureException) = Unit
        }
    )
}

@Composable
private fun PermissionContent(onBack: () -> Unit, onAllow: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(BotanikaPink)
    ) {
        Box(
            Modifier
                .statusBarsPadding()
                .padding(start = 20.dp, top = 16.dp)
        ) {
            CameraIconButton(stringResource(R.string.camera_back), onBack) {
                drawPath(BackArrow, BotanikaGreen, style = IconPen(2.4f))
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DoodleIcon(DoodleIconType.Camera, Modifier.size(84.dp))
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.camera_permission_title),
                color = BotanikaGreen,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.camera_permission_message),
                color = BotanikaGreen,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(BotanikaGreen)
                    .clickable(role = Role.Button, onClick = onAllow),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.camera_permission_allow),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CameraIconButton(
    description: String,
    onClick: () -> Unit,
    draw: DrawScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClickLabel = description, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(34.dp)) {
            scale(size.minDimension / 24f, pivot = Offset.Zero) { draw() }
        }
    }
}

@Composable
private fun CaptureButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(CaptureButtonColor.copy(alpha = 0.88f))
            .clickable(
                role = Role.Button,
                onClickLabel = stringResource(R.string.camera_capture),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        DoodleIcon(DoodleIconType.Camera, Modifier.size(56.dp))
        Canvas(Modifier.size(56.dp)) {
            scale(size.minDimension / 24f, pivot = Offset.Zero) {
                drawPath(Sparkle, BotanikaGreen, style = IconPen(1.2f))
            }
        }
    }
}

@Composable
private fun ScanFrame(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val stroke = 4.dp.toPx()
        val radius = size.width * 0.16f
        val arm = size.width * 0.30f
        val corner = Path().apply {
            moveTo(stroke / 2, arm)
            lineTo(stroke / 2, radius)
            arcTo(Rect(stroke / 2, stroke / 2, stroke / 2 + radius * 2, stroke / 2 + radius * 2), 180f, 90f, false)
            lineTo(arm, stroke / 2)
        }
        val pen = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        for (quarter in 0 until 4) {
            rotate(quarter * 90f, pivot = center) { drawPath(corner, Color.White, style = pen) }
        }
    }
}

private fun IconPen(width: Float) = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round)

private fun DrawScope.drawFlipIcon() {
    val c = Offset(12f, 12f)
    val r = 8f
    val head = pathOf("M20.6,5.4 L19.5,9.3 L16.2,7")
    for (turn in 0..1) {
        rotate(turn * 180f, pivot = c) {
            drawArc(
                color = Color.White,
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(c.x - r, c.y - r),
                size = Size(r * 2, r * 2),
                style = IconPen(2.1f)
            )
            drawPath(head, Color.White, style = IconPen(2.1f))
        }
    }
    drawCircle(Color.White, radius = 2.6f, center = c)
}

private fun hasCameraPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
