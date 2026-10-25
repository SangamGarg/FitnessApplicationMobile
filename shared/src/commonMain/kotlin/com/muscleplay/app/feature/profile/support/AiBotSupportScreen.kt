package com.muscleplay.app.feature.profile.support

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val DarkBackground = Color(0xFF0F0F17)

@Composable
fun AiBotSupportScreen(
    onBack: () -> Unit,
    onNavigateRoute: (String) -> Unit = {},
    viewModel: AiChatViewModel = viewModel { AiChatViewModel() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var isMenuExpanded by remember { mutableStateOf(false) }

    // Auto scroll to latest message when messages list changes
    LaunchedEffect(uiState.messages.size, uiState.isTyping) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(OrangePrimary.copy(alpha = 0.25f))
                                        .border(1.dp, OrangePrimary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.SmartToy, contentDescription = "Bot Avatar", tint = OrangeSecondary, modifier = Modifier.size(22.dp))
                                }
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50))
                                        .border(1.dp, DarkBackground, CircleShape)
                                        .align(Alignment.BottomEnd)
                                )
                            }

                            Column {
                                Text("MusclePlay AI Support", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                Text("Online • 24/7 Instant Assistant", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { isMenuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
                            }
                            DropdownMenu(
                                expanded = isMenuExpanded,
                                onDismissRequest = { isMenuExpanded = false },
                                modifier = Modifier.background(Color(0xFF181824))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Clear Chat History", color = Color.White) },
                                    leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = OrangeSecondary) },
                                    onClick = {
                                        viewModel.clearChat()
                                        isMenuExpanded = false
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Messages Scrollable Area
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(uiState.messages, key = { it.id }) { message ->
                        ChatBubbleItem(
                            message = message,
                            onActionClick = { route -> onNavigateRoute(route) }
                        )
                    }

                    if (uiState.isTyping) {
                        item {
                            TypingIndicatorBubble()
                        }
                    }
                }

                // Quick Prompt Suggestion Chips Row
                if (uiState.suggestionChips.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        items(uiState.suggestionChips) { chip ->
                            SuggestionChip(
                                onClick = {
                                    viewModel.sendMessage(chip)
                                },
                                label = { Text(chip, fontSize = 12.sp, color = Color.White) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = Color.White.copy(alpha = 0.08f)
                                ),
                                border = SuggestionChipDefaults.suggestionChipBorder(
                                    enabled = true,
                                    borderColor = OrangePrimary.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }
                }

                // Sticky Bottom Input Bar
                Surface(
                    color = Color(0xFF181824).copy(alpha = 0.95f),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.attachScreenshot("screenshot_log.png")
                                coroutineScope.launch {
                                    viewModel.sendMessage("Attached screenshot for inspection.")
                                }
                            }
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Attach", tint = OrangeSecondary)
                        }

                        OutlinedTextField(
                            value = uiState.inputText,
                            onValueChange = { viewModel.updateInputText(it) },
                            placeholder = { Text("Ask AI Coach anything...", color = Color.White.copy(alpha = 0.4f)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = OrangePrimary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                            )
                        )

                        IconButton(
                            onClick = { viewModel.sendMessage() },
                            enabled = uiState.inputText.isNotBlank(),
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (uiState.inputText.isNotBlank()) OrangePrimary else Color.White.copy(alpha = 0.12f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (uiState.inputText.isNotBlank()) Color.White else Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(
    message: ChatMessage,
    onActionClick: (String) -> Unit
) {
    val isUser = message.sender == MessageSender.USER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isUser) OrangePrimary.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.08f),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 20.dp
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isUser) OrangePrimary else Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.SmartToy, contentDescription = null, tint = OrangeSecondary, modifier = Modifier.size(16.dp))
                        Text("AI Coach", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = OrangeSecondary)
                    }
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )

                message.actionText?.let { action ->
                    message.actionRoute?.let { route ->
                        Button(
                            onClick = { onActionClick(route) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(action, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun TypingIndicatorBubble() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
    ) {
        Surface(
            color = Color.White.copy(alpha = 0.08f),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(12.dp), color = OrangeSecondary, strokeWidth = 2.dp)
                Text("AI is typing...", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}
