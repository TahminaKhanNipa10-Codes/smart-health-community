package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ArticleEntity
import com.example.data.model.UserEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesScreen(
    currentUser: UserEntity,
    articles: List<ArticleEntity>,
    onCreateArticle: (title: String, excerpt: String, content: String, category: String, author: String) -> Unit,
    onDeleteArticle: (ArticleEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedArticleForDetails by remember { mutableStateOf<ArticleEntity?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val categories = listOf("ALL", "Cardiology", "Nutrition", "Mental Health", "Sleep & Recovery", "Immunology", "Preventive Care")

    val filteredArticles = articles.filter { art ->
        val matchesCategory = (selectedCategory == "ALL" || art.category.equals(selectedCategory, ignoreCase = true))
        val matchesQuery = searchQuery.isBlank() ||
                art.title.contains(searchQuery, ignoreCase = true) ||
                art.excerpt.contains(searchQuery, ignoreCase = true) ||
                art.content.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    Scaffold(
        floatingActionButton = {
            if (currentUser.role == "ADMIN" || currentUser.role == "DOCTOR") {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MedicalTealPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("publish_article_fab")
                ) {
                    Icon(Icons.Default.PostAdd, contentDescription = "Publish Article")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Search & Filter
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search medical guides, wellness tips...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("articles_search_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) MedicalTealPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Article Cards List
            if (filteredArticles.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.MenuBook,
                    title = "No Articles Found",
                    description = "No wellness articles matched your criteria.",
                    actionLabel = if (currentUser.role == "ADMIN" || currentUser.role == "DOCTOR") "Write Article" else null,
                    onAction = { showCreateDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 14.dp, bottom = 80.dp)
                ) {
                    items(filteredArticles) { article ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedArticleForDetails = article }
                                .testTag("article_card_${article.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when (article.category) {
                                            "Cardiology" -> Color(0xFFFFE4E6)
                                            "Nutrition" -> MedicalGreenContainer
                                            "Mental Health" -> Color(0xFFF3E8FF)
                                            else -> MedicalTealContainer
                                        }
                                    ) {
                                        Text(
                                            text = article.category,
                                            color = when (article.category) {
                                                "Cardiology" -> HealthEmergencyRed
                                                "Nutrition" -> MedicalGreenTertiary
                                                "Mental Health" -> Color(0xFF7E22CE)
                                                else -> MedicalTealPrimary
                                            },
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Text(
                                        text = "📅 ${article.date}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = article.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = article.excerpt,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "By ${article.author}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MedicalTealPrimary
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Read Full Guide",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = MedicalTealPrimary
                                        )
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = MedicalTealPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Article Details Modal
    if (selectedArticleForDetails != null) {
        val art = selectedArticleForDetails!!
        AlertDialog(
            onDismissRequest = { selectedArticleForDetails = null },
            title = {
                Column {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MedicalTealContainer
                    ) {
                        Text(
                            text = art.category,
                            color = MedicalTealPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = art.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "Author: ${art.author} • Date: ${art.date}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Divider()
                    }
                    item {
                        Text(
                            text = art.content,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                    }
                    item {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💡 This educational guide is reviewed by medical professionals. Consult your personal physician for clinical treatment.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedArticleForDetails = null }) {
                    Text("Close")
                }
            },
            dismissButton = {
                if (currentUser.role == "ADMIN") {
                    TextButton(
                        onClick = {
                            onDeleteArticle(art)
                            selectedArticleForDetails = null
                        }
                    ) {
                        Text("Delete", color = HealthEmergencyRed)
                    }
                }
            }
        )
    }

    // Publish Article Dialog
    if (showCreateDialog) {
        var title by remember { mutableStateOf("") }
        var excerpt by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Nutrition") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Publish Medical & Wellness Guide") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Article Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        var catExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = catExpanded,
                            onExpandedChange = { catExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = catExpanded,
                                onDismissRequest = { catExpanded = false }
                            ) {
                                listOf("Cardiology", "Nutrition", "Mental Health", "Sleep & Recovery", "Immunology", "Preventive Care").forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c) },
                                        onClick = {
                                            category = c
                                            catExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = excerpt,
                            onValueChange = { excerpt = it },
                            label = { Text("Short Summary / Excerpt") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text("Detailed Guide & Clinical Takeaways") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 5
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreateArticle(title, excerpt, content, category, currentUser.fullName)
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary)
                ) {
                    Text("Publish Guide")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
