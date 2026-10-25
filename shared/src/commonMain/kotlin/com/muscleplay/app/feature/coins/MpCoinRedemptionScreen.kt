package com.muscleplay.app.feature.coins

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.muscleplay.app.feature.coins.model.PayoutChannel
import com.muscleplay.app.feature.coins.model.TransactionRecord
import com.muscleplay.app.feature.coins.model.TransactionStatus

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val DarkBackground = Color(0xFF0F0F17)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MpCoinRedemptionScreen(
    onBack: () -> Unit,
    viewModel: MpCoinRedemptionViewModel = viewModel { MpCoinRedemptionViewModel() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showPolicyBottomSheet by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Cashout, 1 = History

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Redeem MP Coins", fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showPolicyBottomSheet = true }) {
                            Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Rules & Policy", tint = OrangeSecondary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Main Tab Switcher (Redeem Cash vs History)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(4.dp)
            ) {
                Surface(
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedTab == 0) OrangePrimary else Color.Transparent
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Coin Cashout", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    }
                }

                Surface(
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedTab == 1) OrangePrimary else Color.Transparent
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Payout History", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // TAB 0: CASHOUT FLOW
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    // Balance & Threshold Progress Card
                    item {
                        GlassCoinCard {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "AVAILABLE BALANCE",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = OrangeSecondary,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.2.sp
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = OrangeSecondary, modifier = Modifier.size(28.dp))
                                            Text(
                                                text = "${uiState.availableCoins} MP",
                                                style = MaterialTheme.typography.headlineLarge,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Surface(
                                        color = if (uiState.availableCoins >= 1000) Color(0xFF4CAF50).copy(alpha = 0.2f) else OrangePrimary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (uiState.availableCoins >= 1000) Color(0xFF4CAF50) else OrangePrimary)
                                    ) {
                                        Text(
                                            text = if (uiState.availableCoins >= 1000) "Eligible 🎉" else "1,000 MP Min",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (uiState.availableCoins >= 1000) Color(0xFF4CAF50) else OrangePrimary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                val thresholdProgress = (uiState.availableCoins.toFloat() / 1000f).coerceIn(0f, 1f)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    LinearProgressIndicator(
                                        progress = { thresholdProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = OrangePrimary,
                                        trackColor = Color.White.copy(alpha = 0.1f)
                                    )
                                    Text(
                                        text = "Minimum redemption threshold: 1,000 MP Coins (₹10.00)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }

                    // Amount Selection & Financial Breakdown
                    item {
                        GlassCoinCard {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = "Select Coins to Convert",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf(1000, 2000, uiState.availableCoins).distinct().forEach { amt ->
                                        FilterChip(
                                            selected = uiState.selectedCoins == amt,
                                            onClick = { viewModel.updateSelectedCoins(amt) },
                                            label = { Text(if (amt == uiState.availableCoins) "Max ($amt)" else "$amt MP") },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = OrangePrimary,
                                                selectedLabelColor = Color.White,
                                                containerColor = Color.White.copy(alpha = 0.08f),
                                                labelColor = Color.White.copy(alpha = 0.8f)
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                                // Financial Breakdown
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    BreakdownRow("Gross Conversion Value", "₹${uiState.grossAmountRupees}")
                                    BreakdownRow("Processing Fee", "₹0.00 (Free)")
                                    BreakdownRow("Estimated Tax / TDS", "₹${uiState.taxAmountRupees}")
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Net Payout Amount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("₹${uiState.netAmountRupees}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = OrangeSecondary)
                                    }
                                }
                            }
                        }
                    }

                    // Payout Channel Selector & Dynamic Form
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Choose Payout Destination",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            // Channel Pills (Scrollable LazyRow to prevent squishing)
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                items(PayoutChannel.entries) { channel ->
                                    FilterChip(
                                        selected = uiState.selectedChannel == channel,
                                        onClick = { viewModel.selectChannel(channel) },
                                        label = { Text(channel.label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = OrangeSecondary,
                                            selectedLabelColor = Color.White,
                                            containerColor = Color.White.copy(alpha = 0.08f),
                                            labelColor = Color.White.copy(alpha = 0.8f)
                                        )
                                    )
                                }
                            }

                            // Dynamic Channel Form Container
                            GlassCoinCard {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    when (uiState.selectedChannel) {
                                        PayoutChannel.UPI -> {
                                            OutlinedTextField(
                                                value = uiState.upiForm.fullName,
                                                onValueChange = { viewModel.updateUpiForm(fullName = it) },
                                                label = { Text("Account Legal Name") },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = inputColors()
                                            )
                                            OutlinedTextField(
                                                value = uiState.upiForm.upiId,
                                                onValueChange = { viewModel.updateUpiForm(upiId = it) },
                                                label = { Text("UPI ID / VPA (e.g. name@upi)") },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = inputColors()
                                            )
                                            OutlinedTextField(
                                                value = uiState.upiForm.confirmUpiId,
                                                onValueChange = { viewModel.updateUpiForm(confirmUpiId = it) },
                                                label = { Text("Confirm UPI ID") },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = inputColors()
                                            )
                                        }

                                        PayoutChannel.BANK_TRANSFER -> {
                                            OutlinedTextField(
                                                value = uiState.bankForm.accountHolderName,
                                                onValueChange = { viewModel.updateBankForm(accountHolderName = it) },
                                                label = { Text("Bank Account Holder Name") },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = inputColors()
                                            )
                                            OutlinedTextField(
                                                value = uiState.bankForm.accountNumber,
                                                onValueChange = { viewModel.updateBankForm(accountNumber = it) },
                                                label = { Text("Bank Account Number") },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = inputColors()
                                            )
                                            OutlinedTextField(
                                                value = uiState.bankForm.confirmAccountNumber,
                                                onValueChange = { viewModel.updateBankForm(confirmAccountNumber = it) },
                                                label = { Text("Confirm Account Number") },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = inputColors()
                                            )
                                            OutlinedTextField(
                                                value = uiState.bankForm.ifscCode,
                                                onValueChange = { viewModel.updateBankForm(ifscCode = it) },
                                                label = { Text("IFSC Code (e.g. HDFC0001234)") },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = inputColors()
                                            )
                                        }

                                        PayoutChannel.PAYPAL -> {
                                            OutlinedTextField(
                                                value = uiState.paypalForm.fullName,
                                                onValueChange = { viewModel.updatePaypalForm(fullName = it) },
                                                label = { Text("Full Legal Name") },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = inputColors()
                                            )
                                            OutlinedTextField(
                                                value = uiState.paypalForm.paypalEmail,
                                                onValueChange = { viewModel.updatePaypalForm(paypalEmail = it) },
                                                label = { Text("PayPal Registered Email") },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = inputColors()
                                            )
                                            OutlinedTextField(
                                                value = uiState.paypalForm.confirmEmail,
                                                onValueChange = { viewModel.updatePaypalForm(confirmEmail = it) },
                                                label = { Text("Confirm PayPal Email") },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = inputColors()
                                            )
                                        }

                                        PayoutChannel.STRIPE -> {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = if (uiState.stripeForm.isConnected) "Stripe Connected ✓" else "Stripe Connect Direct Deposit",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                    Text(
                                                        text = if (uiState.stripeForm.isConnected) "Account: ${uiState.stripeForm.accountId}" else "Connect your bank account globally via Stripe",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.White.copy(alpha = 0.6f)
                                                    )
                                                }

                                                Button(
                                                    onClick = { viewModel.toggleStripeConnect() },
                                                    colors = ButtonDefaults.buttonColors(containerColor = if (uiState.stripeForm.isConnected) Color(0xFF4CAF50) else OrangePrimary)
                                                ) {
                                                    Text(if (uiState.stripeForm.isConnected) "Disconnect" else "Connect Stripe", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    // Save default checkbox
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { viewModel.toggleSaveAsDefault(!uiState.saveAsDefault) }
                                    ) {
                                        Checkbox(
                                            checked = uiState.saveAsDefault,
                                            onCheckedChange = { viewModel.toggleSaveAsDefault(it) },
                                            colors = CheckboxDefaults.colors(checkedColor = OrangePrimary)
                                        )
                                        Text(
                                            text = "Save as default payout method",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Convert To Cash Action Button
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.triggerBiometricAuthentication() },
                                enabled = uiState.isEligibleToRedeem,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OrangePrimary,
                                    disabledContainerColor = Color.White.copy(alpha = 0.12f)
                                )
                            ) {
                                Text(
                                    text = "Convert to Cash (₹${uiState.netAmountRupees}) 🔥",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (uiState.isEligibleToRedeem) Color.White else Color.White.copy(alpha = 0.4f)
                                )
                            }

                            if (!uiState.isEligibleToRedeem) {
                                Text(
                                    text = when {
                                        uiState.selectedCoins < 1000 -> "⚠️ Minimum redemption threshold is 1,000 MP Coins."
                                        uiState.selectedCoins > uiState.availableCoins -> "⚠️ Selected amount exceeds your available balance."
                                        !uiState.isFormValid -> "⚠️ Please enter valid payout details for ${uiState.selectedChannel.label}."
                                        else -> "⚠️ Unable to process cashout request."
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OrangeSecondary,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // TAB 1: REDEMPTION HISTORY TAB
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(uiState.transactions, key = { it.id }) { tx ->
                        TransactionRowCard(record = tx)
                    }
                }
            }
        }
        }

        // Policy Sheet
        if (showPolicyBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPolicyBottomSheet = false },
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                containerColor = Color(0xFF181824)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "MP Coins Redemption Policy 📜",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PolicyBullet("Minimum Threshold", "Redemptions start at 1,000 MP Coins (1,000 MP = ₹10.00).")
                        PolicyBullet("Velocity Limit", "Maximum 1 cashout request allowed per 24-hour cycle.")
                        PolicyBullet("Identity Matching", "Payout destination legal name must match your profile name.")
                        PolicyBullet("Anti-Fraud Audit", "Coins accumulated via unnatural device acceleration or video/camera manipulation will be flagged for compliance review.")
                    }

                    Button(
                        onClick = { showPolicyBottomSheet = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Text("Understood", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Biometric Confirmation Dialog Modal
        if (uiState.isBiometricModalShowing) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissBiometricModal() },
                shape = RoundedCornerShape(28.dp),
                containerColor = Color(0xFF181824),
                icon = {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(OrangePrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = "Biometric", tint = OrangeSecondary, modifier = Modifier.size(36.dp))
                    }
                },
                title = { Text("Biometric Confirmation", fontWeight = FontWeight.Bold, color = Color.White) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Authenticate using Fingerprint or Face ID to authorize cashout of ${uiState.selectedCoins} MP Coins (₹${uiState.netAmountRupees}).", color = Color.White.copy(alpha = 0.85f))
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmRedemption() },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Text("Authorize Cashout", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissBiometricModal() }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                    }
                }
            )
        }

        // Success Confirmation Modal
        if (uiState.isSuccessModalShowing) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissSuccessModal() },
                shape = RoundedCornerShape(28.dp),
                containerColor = Color(0xFF181824),
                icon = {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = Color(0xFF4CAF50), modifier = Modifier.size(36.dp))
                    }
                },
                title = { Text("Redemption Requested! 🎉", fontWeight = FontWeight.Bold, color = Color.White) },
                text = {
                    uiState.recentTransaction?.let { tx ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Transaction ID: ${tx.id}", fontWeight = FontWeight.Bold, color = OrangeSecondary)
                            Text("Net Payout: ${tx.netAmount}", color = Color.White)
                            Text("Destination: ${tx.destinationMasked}", color = Color.White)
                            Text("Estimated Arrival: ${tx.estimatedArrival}", color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.dismissSuccessModal() },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
private fun BreakdownRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun PolicyBullet(title: String, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("•", color = OrangeSecondary, fontWeight = FontWeight.Bold)
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun TransactionRowCard(record: TransactionRecord) {
    GlassCoinCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(record.id, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                Text("${record.payoutChannel.label} • ${record.destinationMasked}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                Text(record.dateIso, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(record.netAmount, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = OrangeSecondary)
                Surface(
                    color = when (record.status) {
                        TransactionStatus.COMPLETED -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        TransactionStatus.PROCESSING, TransactionStatus.PENDING -> OrangeSecondary.copy(alpha = 0.2f)
                        else -> Color(0xFFFF5252).copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = record.status.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (record.status) {
                            TransactionStatus.COMPLETED -> Color(0xFF4CAF50)
                            TransactionStatus.PROCESSING, TransactionStatus.PENDING -> OrangeSecondary
                            else -> Color(0xFFFF5252)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassCoinCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.10f),
                        Color.White.copy(alpha = 0.03f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.30f),
                        OrangePrimary.copy(alpha = 0.20f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        content()
    }
}

@Composable
private fun inputColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = OrangePrimary,
    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
)
