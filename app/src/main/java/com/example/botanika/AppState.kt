package com.example.botanika

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.botanika.data.remote.PerenualService
import com.example.botanika.data.remote.PlantaDetalhes
import com.example.botanika.data.remote.UsuarioService
import com.example.botanika.ui.components.PlantImage
import com.example.botanika.ui.components.addDays
import com.example.botanika.ui.components.todayUtcMillis
import com.example.botanika.ui.explore.CatalogPlant
import com.example.botanika.ui.explore.PlantFilter
import com.example.botanika.ui.home.CareType
import com.example.botanika.ui.plants.MyPlant
import com.example.botanika.ui.plants.PlantDraft
import com.example.botanika.ui.plants.PlantLocation
import com.example.botanika.ui.plants.Reminder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal enum class Screen {
    Splash, Welcome, Login, Register,
    Home, Explore, Garden, Routine, Camera,
    PlantDetail, AddPlant, PhotoResult, PlantForm, ReminderForm
}

internal class BotanikaViewModel : ViewModel() {


    val usuarioService: UsuarioService = UsuarioService()
    val perenualService: PerenualService = PerenualService()
    val stack = mutableStateListOf(Screen.Splash)
    val screen: Screen get() = stack.last()
    val TAG = "BotanikaViewModelLog"
    val canGoBack: Boolean get() = stack.size > 1

    fun login(email: String, senha: String, lembrar: Boolean) {
        viewModelScope.launch {
            try {
                val success = usuarioService.fazerLogin(email, senha)
                if (success) {
                    push(Screen.Home)
                }
            } catch (e: Exception) {
                Log.d(TAG, "Erro ao logar: $e")

            }
        }
    }

    // Estado que a UI vai observar
    var listaPlantas by mutableStateOf<List<CatalogPlant>>(emptyList())
        private set

    fun carregarPlantas() {
      viewModelScope.launch {
            try {
                val res = withContext(Dispatchers.IO) {
                    perenualService.plantas("")
                }
                listaPlantas = res.map { planta ->
                    CatalogPlant(
                        id = planta.id.toString(),
                        name = planta.nomeComum,
                        tags = planta.nomeCientifico,
                        image = planta.smallUrl,
                        filters = setOf(PlantFilter.All)
                    )
                }

            } catch (e: Exception) {
                Log.d(TAG, "Erro ao buscar plantas: $e")
            }
        }
    }
    fun push(next: Screen) {
        stack.add(next)
    }

    fun resetTo(next: Screen) {
        stack.clear()
        stack.add(next)
    }

    fun back() {
        if (stack.size > 1) stack.removeAt(stack.lastIndex)
    }

    var detailCatalogId by mutableStateOf<String?>(null)
        private set

    var detailPlantId by mutableStateOf<String?>(null)
        private set

    var photo by mutableStateOf<Uri?>(null)
        private set

    var draft by mutableStateOf(PlantDraft(null, "", PlantImage.None))
        private set

    var editingReminderId by mutableStateOf<String?>(null)
        private set

    var reminderPlantId by mutableStateOf<String?>(null)
        private set


    var selectedPlantDetail by mutableStateOf<PlantaDetalhes?>(null) // Substitua pelo seu modelo de dados de detalhes
        private set

    var isLoadingDetail by mutableStateOf(false)
        private set

    fun openCatalogDetail(catalogId: String) {
        detailCatalogId = catalogId
        detailPlantId = null

        viewModelScope.launch {
            isLoadingDetail = true
            try {
                val plantIdInt = catalogId.toIntOrNull()

                if (plantIdInt != null) {
                    val plantaDetalhes: PlantaDetalhes? = withContext(Dispatchers.IO) {
                        perenualService.detalhes(plantIdInt)
                    }
                    if(plantaDetalhes != null) {
                        selectedPlantDetail = plantaDetalhes
                    }
                }
                push(Screen.PlantDetail)

            } catch (e: Exception) {
                Log.d(TAG, "Erro ao buscar detalhes da planta: $e")
            } finally {
                isLoadingDetail = false
            }
        }
    }

    fun openMyPlantDetail(plantId: String) {
        detailPlantId = plantId
        detailCatalogId = null
        push(Screen.PlantDetail)
    }

    fun openPhotoResult(uri: Uri) {
        photo = uri
        push(Screen.PhotoResult)
    }

    fun openPlantForm(initial: PlantDraft) {
        draft = initial
        push(Screen.PlantForm)
    }

    fun openReminderForm(reminderId: String? = null, plantId: String? = null) {
        editingReminderId = reminderId
        reminderPlantId = plantId
        push(Screen.ReminderForm)
    }

    private var nextId = 0

    val garden = mutableStateListOf<MyPlant>()
    val reminders = mutableStateListOf<Reminder>()

    init {
        seedExampleData()
    }

    fun addPlant(plant: MyPlant) {
        garden.add(plant.copy(id = "p${nextId++}"))
    }

    fun waterPlant(plantId: String) {
        val index = garden.indexOfFirst { it.id == plantId }
        if (index >= 0) garden[index] = garden[index].copy(lastWateredUtc = todayUtcMillis())
    }

    fun removePlant(plantId: String) {
        garden.removeAll { it.id == plantId }
        reminders.removeAll { it.plantId == plantId }
    }

    fun saveReminder(reminder: Reminder) {
        val index = reminders.indexOfFirst { it.id == reminder.id }
        if (reminder.id.isEmpty() || index < 0) {
            reminders.add(reminder.copy(id = "r${nextId++}"))
        } else {
            reminders[index] = reminder
        }
    }

    fun deleteReminder(reminderId: String) {
        reminders.removeAll { it.id == reminderId }
    }



    fun toggleNotify(reminderId: String) {
        val index = reminders.indexOfFirst { it.id == reminderId }
        if (index >= 0) reminders[index] = reminders[index].copy(notify = !reminders[index].notify)
    }

    private fun seedExampleData() {
        val today = todayUtcMillis()
        val monstera = MyPlant(
            id = "p${nextId++}", catalogId = "monstera", name = "Monstera",
            image = PlantImage.Res(R.drawable.plant_monstera), location = PlantLocation.Living,
            wateringEveryDays = 7, lastWateredUtc = addDays(today, -4), notes = ""
        )
        val kentia = MyPlant(
            id = "p${nextId++}", catalogId = "kentia", name = "Palmeira-de-Kentia",
            image = PlantImage.Res(R.drawable.plant_kentia), location = PlantLocation.Office,
            wateringEveryDays = 7, lastWateredUtc = addDays(today, -4), notes = ""
        )
        val aloe = MyPlant(
            id = "p${nextId++}", catalogId = "aloe", name = "Babosa",
            image = PlantImage.Res(R.drawable.plant_aloe), location = PlantLocation.Balcony,
            wateringEveryDays = 10, lastWateredUtc = addDays(today, -4), notes = ""
        )
        garden.addAll(listOf(monstera, kentia, aloe))

        fun reminder(plant: MyPlant, type: CareType, inDays: Int, hour: Int, repeatDays: Int) = Reminder(
            id = "r${nextId++}", plantId = plant.id, plantName = plant.name, type = type,
            dateUtc = addDays(today, inDays), hour = hour, minute = 0,
            repeatDays = repeatDays, note = "", notify = true
        )
        reminders.addAll(
            listOf(
                reminder(monstera, CareType.Water, 0, 9, 7),
                reminder(monstera, CareType.Sun, 1, 8, 0),
                reminder(aloe, CareType.Fertilizer, 3, 10, 30),
                reminder(kentia, CareType.Pesticide, 5, 17, 0)
            )
        )
    }
}
