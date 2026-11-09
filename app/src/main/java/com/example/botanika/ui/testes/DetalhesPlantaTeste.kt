package com.example.botanika.ui.testes

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.botanika.data.remote.PerenualService
import com.example.botanika.data.remote.PlantaDetalhes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DetalhesPlantaTeste(
    idPlanta: Int,
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier
) {

    val perenualService = PerenualService()

    var detalhes by remember { mutableStateOf<PlantaDetalhes?>(null) }

    LaunchedEffect(Unit) {
        detalhes = withContext(Dispatchers.IO) {
            perenualService.detalhes(idPlanta)
        }
        Log.d("PERENUALLOG", "Detalhes: ${detalhes}")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(onClick = onVoltar) {
            Text("Voltar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (detalhes == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val d = detalhes!!

            // MAPEAMENTO COMPLETO DE TODOS OS ATRIBUTOS DO DTO
            val todosOsAtributos = listOf(
                "ID" to d.id.toString(),
                "Nome Comum" to (d.nomeComum ?: "N/A"),
                "Nome Científico" to d.nomeCientifico.joinToString(", "),
                "Outros Nomes" to d.outrosNomes.joinToString(", "),
                "Família" to (d.familia ?: "N/A"),
                "Híbrido" to (d.hibrido ?: "N/A"),
                "Autoridade" to (d.autoridade ?: "N/A"),
                "Subespécie" to (d.subespecie ?: "N/A"),
                "Cultivar" to (d.cultivar ?: "N/A"),
                "Variedade" to (d.variedade ?: "N/A"),
                "Epíteto da Espécie" to (d.epitetosEspecie ?: "N/A"),
                "Gênero" to (d.genero ?: "N/A"),
                "Origem" to d.origem.joinToString(", "),
                "Tipo" to (d.tipo ?: "N/A"),
                "Dimensões" to d.dimensoes.joinToString("; ") { "${it.tipo}: ${it.valorMin}-${it.valorMax}" },
                "Ciclo" to (d.ciclo ?: "N/A"),
                "Atrai" to d.atrai.joinToString(", "),
                "Propagação" to d.propagacao.joinToString(", "),
                "Resistência ao Frio (Min/Max)" to "Min: ${d.resistenciaFrio?.min ?: "N/A"} | Max: ${d.resistenciaFrio?.max ?: "N/A"}",
                "URL do Mapa de Resistência" to (d.localResistencia?.urlCompleta ?: "N/A"),
                "Rega" to (d.rega ?: "N/A"),
                "Benchmark de Rega" to "${d.benchmarkRega?.valor ?: "N/A"} ${d.benchmarkRega?.unidade ?: ""}",
                "Anatomia da Planta" to d.anatomiaPlanta.joinToString(", ") { item ->
                    if (item is Map<*, *>) {
                        item.entries.joinToString(", ") { "${it.key}: ${it.value}" }
                    } else {
                        item.toString()
                    }
                },
                "Luz Solar" to d.luzSolar.joinToString(", "),
                "Meses de Poda" to d.mesesPoda.joinToString(", "),
                "Contagem de Poda" to d.contagemPoda.joinToString(", "),
                "Possui Sementes" to if (d.possuiSementes) "Sim" else "Não",
                "Manutenção" to (d.manutencao ?: "N/A"),
                "Guias de Cuidados (URL)" to (d.guiasCuidadosUrl ?: "N/A"),
                "Solo" to d.solo.joinToString(", "),
                "Taxa de Crescimento" to (d.taxaCrescimento ?: "N/A"),
                "Tolerante à Seca" to if (d.toleranteSeca) "Sim" else "Não",
                "Tolerante ao Sal" to if (d.toleranteSal) "Sim" else "Não",
                "Possui Espinhos" to if (d.possuiEspinhos) "Sim" else "Não",
                "Invasiva" to if (d.invasiva) "Sim" else "Não",
                "Tropical" to if (d.tropical) "Sim" else "Não",
                "Planta de Interior (Indoor)" to if (d.plantaInterior) "Sim" else "Não",
                "Nível de Cuidado" to (d.nivelCuidados ?: "N/A"),
                "Suscetibilidade a Pragas" to d.suscetibilidadePragas.joinToString(", "),
                "Possui Flores" to if (d.possuiFlores) "Sim" else "Não",
                "Temporada de Floração" to (d.temporadaFloracao ?: "N/A"),
                "Possui Cones" to if (d.possuiCones) "Sim" else "Não",
                "Possui Frutos" to if (d.possuiFrutos) "Sim" else "Não",
                "Fruto Comestível" to if (d.frutoComestivel) "Sim" else "Não",
                "Temporada de Colheita" to (d.temporadaColheita ?: "N/A"),
                "Possui Folhas" to if (d.possuiFolhas) "Sim" else "Não",
                "Folha Comestível" to if (d.folhaComestivel) "Sim" else "Não",
                "Culinária" to if (d.culinaria) "Sim" else "Não",
                "Medicinal" to if (d.medicinal) "Sim" else "Não",
                "Venenosa para Humanos" to if (d.venenosaHumanos) "Sim" else "Não",
                "Venenosa para Pets" to if (d.venenosaPets) "Sim" else "Não",
                "Descrição" to (d.descricao ?: "N/A"),
                "Licença da Imagem" to (d.imagemPadrao?.nomeLicenca ?: "N/A"),
                "URL da Imagem Padrão" to (d.imagemPadrao?.urlRegular ?: "N/A")
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(todosOsAtributos) { (chave, valor) ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = chave,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = valor,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}