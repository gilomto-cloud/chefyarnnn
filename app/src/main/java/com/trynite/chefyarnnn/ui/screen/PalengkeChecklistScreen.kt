package com.trynite.chefyarnnn.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.consumeWindowInsets
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
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.AddShoppingCart
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.trynite.chefyarnnn.data.repository.PalengkeRepository
import com.trynite.chefyarnnn.model.MarketCategory
import com.trynite.chefyarnnn.model.PalengkeItem
import com.trynite.chefyarnnn.ui.theme.chefTextFieldColors
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PalengkeChecklistScreen(
    palengkeRepository: PalengkeRepository
) {
    val palengkeItems by palengkeRepository.items.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var itemForPriceEdit by remember { mutableStateOf<PalengkeItem?>(null) }

    val groupedItems = remember(palengkeItems) {
        palengkeItems.groupBy { it.category }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
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
                .consumeWindowInsets(innerPadding)
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
                            val checkedCount = palengkeItems.count { it.isChecked }
                            Text(
                                text = "$checkedCount sa ${palengkeItems.size} item nabili",
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
                            enabled = palengkeItems.any { it.isChecked },
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
                            onClick = { showClearAllDialog = true },
                            enabled = palengkeItems.isNotEmpty(),
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

            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(key = "price_calculator") {
                    PalengkePriceSummary(items = palengkeItems)
                }

                if (palengkeItems.isEmpty()) {
                    item(key = "empty_list") {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AddShoppingCart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Wala pang laman ang listahan",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Magdagdag ng sangkap o ilagay ang mga kailangan sa isang resipe.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(onClick = { showAddDialog = true }) {
                                    Icon(Icons.Rounded.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Magdagdag ng sangkap")
                                }
                            }
                        }
                    }
                } else {
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
                                    onRemove = { palengkeRepository.removeItem(it.id) },
                                    onEditPrice = { itemForPriceEdit = it }
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
            onConfirm = { name, amount, unit, category, price ->
                palengkeRepository.addItem(
                    name = name,
                    amount = amount,
                    unit = unit,
                    category = category,
                    price = price,
                    recipeOriginTitle = "Custom Entry"
                )
                showAddDialog = false
            }
        )
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Linisin ang buong listahan?") },
            text = { Text("Mawawala ang lahat ng sangkap at impormasyong nailagay mo sa Palengke.") },
            confirmButton = {
                Button(
                    onClick = {
                        palengkeRepository.clearAllItems()
                        showClearAllDialog = false
                    }
                ) {
                    Text("Linisin lahat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Kanselahin")
                }
            }
        )
    }

    itemForPriceEdit?.let { item ->
        EditItemPriceDialog(
            item = item,
            onDismiss = { itemForPriceEdit = null },
            onConfirm = { price ->
                palengkeRepository.updateItemPrice(item.id, price)
                itemForPriceEdit = null
            }
        )
    }
}

@Composable
private fun PalengkePriceSummary(items: List<PalengkeItem>) {
    val total = items.sumItemPrices()
    val purchasedTotal = items.filter { it.isChecked }.sumItemPrices()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Calculate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Kuwentador ng Gastos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Kabuuang may presyo", color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text(
                    formatPeso(total),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Nabili na", color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text(
                    formatPeso(purchasedTotal),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Text(
                text = "Ilagay ang presyo ng bawat item; awtomatikong kakalkulahin ang kabuuan.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

private fun List<PalengkeItem>.sumItemPrices(): BigDecimal =
    fold(BigDecimal.ZERO) { total, item ->
        total + (item.price.toBigDecimalOrNull()?.takeIf { it >= BigDecimal.ZERO } ?: BigDecimal.ZERO)
    }

private fun formatPeso(value: BigDecimal): String =
    NumberFormat.getCurrencyInstance(Locale("en", "PH")).format(value)

@Composable
fun PalengkeItemRow(
    item: PalengkeItem,
    onToggle: (PalengkeItem) -> Unit,
    onRemove: (PalengkeItem) -> Unit,
    onEditPrice: (PalengkeItem) -> Unit
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
                TextButton(
                    onClick = { onEditPrice(item) },
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.price.toBigDecimalOrNull()?.let(::formatPeso) ?: "＋ Ilagay ang presyo",
                        style = MaterialTheme.typography.labelLarge
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
    onConfirm: (name: String, amount: String, unit: String, category: MarketCategory, price: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("pc") }
    var price by remember { mutableStateOf("") }
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

                OutlinedTextField(
                    value = price,
                    onValueChange = { value ->
                        if (value.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) price = value
                    },
                    label = { Text("Presyo (₱, optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = chefTextFieldColors()
                )

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
                        onConfirm(name.trim(), amount.trim(), unit.trim(), selectedCategory, price)
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

@Composable
private fun EditItemPriceDialog(
    item: PalengkeItem,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var price by remember(item.id) { mutableStateOf(item.price) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Presyo ng ${item.name}") },
        text = {
            OutlinedTextField(
                value = price,
                onValueChange = { value ->
                    if (value.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) price = value
                },
                label = { Text("Presyo (₱)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = chefTextFieldColors()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(price) }) {
                Text("I-save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Kanselahin")
            }
        }
    )
}
