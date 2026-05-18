package com.abc.locusvisionis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abc.locusvisionis.ui.theme.DashboardTheme

data class ReflectionEntry(
    val title: String,
    val content: String,
    val mood: String,
    val date: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReflectionScreen() {
    val moods = listOf("\uD83D\uDE0A", "\uD83D\uDE0C", "\uD83E\uDD14", "\uD83D\uDE22", "\uD83D\uDE24", "\uD83D\uDE34")
    var selectedMood by remember { mutableStateOf("") }
    val appColors = DashboardTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Daily Reflection",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = appColors.textPrimary
        )
        Text(
            text = "Take a moment to reflect on your day",
            style = MaterialTheme.typography.bodyLarge,
            color = appColors.textSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "How are you feeling?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = appColors.textPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    moods.forEach { mood ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    if (selectedMood == mood) appColors.primary.copy(alpha = 0.12f)
                                    else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedMood = mood },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mood,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = appColors.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            androidx.compose.material3.Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Write New Reflection")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Previous Reflections",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = appColors.textPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(getSampleReflections()) { reflection ->
                ReflectionCard(reflection)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ReflectionCard(reflection: ReflectionEntry) {
    val appColors = DashboardTheme.colors

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Text(
                    text = reflection.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary
                )
                Text(
                    text = reflection.mood,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = reflection.content,
                style = MaterialTheme.typography.bodyMedium,
                color = appColors.textSecondary,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = reflection.date,
                style = MaterialTheme.typography.bodySmall,
                color = appColors.textLight
            )
        }
    }
}

fun getSampleReflections(): List<ReflectionEntry> = listOf(
    ReflectionEntry(
        "Grateful for Today",
        "Today was a productive day. I managed to complete all my tasks and even had time for a walk in the park...",
        "\uD83D\uDE0A",
        "March 15, 2024"
    ),
    ReflectionEntry(
        "Learning Patience",
        "Today taught me that patience is key. Some things take time, and that's okay...",
        "\uD83E\uDD14",
        "March 14, 2024"
    ),
    ReflectionEntry(
        "Finding Peace",
        "Spent some quiet time reading and meditating. Found peace in the simple moments...",
        "\uD83D\uDE0C",
        "March 13, 2024"
    )
)
