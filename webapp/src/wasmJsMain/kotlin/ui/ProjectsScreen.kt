package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.Project
import data.Repository
import kotlinx.coroutines.launch
import theme.IvyColors
import ui.components.*

@Composable
fun ProjectsScreen(onProjectClick: (String) -> Unit) {
    var projects by remember { mutableStateOf<List<Project>>(emptyList()) }
    var total by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var hasMore by remember { mutableStateOf(false) }
    var offset by remember { mutableStateOf(0) }
    var localityFilter by remember { mutableStateOf("") }

    val localities = remember {
        listOf(
            "whitefield", "koramangala", "hsr layout", "electronic city",
            "bellandur", "sarjapur road", "hebbal", "yelahanka",
            "jp nagar", "marathahalli", "bannerghatta road"
        )
    }
    val scope = rememberCoroutineScope()

    fun loadProjects(reset: Boolean = false) {
        scope.launch {
            isLoading = true
            val newOffset = if (reset) 0 else offset
            val response = Repository.fetchProjects(
                locality = localityFilter.ifBlank { null },
                offset = newOffset
            )
            if (reset) {
                projects = response.results
                offset = response.results.size
            } else {
                projects = projects + response.results
                offset += response.results.size
            }
            total = response.total
            hasMore = response.has_more
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadProjects(reset = true) }
    LaunchedEffect(localityFilter) { loadProjects(reset = true) }

    Column(modifier = Modifier.fillMaxSize().background(IvyColors.Background)) {
        Box(
            modifier = Modifier.fillMaxWidth().background(IvyColors.Surface).padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column {
                Text("Projects", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = IvyColors.TextPrimary)
                Text("$total projects • Bangalore", fontSize = 13.sp, color = IvyColors.TextSecondary)
            }
        }

        // Simple locality filter
        Row(
            modifier = Modifier.fillMaxWidth().background(IvyColors.Background).padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            ui.components.PillDropdown(
                label = if (localityFilter.isEmpty()) "Any Locality" else localityFilter.replaceFirstChar { c -> c.uppercase() },
                options = listOf("") + localities,
                displayText = { if (it.isEmpty()) "Any Locality" else it.replaceFirstChar { c -> c.uppercase() } },
                onSelected = { localityFilter = it }
            )
        }

        Divider(color = IvyColors.Border, thickness = 0.5.dp)

        if (isLoading && projects.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = IvyColors.Primary)
            }
        } else if (projects.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No projects found", color = IvyColors.TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(projects, key = { it.project_id }) { project ->
                    ProjectCard(project = project, onClick = { onProjectClick(project.project_id) })
                }
                if (hasMore && !isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            Button(
                                onClick = { loadProjects() },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IvyColors.SurfaceVariant, contentColor = IvyColors.Primary)
                            ) { Text("Load More") }
                        }
                    }
                }
            }
        }
    }
}
