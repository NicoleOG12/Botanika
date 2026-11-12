package com.example.botanika

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.botanika.data.remote.AuthErro
import com.example.botanika.data.remote.PlantNetErro
import com.example.botanika.ui.camera.CameraScreen
import com.example.botanika.ui.components.BotanikaLoader
import com.example.botanika.ui.components.BotanikaToast
import com.example.botanika.ui.components.MainTab
import com.example.botanika.ui.components.PlantImage
import com.example.botanika.ui.components.todayUtcMillis
import com.example.botanika.ui.explore.ExploreScreen
import com.example.botanika.ui.garden.GardenScreen
import com.example.botanika.ui.home.HomeScreen
import com.example.botanika.ui.logo.rememberFlowerRender
import com.example.botanika.ui.login.LoginScreen
import com.example.botanika.ui.plants.AddPlantScreen
import com.example.botanika.ui.plants.PhotoResultScreen
import com.example.botanika.ui.plants.PlantDetailScreen
import com.example.botanika.ui.plants.PlantDraft
import com.example.botanika.ui.plants.PlantFormScreen
import com.example.botanika.ui.plants.PlantGuides
import com.example.botanika.ui.plants.ReminderFormScreen
import com.example.botanika.ui.plants.isFuture
import com.example.botanika.ui.plants.nextOccurrence
import com.example.botanika.ui.plants.toCareTask
import com.example.botanika.ui.plants.toPlantInfo
import com.example.botanika.ui.profile.ProfileScreen
import com.example.botanika.ui.register.RegisterScreen
import com.example.botanika.ui.routine.RoutineScreen
import com.example.botanika.ui.splash.SplashScreen
import com.example.botanika.ui.theme.BotanikaPink
import com.example.botanika.ui.welcome.WelcomeScreen

private const val SCREEN_FADE_MILLIS = 600
private const val TAB_FADE_MILLIS = 220
private const val HOME_TASKS = 4

private val IntroScreens = setOf(Screen.Splash, Screen.Welcome, Screen.Login, Screen.Register)

@Composable
private fun authErrorMessage(error: AuthErro?): String? = when (error) {
    null -> null
    AuthErro.SenhaCurta -> stringResource(R.string.register_error_password_short)
    AuthErro.EmailInvalido -> stringResource(R.string.login_error_email_invalid)
    AuthErro.EmailEmUso -> stringResource(R.string.auth_error_email_in_use)
    AuthErro.CredenciaisInvalidas -> stringResource(R.string.auth_error_invalid_credentials)
    AuthErro.SemRede -> stringResource(R.string.auth_error_network)
    AuthErro.FalhaAoSalvarPerfil -> stringResource(R.string.auth_error_profile)
    AuthErro.Desconhecido -> stringResource(R.string.auth_error_unknown)
}

@Composable
private fun plantNetErrorMessage(error: PlantNetErro?): String? = when (error) {
    null -> null
    PlantNetErro.SemChave -> stringResource(R.string.plantnet_error_no_key)
    PlantNetErro.SemRede -> stringResource(R.string.auth_error_network)
    PlantNetErro.ChaveInvalida -> stringResource(R.string.plantnet_error_invalid_key)
    PlantNetErro.LimiteExcedido -> stringResource(R.string.plantnet_error_limit)
    PlantNetErro.FotoInvalida -> stringResource(R.string.plantnet_error_photo)
    PlantNetErro.Desconhecido -> stringResource(R.string.auth_error_unknown)
}

@Composable
internal fun BotanikaApp(vm: BotanikaViewModel = viewModel()) {
    val flower = rememberFlowerRender()
    val context = LocalContext.current

    if (flower == null) {
        Box(Modifier.fillMaxSize().background(BotanikaPink))
        return
    }

    BackHandler(enabled = vm.canGoBack) { vm.back() }

    val goToTab: (MainTab) -> Unit = { tab ->
        if (tabScreen(tab) != vm.screen) tabScreen(tab)?.let(vm::resetTo)
    }
    val openProfile: () -> Unit = { goToTab(MainTab.Profile) }

    Box(Modifier.fillMaxSize()) {
        SharedTransitionLayout {
            AnimatedContent(
                targetState = vm.screen,
                transitionSpec = {
                    val millis = if (initialState in IntroScreens || targetState in IntroScreens) {
                        SCREEN_FADE_MILLIS
                    } else {
                        TAB_FADE_MILLIS
                    }
                    fadeIn(tween(millis)) togetherWith fadeOut(tween(millis))
                },
                label = "screen"
            ) { target ->
                when (target) {
                    Screen.Splash -> SplashScreen(
                        flower = flower,
                        animatedVisibilityScope = this,
                        onFinished = { vm.resetTo(Screen.Welcome) }
                    )

                    Screen.Welcome -> WelcomeScreen(
                        flower = flower,
                        animatedVisibilityScope = this,
                        onStart = { vm.resetTo(Screen.Login) }
                    )

                    Screen.Login -> LoginScreen(
                        flower = flower,
                        animatedVisibilityScope = this,
                        initialEmail = vm.rememberedEmail,
                        initialRemember = vm.rememberMeChecked,
                        serverError = authErrorMessage(vm.authError),
                        onLogin = { email, senha, lembrar ->
                            vm.login(email, senha, lembrar)
                        },
                        onForgotPassword = vm::sendPasswordReset,
                        onSignUp = { vm.resetTo(Screen.Register) }
                    )

                    Screen.Register -> RegisterScreen(
                        flower = flower,
                        animatedVisibilityScope = this,
                        serverError = authErrorMessage(vm.authError),
                        showSuccess = vm.registerSuccess,
                        onSuccessContinue = { vm.resetTo(Screen.Login) },
                        onRegister = { name, email, password ->
                            vm.register(name, email, password)
                        },
                        onLogin = { vm.resetTo(Screen.Login) }
                    )

                    Screen.Home -> {
                        val today = todayUtcMillis()
                        val tasks = vm.reminders
                            .filter { it.isFuture() }
                            .mapNotNull { r -> r.nextOccurrence(today)?.let { day -> r to day } }
                            .sortedWith(compareBy({ it.second }, { it.first.hour * 60 + it.first.minute }))
                            .take(HOME_TASKS)
                            .map { (reminder, day) -> reminder.toCareTask(day) }
                        HomeScreen(
                            userName = vm.userName,
                            plantCount = vm.garden.size,
                            tasks = tasks,
                            thirstyCount = vm.garden.count { it.nextWateringUtc <= today },
                            onOpenSettings = openProfile,
                            onOpenGarden = { vm.resetTo(Screen.Garden) },
                            onSeeAllTasks = { vm.resetTo(Screen.Routine) },
                            onDeleteTask = { vm.deleteReminder(it.id) },
                            onTabSelected = goToTab
                        )
                    }

                    Screen.Explore -> ExploreScreen(
                        viewModel = vm,
                        onOpenSettings = openProfile,
                        onCameraSearch = { vm.push(Screen.Camera) },
                        onPlantClick = { vm.openCatalogDetail(it.id) },
                        onTabSelected = goToTab
                    )

                    Screen.Camera -> CameraScreen(
                        onBack = vm::back,
                        onPhoto = { uri ->
                            vm.back()
                            vm.openPhotoResult(uri)
                        }
                    )

                    Screen.Garden -> {
                        LaunchedEffect(Unit) {
                            vm.carregarJardim()
                        }
                        GardenScreen(
                            plants = vm.garden,
                            onOpenSettings = openProfile,
                            onAddPlant = { vm.push(Screen.AddPlant) },
                            onPlantClick = { vm.openMyPlantDetail(it.id) },
                            onTabSelected = goToTab
                        )
                    }

                    Screen.Profile -> {
                        val today = todayUtcMillis()
                        ProfileScreen(
                            userName = vm.userName,
                            email = vm.usuarioService.usuarioAtual()?.email.orEmpty(),
                            memberSinceMillis = vm.usuarioService.criadoEmMillis(),
                            plantCount = vm.garden.size,
                            reminderCount = vm.reminders.count { it.isFuture() },
                            thirstyCount = vm.garden.count { it.nextWateringUtc <= today },
                            onEditName = vm::updateUserName,
                            onResetPassword = {
                                vm.sendPasswordReset(vm.usuarioService.usuarioAtual()?.email.orEmpty())
                            },
                            onLogout = vm::logout,
                            onTabSelected = goToTab
                        )
                    }

                    Screen.Routine -> {
                        LaunchedEffect(Unit) {
                            vm.carregarLembretesLocais()
                        }
                        RoutineScreen(
                            reminders = vm.reminders,
                            onOpenSettings = openProfile,
                            onNewReminder = { vm.openReminderForm() },
                            onEditReminder = { vm.openReminderForm(reminderId = it.id) },
                            onDeleteReminder = { vm.deleteReminder(it.id) },
                            onTabSelected = goToTab
                        )
                    }

                    Screen.AddPlant -> AddPlantScreen(
                        onBack = vm::back,
                        onPickFromCatalog = { vm.resetTo(Screen.Explore) },
                        onUseCamera = { vm.push(Screen.Camera) },
                        onPhotoPicked = vm::openPhotoResult,
                        onManual = { vm.openPlantForm(PlantDraft(null, "", PlantImage.None)) }
                    )

                    Screen.PhotoResult -> {
                        val photo = vm.photo
                        if (photo != null) {
                            PhotoResultScreen(
                                photo = photo,
                                analyzing = vm.photoAnalyzing,
                                matches = vm.photoMatches,
                                errorMessage = plantNetErrorMessage(vm.photoError),
                                onBack = vm::back,
                                onViewInfo = { vm.openCatalogDetail(it.id) },
                                onAddToGarden = { info ->
                                    vm.openPlantForm(
                                        PlantDraft(info.id.ifBlank { null }, info.name ?: info.scientific, PlantImage.Photo(photo))
                                    )
                                }
                            )
                        }
                    }

                    Screen.PlantDetail -> {
                        val mine = vm.garden.firstOrNull { it.id == vm.detailPlantId }
                        val catalog = PlantGuides.find(vm.detailCatalogId)
                        val apiDetail = vm.selectedPlantDetail
                        val catalogInfo = if (mine?.catalogId != null) PlantGuides.find(mine.catalogId) else catalog
                        val plantInfo = apiDetail?.toPlantInfo() ?: catalogInfo

                        val plantName = mine?.name ?: apiDetail?.nomeComum ?: catalogInfo?.name ?: "Nome desconhecido"
                        val plantImage = mine?.image ?: apiDetail?.let {
                            PlantImage.Url(it.imagemPadrao?.urlRegular ?: it.imagemPadrao?.miniatura ?: "")
                        } ?: catalogInfo?.image?.let { PlantImage.Url(it) } ?: PlantImage.None

                        PlantDetailScreen(
                            name = plantName,
                            image = plantImage,
                            info = plantInfo,
                            myPlant = mine,
                            reminders = if (mine != null) vm.reminders.filter { it.plantId == mine.id && it.isFuture() } else emptyList(),
                            onBack = vm::back,
                            onAddToGarden = {
                                if (apiDetail != null) {
                                    vm.addPlantToGarden(apiDetail)
                                }
                            },
                            onWater = {
                                if (mine != null) {
                                    vm.waterPlant(mine.id)
                                }
                            },
                            onCreateReminder = {
                                if (mine != null) {
                                    vm.openReminderForm(plantId = mine.id)
                                }
                            },
                            onEditReminder = { reminder ->
                                vm.openReminderForm(reminderId = reminder.id)
                            },
                            onDeleteReminder = { reminder ->
                                vm.deleteReminder(reminder.id)
                            },
                            onRemove = {
                                if (mine != null) {
                                    vm.removePlant(mine.id)
                                    vm.back()
                                }
                            }
                        )
                    }

                    Screen.PlantForm -> PlantFormScreen(
                        initial = vm.draft,
                        onBack = vm::back,
                        onSave = { plant ->
                            vm.addPlant(plant)
                            vm.resetTo(Screen.Garden)
                        }
                    )

                    Screen.ReminderForm -> ReminderFormScreen(
                        plants = vm.garden,
                        existing = vm.reminders.firstOrNull { it.id == vm.editingReminderId },
                        presetPlantId = vm.reminderPlantId,
                        onBack = vm::back,
                        onSave = { reminder ->
                            vm.saveReminder(reminder)
                            vm.back()
                        },
                        onDelete = { reminder ->
                            vm.deleteReminder(reminder.id)
                            vm.back()
                        },
                        onAddPlant = { vm.push(Screen.AddPlant) }
                    )
                }
            }
        }

        BotanikaToast(
            message = vm.notice?.let { stringResource(it) },
            onDismiss = vm::clearNotice,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (vm.isLoading) {
            BotanikaLoader()
        }
    }
}

private fun tabScreen(tab: MainTab): Screen? = when (tab) {
    MainTab.Home -> Screen.Home
    MainTab.Explore -> Screen.Explore
    MainTab.Garden -> Screen.Garden
    MainTab.Routine -> Screen.Routine
    MainTab.Profile -> Screen.Profile
}
