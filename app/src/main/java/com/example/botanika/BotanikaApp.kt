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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.botanika.ui.camera.CameraScreen
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
import com.example.botanika.ui.plants.nextOccurrence
import com.example.botanika.ui.plants.toCareTask
import com.example.botanika.ui.register.RegisterScreen
import com.example.botanika.ui.routine.RoutineScreen
import com.example.botanika.ui.splash.SplashScreen
import com.example.botanika.ui.theme.BotanikaPink
import com.example.botanika.ui.welcome.WelcomeScreen

private const val SCREEN_FADE_MILLIS = 600
private const val HOME_TASKS = 4

@Composable
internal fun BotanikaApp(vm: BotanikaViewModel = viewModel()) {
    val flower = rememberFlowerRender()
    val context = LocalContext.current

    if (flower == null) {
        Box(Modifier.fillMaxSize().background(BotanikaPink))
        return
    }

    BackHandler(enabled = vm.canGoBack) { vm.back() }

    val goToTab: (MainTab) -> Unit = { tab -> tabScreen(tab)?.let(vm::resetTo) }

    SharedTransitionLayout {
        AnimatedContent(
            targetState = vm.screen,
            transitionSpec = {
                fadeIn(tween(SCREEN_FADE_MILLIS)) togetherWith fadeOut(tween(SCREEN_FADE_MILLIS))
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
                    onLogin = { _, _, _ -> vm.resetTo(Screen.Home) },
                    onForgotPassword = {},
                    onSignUp = { vm.resetTo(Screen.Register) }
                )

                Screen.Register -> RegisterScreen(
                    flower = flower,
                    animatedVisibilityScope = this,
                    onRegister = { _, _, _ -> vm.resetTo(Screen.Home) },
                    onLogin = { vm.resetTo(Screen.Login) }
                )

                Screen.Home -> {
                    val today = todayUtcMillis()
                    val tasks = vm.reminders
                        .mapNotNull { r -> r.nextOccurrence(today)?.let { day -> r to day } }
                        .sortedWith(compareBy({ it.second }, { it.first.hour * 60 + it.first.minute }))
                        .take(HOME_TASKS)
                        .map { (reminder, day) -> reminder.toCareTask(day) }
                    HomeScreen(
                        plantCount = vm.garden.size,
                        tasks = tasks,
                        onOpenGarden = { vm.resetTo(Screen.Garden) },
                        onSeeAllTasks = { vm.resetTo(Screen.Routine) },
                        onTaskReminder = { vm.toggleNotify(it.id) },
                        onTabSelected = goToTab
                    )
                }

                Screen.Explore -> ExploreScreen(
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

                Screen.Garden -> GardenScreen(
                    plants = vm.garden,
                    onAddPlant = { vm.push(Screen.AddPlant) },
                    onPlantClick = { vm.openMyPlantDetail(it.id) },
                    onTabSelected = goToTab
                )

                Screen.Routine -> RoutineScreen(
                    reminders = vm.reminders,
                    onNewReminder = { vm.openReminderForm() },
                    onEditReminder = { vm.openReminderForm(reminderId = it.id) },
                    onToggleNotify = { vm.toggleNotify(it.id) },
                    onTabSelected = goToTab
                )

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
                            onBack = vm::back,
                            onViewInfo = { vm.openCatalogDetail(it.id) },
                            onAddToGarden = { info ->
                                vm.openPlantForm(
                                    PlantDraft(info.id, context.getString(info.name), PlantImage.Photo(photo))
                                )
                            }
                        )
                    }
                }

                Screen.PlantDetail -> {
                    val mine = vm.garden.firstOrNull { it.id == vm.detailPlantId }
                    val catalog = PlantGuides.find(vm.detailCatalogId)
                    if (mine != null) {
                        PlantDetailScreen(
                            name = mine.name,
                            image = mine.image,
                            info = PlantGuides.find(mine.catalogId),
                            myPlant = mine,
                            reminders = vm.reminders.filter { it.plantId == mine.id },
                            onBack = vm::back,
                            onAddToGarden = {},
                            onWater = { vm.waterPlant(mine.id) },
                            onCreateReminder = { vm.openReminderForm(plantId = mine.id) },
                            onEditReminder = { vm.openReminderForm(reminderId = it.id) },
                            onRemove = {
                                vm.removePlant(mine.id)
                                vm.back()
                            }
                        )
                    } else if (catalog != null) {
                        val name = context.getString(catalog.name)
                        PlantDetailScreen(
                            name = name,
                            image = PlantImage.Res(catalog.image),
                            info = catalog,
                            myPlant = null,
                            reminders = emptyList(),
                            onBack = vm::back,
                            onAddToGarden = {
                                vm.openPlantForm(PlantDraft(catalog.id, name, PlantImage.Res(catalog.image)))
                            },
                            onWater = {},
                            onCreateReminder = {},
                            onEditReminder = {},
                            onRemove = {}
                        )
                    }
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
}

private fun tabScreen(tab: MainTab): Screen? = when (tab) {
    MainTab.Home -> Screen.Home
    MainTab.Explore -> Screen.Explore
    MainTab.Garden -> Screen.Garden
    MainTab.Routine -> Screen.Routine
    MainTab.Profile -> null
}
