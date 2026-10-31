package com.trynite.chefyarnnn.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trynite.chefyarnnn.data.repository.ChefAiRepository
import com.trynite.chefyarnnn.data.repository.RecipeRepository
import com.trynite.chefyarnnn.model.ChatMessage
import com.trynite.chefyarnnn.model.ChatSender
import com.trynite.chefyarnnn.ui.theme.chefTextFieldColors
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun AiChefChatScreen(
    geminiRepository: ChefAiRepository,
    recipeRepository: RecipeRepository,
    onViewRecipe: (String) -> Unit,
    initialPrompt: String = ""
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputMessage by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                id = "welcome_msg",
                sender = ChatSender.PINOY_CHEF,
                content = "Kamusta anak/i-tito! Ako si Nanay/Tito Chef. May tanong ka ba sa pagluluto ng Adobo, Sinigang, o paano mag-gisa at i-tantsa ang lasa? Tanong ka lang sa akin!"
            )
        )
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessageText(textToSend: String) {
        val text = textToSend.trim()
        if (text.isBlank() || isThinking) return

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = ChatSender.USER,
            content = text
        )
        messages.add(userMsg)
        inputMessage = ""
        isThinking = true

        coroutineScope.launch {
            try {
                val reply = geminiRepository.chatWithNanayTito(
                    userMessage = text,
                    chatHistory = messages
                )
                val chefMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = ChatSender.PINOY_CHEF,
                    content = reply
                )
                messages.add(chefMsg)
            } catch (e: Exception) {
                e.printStackTrace()
                messages.add(
                    ChatMessage(
                        id = UUID.randomUUID().toString(),
                        sender = ChatSender.PINOY_CHEF,
                        content = "Hindi ako makakonekta sa Firebase AI. ${e.localizedMessage ?: e.javaClass.simpleName}"
                    )
                )
            } finally {
                isThinking = false
            }
        }
    }

    LaunchedEffect(initialPrompt) {
        if (initialPrompt.isNotBlank()) {
            sendMessageText(initialPrompt)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Chat Header
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Face,
                        contentDescription = "Nanay/Tito Chef",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Nanay / Tito Chef",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Pinoy Kitchen Companion & Cooking Assistant",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageItem(
                    message = msg,
                    onViewRecipe = onViewRecipe
                )
            }

            if (isThinking) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nagiisip si Nanay/Tito Chef...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Input Field
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value = inputMessage,
                    onValueChange = { inputMessage = it },
                    placeholder = { Text("Magtanong kay Nanay/Tito Chef...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = chefTextFieldColors(),
                    maxLines = 4
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { sendMessageText(inputMessage) },
                    enabled = inputMessage.isNotBlank() && !isThinking,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputMessage.isNotBlank() && !isThinking)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onViewRecipe: (String) -> Unit
) {
    val isUser = message.sender == ChatSender.USER

    Row(
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Face,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth(0.82f)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (message.recipeAttachment != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onViewRecipe(message.recipeAttachment.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tingnan ang Resipe")
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
