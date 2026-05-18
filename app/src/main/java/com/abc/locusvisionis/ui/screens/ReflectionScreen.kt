package com.abc.locusvisionis.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abc.locusvisionis.data.database.AppDatabase
import com.abc.locusvisionis.data.models.Reflection
import com.abc.locusvisionis.ui.theme.DashboardTheme
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

private data class ReflectionFormState(
    val title: String = "",
    val content: String = "",
    val mood: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReflectionScreen(
    currentUser: FirebaseUser?,
    firestore: FirebaseFirestore
) {
    val context = LocalContext.current
    val appColors = DashboardTheme.colors
    val moods = remember {
        listOf("\uD83D\uDE0A", "\uD83D\uDE0C", "\uD83E\uDD14", "\uD83D\uDE22", "\uD83D\uDE24", "\uD83D\uDE34")
    }
    val database = remember(context) { AppDatabase.getDatabase(context) }
    val reflectionDao = remember(database) { database.reflectionDao() }
    val reflections by reflectionDao.observeAll().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    var selectedMood by remember { mutableStateOf(moods.first()) }
    var formDialogTarget by remember { mutableStateOf<Reflection?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Reflection?>(null) }
    var publishingReflectionId by remember { mutableStateOf<Long?>(null) }

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
            text = "Create, edit, and keep reflections on this phone. Publish to Firestore only when you choose.",
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
                    text = "Mood for your next reflection",
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

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { showCreateDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = appColors.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Write New Reflection")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Saved On This Phone",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = appColors.textPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (reflections.isEmpty()) {
            ReflectionEmptyState()
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reflections, key = { it.id }) { reflection ->
                    ReflectionCard(
                        reflection = reflection,
                        isPublishing = publishingReflectionId == reflection.id,
                        canPublish = currentUser != null,
                        onEdit = { formDialogTarget = reflection },
                        onDelete = { deleteTarget = reflection },
                        onPublish = {
                            if (currentUser == null) {
                                Toast.makeText(
                                    context,
                                    "Sign in first to publish reflections.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@ReflectionCard
                            }

                            publishingReflectionId = reflection.id
                            publishReflection(
                                firestore = firestore,
                                currentUser = currentUser,
                                reflection = reflection,
                                onSuccess = {
                                    publishingReflectionId = null
                                    Toast.makeText(
                                        context,
                                        "Reflection published to Firestore.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onError = { message ->
                                    publishingReflectionId = null
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        ReflectionEditorDialog(
            title = "New Reflection",
            initialState = ReflectionFormState(mood = selectedMood),
            moods = moods,
            onDismiss = { showCreateDialog = false },
            onSubmit = { formState ->
                coroutineScope.launch {
                    reflectionDao.upsert(
                        Reflection(
                            title = formState.title,
                            content = formState.content,
                            mood = formState.mood
                        )
                    )
                    showCreateDialog = false
                    selectedMood = formState.mood
                    Toast.makeText(context, "Reflection saved on this phone.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (formDialogTarget != null) {
        val reflection = formDialogTarget!!
        ReflectionEditorDialog(
            title = "Edit Reflection",
            initialState = ReflectionFormState(
                title = reflection.title,
                content = reflection.content,
                mood = reflection.mood
            ),
            moods = moods,
            onDismiss = { formDialogTarget = null },
            onSubmit = { formState ->
                coroutineScope.launch {
                    reflectionDao.upsert(
                        reflection.copy(
                            title = formState.title,
                            content = formState.content,
                            mood = formState.mood
                        )
                    )
                    formDialogTarget = null
                    selectedMood = formState.mood
                    Toast.makeText(context, "Reflection updated.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (deleteTarget != null) {
        val reflection = deleteTarget!!
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Reflection") },
            text = { Text("Delete \"${reflection.title}\" from this phone? This cannot be undone.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.accentRed),
                    onClick = {
                        coroutineScope.launch {
                            reflectionDao.deleteById(reflection.id)
                            deleteTarget = null
                            Toast.makeText(context, "Reflection deleted.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ReflectionCard(
    reflection: Reflection,
    isPublishing: Boolean,
    canPublish: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPublish: () -> Unit
) {
    val appColors = DashboardTheme.colors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reflection.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = reflection.date.formatReflectionDate(),
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textLight
                    )
                }

                Text(
                    text = reflection.mood,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Text(
                text = reflection.content,
                style = MaterialTheme.typography.bodyMedium,
                color = appColors.textSecondary,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit")
                }

                TextButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete")
                }

                Button(
                    onClick = onPublish,
                    enabled = canPublish && !isPublishing,
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.secondary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isPublishing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isPublishing) "Publishing..." else "Publish")
                }
            }
        }
    }
}

@Composable
private fun ReflectionEditorDialog(
    title: String,
    initialState: ReflectionFormState,
    moods: List<String>,
    onDismiss: () -> Unit,
    onSubmit: (ReflectionFormState) -> Unit
) {
    var formState by remember(title, initialState) { mutableStateOf(initialState) }
    var validationMessage by remember(title) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = formState.title,
                    onValueChange = {
                        formState = formState.copy(title = it)
                    },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = formState.content,
                    onValueChange = {
                        formState = formState.copy(content = it)
                    },
                    label = { Text("Reflection") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )

                Text(
                    text = "Mood",
                    style = MaterialTheme.typography.labelLarge
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    moods.forEach { mood ->
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    if (formState.mood == mood) {
                                        DashboardTheme.colors.primary.copy(alpha = 0.12f)
                                    } else {
                                        Color.Transparent
                                    },
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    formState = formState.copy(mood = mood)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mood,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }

                if (!validationMessage.isNullOrBlank()) {
                    Text(
                        text = validationMessage.orEmpty(),
                        color = DashboardTheme.colors.accentRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedTitle = formState.title.trim()
                    val trimmedContent = formState.content.trim()
                    val message = when {
                        trimmedTitle.isBlank() -> "Title is required."
                        trimmedContent.isBlank() -> "Reflection text is required."
                        formState.mood.isBlank() -> "Choose a mood."
                        else -> null
                    }

                    if (message != null) {
                        validationMessage = message
                    } else {
                        validationMessage = null
                        onSubmit(
                            formState.copy(
                                title = trimmedTitle,
                                content = trimmedContent
                            )
                        )
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ReflectionEmptyState() {
    val appColors = DashboardTheme.colors
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "No reflections yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimary
            )
            Text(
                text = "Write your first reflection and it will be stored locally on this device.",
                style = MaterialTheme.typography.bodyMedium,
                color = appColors.textSecondary
            )
        }
    }
}

private fun publishReflection(
    firestore: FirebaseFirestore,
    currentUser: FirebaseUser,
    reflection: Reflection,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val payload = hashMapOf(
        "authorUid" to currentUser.uid,
        "authorName" to (currentUser.displayName ?: currentUser.email ?: "Anonymous"),
        "authorEmail" to currentUser.email.orEmpty(),
        "title" to reflection.title,
        "content" to reflection.content,
        "mood" to reflection.mood,
        "localReflectionId" to reflection.id,
        "localDateMillis" to reflection.date.time,
        "publishedAt" to FieldValue.serverTimestamp()
    )

    firestore.collection("public_reflections")
        .add(payload)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { error ->
            onError(error.localizedMessage ?: "Could not publish reflection.")
        }
}

private fun Date.formatReflectionDate(): String {
    return SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(this)
}
