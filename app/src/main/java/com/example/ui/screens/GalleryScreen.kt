package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.RecordingEntity
import com.example.ui.components.RecordingCard
import com.example.ui.theme.RecordRed
import com.example.ui.viewmodel.GallerySort
import com.example.ui.viewmodel.GalleryViewModel

@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    onNavigateToPlayer: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recordings by viewModel.recordings.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val isGridView by viewModel.isGridView.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()

    var showSortMenu by remember { mutableStateOf(false) }
    var recordingToRename by remember { mutableStateOf<RecordingEntity?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var recordingToDelete by remember { mutableStateOf<RecordingEntity?>(null) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "My Recordings",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${recordings.size} video${if (recordings.size != 1) "s" else ""}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // View Mode Toggle (Grid / List)
                IconButton(
                    onClick = { viewModel.toggleViewMode() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("gallery_view_mode_toggle")
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = "Switch view mode",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Sort Menu
                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("gallery_sort_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort recordings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Newest first") },
                            onClick = {
                                viewModel.setSort(GallerySort.NEWEST)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Oldest first") },
                            onClick = {
                                viewModel.setSort(GallerySort.OLDEST)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Longest duration") },
                            onClick = {
                                viewModel.setSort(GallerySort.DURATION_HIGH)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Largest size") },
                            onClick = {
                                viewModel.setSort(GallerySort.SIZE_HIGH)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search recordings...", fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gallery_search_input")
        )

        // Multi-select Action Bar
        AnimatedVisibility(visible = selectedIds.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(RecordRed.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedIds.size} selected",
                    color = RecordRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { viewModel.clearSelection() }) {
                        Text("Cancel", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { showBatchDeleteConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = RecordRed)
                    ) {
                        Text("Delete", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Video Grid or List
        if (recordings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 96.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No recordings match \"$searchQuery\"" else "No recordings found",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().testTag("gallery_grid")
            ) {
                items(recordings, key = { it.id }) { rec ->
                    RecordingCard(
                        recording = rec,
                        isGrid = true,
                        isSelected = selectedIds.contains(rec.id),
                        onClick = { onNavigateToPlayer(rec.id) },
                        onRename = {
                            recordingToRename = rec
                            renameInput = rec.title
                        },
                        onDelete = { recordingToDelete = rec },
                        onShare = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "video/mp4"
                                putExtra(Intent.EXTRA_SUBJECT, rec.title)
                                putExtra(Intent.EXTRA_TEXT, "Watch screen recording: ${rec.title}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share recording"))
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(rec.id) }
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize().testTag("gallery_list")
            ) {
                items(recordings, key = { it.id }) { rec ->
                    RecordingCard(
                        recording = rec,
                        isGrid = false,
                        isSelected = selectedIds.contains(rec.id),
                        onClick = { onNavigateToPlayer(rec.id) },
                        onRename = {
                            recordingToRename = rec
                            renameInput = rec.title
                        },
                        onDelete = { recordingToDelete = rec },
                        onShare = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "video/mp4"
                                putExtra(Intent.EXTRA_SUBJECT, rec.title)
                                putExtra(Intent.EXTRA_TEXT, "Watch screen recording: ${rec.title}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share recording"))
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(rec.id) }
                    )
                }
            }
        }
    }

    // Rename Dialog
    recordingToRename?.let { rec ->
        AlertDialog(
            onDismissRequest = { recordingToRename = null },
            title = { Text("Rename Recording") },
            text = {
                TextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("rename_input_field")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            viewModel.renameRecording(rec.id, renameInput)
                        }
                        recordingToRename = null
                    },
                    modifier = Modifier.testTag("rename_save_button")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { recordingToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Single Delete Dialog
    recordingToDelete?.let { rec ->
        AlertDialog(
            onDismissRequest = { recordingToDelete = null },
            title = { Text("Delete Recording") },
            text = { Text("Are you sure you want to delete \"${rec.title}\"? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRecording(rec)
                        recordingToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RecordRed),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { recordingToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Batch Delete Dialog
    if (showBatchDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteConfirm = false },
            title = { Text("Delete Selected Videos") },
            text = { Text("Delete ${selectedIds.size} recordings permanently?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSelected()
                        showBatchDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RecordRed)
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
