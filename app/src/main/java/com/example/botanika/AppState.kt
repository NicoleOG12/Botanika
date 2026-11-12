package com.example.botanika

import android.app.Application
import androidx.annotation.StringRes
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
import com.example.botanika.data.remote.Planta
import com.example.botanika.data.remote.PlantaDetalhes
import com.example.botanika.data.remote.PlantasService
import com.example.botanika.data.remote.AuthErro
import com.example.botanika.data.remote.PlantNetErro
import com.example.botanika.data.remote.PlantNetException
import com.example.botanika.data.remote.PlantNetService
import com.example.botanika.ui.plants.PlantMatch
import com.example.botanika.data.remote.UsuarioService
import com.example.botanika.ui.components.PlantImage
import com.example.botanika.ui.components.photoToDataUri
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
    Home, Explore, Garden, Routine, Profile, Camera,
    PlantDetail, AddPlant, PhotoResult, PlantForm, ReminderForm
}

internal class BotanikaViewModel(application: Application) : AndroidViewModel(application) {

    val usuarioService: UsuarioService = UsuarioService()
    val perenualService: PerenualService = PerenualService()
    val plantasService: PlantasService = PlantasService()
    val lembreteService: LembreteService = LembreteService()
    private val plantNetService: PlantNetService = PlantNetService(plantasService)
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

    var authError by mutableStateOf<AuthErro?>(null)
        private set

    var registerSuccess by mutableStateOf(false)
        private set

    fun clearAuthError() {
        authError = null
    }

    var notice by mutableStateOf<Int?>(null)
        private set

    fun showNotice(@StringRes message: Int) {
        notice = message
    }

    fun clearNotice() {
        notice = null
    }

    fun updateUserName(newName: String) {
        val nome = newName.trim()
        if (nome.isBlank() || nome == userName) return
        viewModelScope.launch {
            isLoading = true
            try {
                val ok = withContext(Dispatchers.IO) { usuarioService.atualizarNome(nome) }
                if (ok) {
                    userName = nome
                    showNotice(R.string.profile_name_updated)
                } else {
                    showNotice(R.string.profile_name_error)
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            showNotice(R.string.login_error_email_required)
            return
        }
        viewModelScope.launch {
            isLoading = true
            try {
                val ok = withContext(Dispatchers.IO) { usuarioService.enviarRedefinicaoSenha(email) }
                showNotice(if (ok) R.string.reset_password_sent else R.string.reset_password_error)
            } finally {
                isLoading = false
            }
        }
    }

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
            authError = null
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
                } else {
                    authError = usuarioService.ultimoErro ?: AuthErro.Desconhecido
                }
            } catch (e: Exception) {
                Log.d(TAG, "Erro ao logar: $e")
                authError = AuthErro.Desconhecido
            } finally {
                isLoading = false
            }
        }
    }

    fun register(name: String, email: String, senha: String) {
        if (registerSuccess) return
        viewModelScope.launch {
            isLoading = true
            authError = null
            try {
                val success = withContext(Dispatchers.IO) {
                    usuarioService.criarConta(name, email, senha)
                }
                if (success) {
                    usuarioService.sair()
                    userName = "Usuário"
                    registerSuccess = true
                } else {
                    authError = usuarioService.ultimoErro ?: AuthErro.Desconhecido
                }
            } catch (e: Exception) {
                Log.d(TAG, "Erro ao cadastrar: $e")
                authError = AuthErro.Desconhecido
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
            // Se já há plantas na tela, atualiza em segundo plano sem bloquear com o loader
            val silencioso = garden.isNotEmpty()
            if (!silencioso) isLoading = true
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
                if (!silencioso) isLoading = false
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

    /** Salva no Firestore a planta criada pelo formulário (foto/câmera/manual) e só então abre o jardim. */
    fun savePlantFromForm(plant: MyPlant) {
        viewModelScope.launch {
            isLoading = true
            try {
                val imagem: String? = withContext(Dispatchers.IO) {
                    when (val img = plant.image) {
                        is PlantImage.Photo -> photoToDataUri(getApplication(), img.uri)
                        is PlantImage.Url -> img.url
                        else -> null
                    }
                }
                // Plantas sem id de catálogo recebem um id negativo para não colidir com os do catálogo
                val idPlanta = plant.catalogId?.toIntOrNull()
                    ?: -((System.currentTimeMillis() % Int.MAX_VALUE).toInt().coerceAtLeast(1))
                val planta = Planta(
                    id = idPlanta,
                    nomeComum = plant.name,
                    nomeCientifico = draft.scientific?.takeIf { it.isNotBlank() } ?: "Científico não informado",
                    license = null,
                    licenseName = null,
                    licenseUrl = null,
                    originalUrl = null,
                    regularUrl = imagem,
                    mediumUrl = null,
                    smallUrl = null,
                    thumbnail = imagem,
                    localizacao = plant.location.name,
                    diasRega = plant.wateringEveryDays,
                    ultimaRegaUtc = plant.lastWateredUtc,
                    notas = plant.notes
                )
                val sucesso = withContext(Dispatchers.IO) {
                    plantasService.adicionarPlantaAoJardim(planta)
                }
                if (sucesso) {
                    garden.add(planta.toMyPlant())
                    resetTo(Screen.Garden)
                } else {
                    showNotice(R.string.notice_plant_save_error)
                }
            } catch (e: Exception) {
                Log.d(TAG, "Erro ao salvar planta no jardim: $e")
                showNotice(R.string.notice_plant_save_error)
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
        authError = null
        registerSuccess = false
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

    var photoMatches by mutableStateOf<List<PlantMatch>>(emptyList())
        private set

    var photoAnalyzing by mutableStateOf(false)
        private set

    var photoError by mutableStateOf<PlantNetErro?>(null)
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
        photoMatches = emptyList()
        photoError = null
        photoAnalyzing = true
        push(Screen.PhotoResult)

        viewModelScope.launch {
            try {
                photoMatches = plantNetService.identificar(getApplication<Application>().contentResolver, uri)
            } catch (e: PlantNetException) {
                Log.d(TAG, "Erro ao identificar planta: ${e.erro}")
                photoError = e.erro
            } catch (e: Exception) {
                Log.d(TAG, "Erro ao identificar planta: $e")
                photoError = PlantNetErro.Desconhecido
            } finally {
                photoAnalyzing = false
            }
        }
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
        if (index >= 0) {
            garden[index] = garden[index].copy(lastWateredUtc = todayUtcMillis())
            showNotice(R.string.notice_watered)
        }
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
            // Ressincroniza após gravar, para que uma recarga concorrente (ex.: tela Rotina) não apague o item
            carregarLembretesLocais()
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
            carregarLembretesLocais()
        }
    }
}
