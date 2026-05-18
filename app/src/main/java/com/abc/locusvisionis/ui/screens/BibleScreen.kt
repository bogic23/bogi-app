package com.abc.locusvisionis.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.abc.locusvisionis.data.bible.BibleContent
import com.abc.locusvisionis.data.bible.BibleContentItem
import com.abc.locusvisionis.ui.theme.DashboardTheme

enum class BibleLanguage {
    ENGLISH,
    INDONESIAN
}

@Composable
fun BibleScreen() {
    val appColors = DashboardTheme.colors
    var searchQuery by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf(BibleLanguage.ENGLISH) }
    val bibleList = remember { BibleContent.verses }
    val verseOfTheDay = bibleList.firstOrNull()
    val filteredVerses = remember(searchQuery, selectedLanguage, bibleList) {
        val keyword = searchQuery.trim()
        if (keyword.isEmpty()) {
            bibleList
        } else {
            bibleList.filter { item ->
                item.book.contains(keyword, ignoreCase = true) ||
                    item.reference.contains(keyword, ignoreCase = true) ||
                    item.getText(selectedLanguage).contains(keyword, ignoreCase = true)
            }
        }
    }
    val listState = rememberLazyListState()

    LaunchedEffect(searchQuery, selectedLanguage) {
        listState.scrollToItem(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        verseOfTheDay?.let { verse ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = appColors.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = appColors.accentGold,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Verse of the Day",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "\"${verse.getText(selectedLanguage)}\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${verse.reference} • ${verse.getTranslationLabel(selectedLanguage)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = appColors.accentGold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Bible Library",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = appColors.textPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Edit your verses in `data/bible/BibleContent.kt`, then search by book, reference, or verse text here.",
            style = MaterialTheme.typography.bodyMedium,
            color = appColors.textSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedLanguage == BibleLanguage.ENGLISH,
                onClick = { selectedLanguage = BibleLanguage.ENGLISH },
                label = { Text("English") }
            )
            FilterChip(
                selected = selectedLanguage == BibleLanguage.INDONESIAN,
                onClick = { selectedLanguage = BibleLanguage.INDONESIAN },
                label = { Text("Indonesia") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            label = {
                Text(
                    text = if (selectedLanguage == BibleLanguage.ENGLISH) {
                        "Search Bible"
                    } else {
                        "Cari Alkitab"
                    }
                )
            },
            placeholder = {
                Text(
                    text = if (selectedLanguage == BibleLanguage.ENGLISH) {
                        "Try John, 3:16, love, strength..."
                    } else {
                        "Coba John, 3:16, kasih, kuat..."
                    }
                )
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (selectedLanguage == BibleLanguage.ENGLISH) {
                "${filteredVerses.size} verse${if (filteredVerses.size == 1) "" else "s"} found"
            } else {
                "${filteredVerses.size} ayat ditemukan"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = appColors.textSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(state = listState) {
            items(
                items = filteredVerses,
                key = { verse -> verse.reference }
            ) {
                BibleBookCard(
                    book = it,
                    selectedLanguage = selectedLanguage
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (filteredVerses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = appColors.surface)
                    ) {
                        Text(
                            text = if (selectedLanguage == BibleLanguage.ENGLISH) {
                                "No verses matched your search. Update the content file or try another keyword."
                            } else {
                                "Tidak ada ayat yang cocok. Ubah file konten atau coba kata kunci lain."
                            },
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = appColors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BibleBookCard(
    book: BibleContentItem,
    selectedLanguage: BibleLanguage
) {
    var isExpanded by remember { mutableStateOf(false) }
    val appColors = DashboardTheme.colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.fillMaxWidth(0.78f)) {
                    Text(
                        text = book.reference,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary
                    )
                    Text(
                        text = book.getTranslationLabel(selectedLanguage),
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textSecondary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = if (book.isFavorite) {
                                Icons.Default.Favorite
                            } else {
                                Icons.Default.FavoriteBorder
                            },
                            contentDescription = "Favorite",
                            tint = if (book.isFavorite) appColors.accentRed else appColors.textSecondary
                        )
                    }
                    Icon(
                        imageVector = if (isExpanded) {
                            Icons.Default.ExpandLess
                        } else {
                            Icons.Default.ExpandMore
                        },
                        contentDescription = null,
                        tint = appColors.textSecondary
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = book.getText(selectedLanguage),
                    style = MaterialTheme.typography.bodyLarge,
                    color = appColors.textSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { }) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (selectedLanguage == BibleLanguage.ENGLISH) "Notes" else "Catatan")
                    }
                    TextButton(onClick = { }) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (selectedLanguage == BibleLanguage.ENGLISH) "Share" else "Bagikan")
                    }
                }
            }
        }
    }
}

private fun BibleContentItem.getText(language: BibleLanguage): String {
    return when (language) {
        BibleLanguage.ENGLISH -> englishText
        BibleLanguage.INDONESIAN -> indonesianText
    }
}

private fun BibleContentItem.getTranslationLabel(language: BibleLanguage): String {
    return when (language) {
        BibleLanguage.ENGLISH -> "English"
        BibleLanguage.INDONESIAN -> "Indonesia"
    }
}
