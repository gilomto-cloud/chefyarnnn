package com.trynite.chefyarnnn.ui.screen

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.trynite.chefyarnnn.data.repository.PalengkeRepository
import com.trynite.chefyarnnn.model.MarketCategory
import com.trynite.chefyarnnn.model.PalengkeItem
import com.trynite.chefyarnnn.ui.theme.chefTextFieldColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PalengkeChecklistScreen(
    palengkeRepository: PalengkeRepository
) {
    val palengkeItems by palengkeRepository.items.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val groupedItems = remember(palengkeItems) {
        palengkeItems.groupBy { it.category }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add Item")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ShoppingBag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Smart Palengke Checklist",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Kabuuan: ${palengkeItems.size} | Nabili na: ${palengkeItems.count { it.isChecked }}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { palengkeRepository.clearCheckedItems() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteSweep,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Alisin ang Nabili")
                        }

                        OutlinedButton(
                            onClick = { palengkeRepository.clearAllItems() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ClearAll,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Linisin Lahat")
                        }
                    }
                }
            }

            if (palengkeItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Walang laman ang palengke list! Pindutin ang '+' para magdagdag ng sangkap.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    MarketCategory.entries.forEach { category ->
                        val categoryItems = groupedItems[category] ?: emptyList()
                        if (categoryItems.isNotEmpty()) {
                            item(key = "header_${category.name}") {
                                Text(
                                    text = category.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            items(categoryItems, key = { it.id }) { palengkeItem ->
                                PalengkeItemRow(
                                    item = palengkeItem,
                                    onToggle = { palengkeRepository.toggleItemChecked(it.id) },
                                    onRemove = { palengkeRepository.removeItem(it.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddItemDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, amount, unit, category ->
                palengkeRepository.addItem(
                    name = name,
                    amount = amount,
                    unit = unit,
                    category = category,
                    recipeOriginTitle = "Custom Entry"
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PalengkeItemRow(
    item: PalengkeItem,
    onToggle: (PalengkeItem) -> Unit,
    onRemove: (PalengkeItem) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isChecked)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isChecked) 0.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(item) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onToggle(item) },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${item.name} (${item.amount} ${item.unit})",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.Medium,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (item.isChecked)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                if (!item.recipeOriginTitle.isNullOrBlank()) {
                    Text(
                        text = "Mula sa: ${item.recipeOriginTitle}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }

            IconButton(onClick = { onRemove(item) }) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, amount: String, unit: String, category: MarketCategory) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("pc") }
    var selectedCategory by remember { mutableStateOf(MarketCategory.GULAYAN) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Magdagdag sa Palengke List") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.imePadding()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Pangalan ng Sangkap") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = chefTextFieldColors()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Dami (Amount)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = chefTextFieldColors()
                    )

                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit (g, kg, pc)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = chefTextFieldColors()
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategorya sa Palengke") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        colors = chefTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        MarketCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.displayName) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), amount.trim(), unit.trim(), selectedCategory)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Idagdag")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Kanselahin")
            }
        }
    )
}
