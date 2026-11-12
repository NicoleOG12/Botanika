package com.example.botanika

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.botanika.data.local.LembreteService
import com.example.botanika.data.remote.PerenualService
import com.example.botanika.data.remote.PlantaDetalhes
import com.example.botanika.data.remote.PlantasService
import com.example.botanika.data.remote.UsuarioService
import com.example.botanika.ui.components.PlantImage
import com.example.botanika.ui.components.todayUtcMillis
import com.example.botanika.ui.explore.CatalogPlant
import com.example.botanika.ui.explore.PlantFilter
import com.example.botanika.ui.plants.MyPlant
import com.example.botanika.ui.plants.PlantDraft
import com.example.botanika.ui.plants.Reminder
import com.example.botanika.ui.plants.toMyPlant
import com.example.botanika.ui.plants.toPlanta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal enum class Screen {
    Splash, Welcome, Login, Register,
    Home, Explore, Garden, Routine, Camera,
    PlantDetail, AddPlant, PhotoResult, PlantForm, ReminderForm
}

internal class BotanikaViewModel(application: Application) : AndroidViewModel(application) {

    val usuarioService: UsuarioService = UsuarioService()
    val perenualService: PerenualService = PerenualService()
    val plantasService: PlantasService = PlantasService()
    val lembreteService: LembreteService = LembreteService()
    val stack = mutableStateListOf(Screen.Splash)
    val screen: Screen get() = stack.last()
    val TAG = "BotanikaViewModelLog"
    val canGoBack: Boolean get() = stack.size > 1

    var isLoading by mutableStateOf(false)
        private set

    private val prefs = application.getSharedPreferences("botanika_prefs", Context.MODE_PRIVATE)

    var rememberedEmail by mutableStateOf(prefs.getString("remembered_email", "") ?: "")
        private set

    var rememberMeChecked by mutableStateOf(prefs.getBoolean("remember_me", false))
        private set

    var userName by mutableStateOf(usuarioService.usuarioAtual()?.displayName?.ifBlank { null } ?: "Usuário")
        private set

    init {
        if (usuarioService.usuarioAtual() != null) {
            stack.clear()
            stack.add(Screen.Home)
            carregarNomeUsuario()
            carregarJardim()
        }
        carregarLembretesLocais()
    }

    fun carregarNomeUsuario() {
        val user = usuarioService.usuarioAtual() ?: return
        val currentDisplay = user.displayName
        if (!currentDisplay.isNullOrBlank()) {
            userName = currentDisplay
        }
        viewModelScope.launch {
            val nome = withContext(Dispatchers.IO) {
                usuarioService.obterNomeUsuario()
            }
            if (nome.isNotBlank()) {
                userName = nome
            }
        }
    }

    fun login(email: String, senha: String, lembrar: Boolean) {
        viewModelScope.launch {
            isLoading = true
            try {
                val success = withContext(Dispatchers.IO) {
                    usuarioService.fazerLogin(email, senha)
                }
                if (success) {
                    with(prefs.edit()) {
                        putBoolean("remember_me", lembrar)
                        if (lembrar) {
                            putString("remembered_email", email)
                        } else {
                            remove("remembered_email")
                        }
                        apply()
                    }
                    carregarNomeUsuario()
                    carregarJardim()
                    carregarLembretesLocais()
                    resetTo(Screen.Home)
                }
            } catch (e: Exception) {
                Log.d(TAG, "Erro ao logar: $e")
            } finally {
                isLoading = false
            }
        }
    }

    fun register(name: String, email: String, senha: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val success = withContext(Dispatchers.IO) {
                    usuarioService.criarConta(name, email, senha)
                }
                if (success) {
                    val nomeCadastrado = name.trim().ifBlank { "Usuário" }
                    userName = nomeCadastrado
                    carregarJardim()
                    carregarLembretesLocais()
                    resetTo(Screen.Home)
                }
            } catch (e: Exception) {
                Log.d(TAG, "Erro ao cadastrar: $e")
            } finally {
                isLoading = false
            }
        }
    }

    fun logout() {
        usuarioService.sair()
        userName = "Usuário"
        garden.clear()
        reminders.clear()
        resetTo(Screen.Welcome)
    }

    fun carregarJardim() {
        viewModelScope.launch {
            isLoading = true
            try {
                val plantasRemotas = withContext(Dispatchers.IO) {
                    if (usuarioService.usuarioAtual() != null) {
                        plantasService.verPlantasDoUsuario()
                    } else {
                        null
                    }
                }
                if (plantasRemotas != null) {
                    garden.clear()
                    garden.addAll(plantasRemotas.map { it.toMyPlant() })
                }
            } catch (e: Exception) {
                Log.d(TAG, "Erro ao carregar jardim: $e")
            } finally {
                isLoading = false
            }
        }
    }

    fun addPlantToGarden(apiDetail: PlantaDetalhes) {
        viewModelScope.launch {
            isLoading = true
            try {
                val planta = apiDetail.toPlanta()
                val sucesso = withContext(Dispatchers.IO) {
                    plantasService.adicionarPlantaAoJardim(planta)
                }
                if (sucesso) {
                    garden.add(planta.toMyPlant())
                }
                resetTo(Screen.Garden)
            } catch (e: Exception) {
                Log.d(TAG, "Erro ao adicionar planta ao jardim: $e")
            } finally {
                isLoading = false
            }
        }
    }

    fun carregarLembretesLocais() {
        viewModelScope.launch {
            try {
                val locais = withContext(Dispatchers.IO) {
                    lembreteService.carregarLembretesDoCelular(getApplication())
                }
                reminders.clear()
                reminders.addAll(locais)
            } catch (e: Exception) {
                Log.d(TAG, "Erro ao carregar lembretes locais: $e")
            }
        }
    }

    var listaPlantas by mutableStateOf<List<CatalogPlant>>(emptyList())
        private set

    fun carregarPlantas(busca: String = "", filtro: PlantFilter = PlantFilter.All) {
        viewModelScope.launch {
            isLoading = true
            try {
                val res = withContext(Dispatchers.IO) {
                    plantasService.buscarPlantasPerenual(busca, filtro)
                }
                listaPlantas = res.map { planta ->
                    val plantFilters = mutableSetOf(PlantFilter.All)
                    if (planta.isIndoor) plantFilters.add(PlantFilter.Indoor)
                    if (planta.isSun) plantFilters.add(PlantFilter.Sun)

                    CatalogPlant(
                        id = planta.id.toString(),
                        name = planta.nomeComum,
                        tags = planta.nomeCientifico,
                        image = planta.regularUrl
                            ?: planta.mediumUrl
                            ?: planta.originalUrl
                            ?: planta.smallUrl
                            ?: planta.thumbnail
                            ?: "",
                        filters = plantFilters
                    )
                }

            } catch (e: Exception) {
                Log.d(TAG, "Erro ao buscar plantas: $e")
            } finally {
                isLoading = false
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

    var selectedPlantDetail by mutableStateOf<PlantaDetalhes?>(null)
        private set

    var isLoadingDetail by mutableStateOf(false)
        private set

    fun openCatalogDetail(catalogId: String) {
        Log.d(TAG, "Chegou em openCatalogDetail: $catalogId")

        detailCatalogId = catalogId
        detailPlantId = null
        selectedPlantDetail = null

        viewModelScope.launch {
            isLoadingDetail = true
            isLoading = true
            try {
                val plantaDetalhes: PlantaDetalhes? = withContext(Dispatchers.IO) {
                    plantasService.buscarDetalhesPlanta(catalogId)
                }
                if (plantaDetalhes != null) {
                    selectedPlantDetail = plantaDetalhes
                }

                Log.d(TAG, "Chegou em openCatalogDetail: ${selectedPlantDetail.toString()}")
                push(Screen.PlantDetail)

            } catch (e: Exception) {
                Log.d(TAG, "Erro ao buscar detalhes da planta: $e")
            } finally {
                isLoadingDetail = false
                isLoading = false
            }
        }
    }

    fun openMyPlantDetail(plantId: String) {
        detailPlantId = plantId
        detailCatalogId = null
        selectedPlantDetail = null

        val mine = garden.firstOrNull { it.id == plantId }
        val catId = mine?.catalogId
        if (!catId.isNullOrBlank()) {
            viewModelScope.launch {
                try {
                    val plantaDetalhes = withContext(Dispatchers.IO) {
                        plantasService.buscarDetalhesPlanta(catId)
                    }
                    if (plantaDetalhes != null) {
                        selectedPlantDetail = plantaDetalhes
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Erro ao buscar detalhes da planta do jardim: $e")
                }
            }
        }
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

    val garden = mutableStateListOf<MyPlant>()
    val reminders = mutableStateListOf<Reminder>()

    fun addPlant(plant: MyPlant) {
        garden.add(plant)
    }

    fun waterPlant(plantId: String) {
        val index = garden.indexOfFirst { it.id == plantId }
        if (index >= 0) garden[index] = garden[index].copy(lastWateredUtc = todayUtcMillis())
    }

    fun removePlant(plantId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val plantIdInt = plantId.toIntOrNull()
                if (plantIdInt != null) {
                    withContext(Dispatchers.IO) {
                        plantasService.removerPlantaDoJardim(plantIdInt)
                    }
                }
                garden.removeAll { it.id == plantId }
                reminders.removeAll { it.plantId == plantId }
            } catch (e: Exception) {
                Log.d(TAG, "Erro ao remover planta: $e")
            } finally {
                isLoading = false
            }
        }
    }

    fun saveReminder(reminder: Reminder) {
        val generatedId = if (reminder.id.isEmpty()) "r_${System.currentTimeMillis()}" else reminder.id
        val finalReminder = reminder.copy(id = generatedId, notify = true)

        val index = reminders.indexOfFirst { it.id == finalReminder.id }
        if (index < 0) {
            reminders.add(finalReminder)
        } else {
            reminders[index] = finalReminder
        }

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    lembreteService.salvarEAgendar(getApplication(), finalReminder)
                }
            } catch (e: Exception) {
                Log.d(TAG, "Erro ao salvar lembrete: $e")
            }
        }
    }

    fun deleteReminder(reminderId: String) {
        reminders.removeAll { it.id == reminderId }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    lembreteService.excluirLembreteDoCelular(getApplication(), reminderId)
                }
            } catch (e: Exception) {
                Log.d(TAG, "Erro ao deletar lembrete: $e")
            }
        }
    }
}
