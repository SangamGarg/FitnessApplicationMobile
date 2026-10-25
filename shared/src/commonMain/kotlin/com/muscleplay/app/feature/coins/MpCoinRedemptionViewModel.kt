package com.muscleplay.app.feature.coins

import androidx.lifecycle.ViewModel
import com.muscleplay.app.feature.coins.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MpCoinRedemptionUiState(
    val availableCoins: Int = 1250,
    val selectedCoins: Int = 1000,
    val selectedChannel: PayoutChannel = PayoutChannel.UPI,
    val upiForm: UpiFormData = UpiFormData(
        fullName = "Alex Fitness",
        upiId = "alex.fitness@okaxis",
        confirmUpiId = "alex.fitness@okaxis"
    ),
    val bankForm: BankFormData = BankFormData(
        accountHolderName = "Alex Fitness",
        accountNumber = "987654321012",
        confirmAccountNumber = "987654321012",
        ifscCode = "HDFC0001234"
    ),
    val paypalForm: PaypalFormData = PaypalFormData(
        fullName = "Alex Fitness",
        paypalEmail = "alex.fitness@gmail.com",
        confirmEmail = "alex.fitness@gmail.com"
    ),
    val stripeForm: StripeFormData = StripeFormData(
        isConnected = true,
        accountId = "acct_mp_98214"
    ),
    val saveAsDefault: Boolean = true,
    val is24HourLimitReached: Boolean = false,
    val isBiometricModalShowing: Boolean = false,
    val isSuccessModalShowing: Boolean = false,
    val recentTransaction: TransactionRecord? = null,
    val transactions: List<TransactionRecord> = sampleTransactionHistory
) {
    val isUpiValid: Boolean
        get() = upiForm.fullName.isNotBlank() &&
            upiForm.upiId.contains("@") &&
            (upiForm.confirmUpiId.isBlank() || upiForm.upiId == upiForm.confirmUpiId)

    val isBankValid: Boolean
        get() = bankForm.accountHolderName.isNotBlank() &&
            bankForm.accountNumber.length >= 8 &&
            (bankForm.confirmAccountNumber.isBlank() || bankForm.accountNumber == bankForm.confirmAccountNumber) &&
            bankForm.ifscCode.isNotBlank()

    val isPaypalValid: Boolean
        get() = paypalForm.fullName.isNotBlank() &&
            paypalForm.paypalEmail.contains("@") &&
            (paypalForm.confirmEmail.isBlank() || paypalForm.paypalEmail == paypalForm.confirmEmail)

    val isStripeValid: Boolean
        get() = stripeForm.isConnected

    val isFormValid: Boolean
        get() = when (selectedChannel) {
            PayoutChannel.UPI -> isUpiValid
            PayoutChannel.BANK_TRANSFER -> isBankValid
            PayoutChannel.PAYPAL -> isPaypalValid
            PayoutChannel.STRIPE -> isStripeValid
        }

    val isEligibleToRedeem: Boolean
        get() = selectedCoins >= 1000 &&
            selectedCoins <= availableCoins &&
            !is24HourLimitReached &&
            isFormValid

    val grossAmountRupees: Double
        get() = (selectedCoins / 100.0)

    val taxAmountRupees: Double
        get() = if (selectedCoins >= 10000) grossAmountRupees * 0.10 else 0.0

    val netAmountRupees: Double
        get() = (grossAmountRupees - taxAmountRupees).coerceAtLeast(0.0)
}

class MpCoinRedemptionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MpCoinRedemptionUiState())
    val uiState: StateFlow<MpCoinRedemptionUiState> = _uiState.asStateFlow()

    fun updateSelectedCoins(coins: Int) {
        val clamped = coins.coerceIn(1000, _uiState.value.availableCoins.coerceAtLeast(1000))
        _uiState.value = _uiState.value.copy(selectedCoins = clamped)
    }

    fun selectChannel(channel: PayoutChannel) {
        _uiState.value = _uiState.value.copy(selectedChannel = channel)
    }

    fun updateUpiForm(fullName: String? = null, upiId: String? = null, confirmUpiId: String? = null) {
        val current = _uiState.value.upiForm
        _uiState.value = _uiState.value.copy(
            upiForm = current.copy(
                fullName = fullName ?: current.fullName,
                upiId = upiId ?: current.upiId,
                confirmUpiId = confirmUpiId ?: current.confirmUpiId
            )
        )
    }

    fun updateBankForm(
        accountHolderName: String? = null,
        accountNumber: String? = null,
        confirmAccountNumber: String? = null,
        ifscCode: String? = null
    ) {
        val current = _uiState.value.bankForm
        _uiState.value = _uiState.value.copy(
            bankForm = current.copy(
                accountHolderName = accountHolderName ?: current.accountHolderName,
                accountNumber = accountNumber ?: current.accountNumber,
                confirmAccountNumber = confirmAccountNumber ?: current.confirmAccountNumber,
                ifscCode = ifscCode ?: current.ifscCode
            )
        )
    }

    fun updatePaypalForm(fullName: String? = null, paypalEmail: String? = null, confirmEmail: String? = null) {
        val current = _uiState.value.paypalForm
        _uiState.value = _uiState.value.copy(
            paypalForm = current.copy(
                fullName = fullName ?: current.fullName,
                paypalEmail = paypalEmail ?: current.paypalEmail,
                confirmEmail = confirmEmail ?: current.confirmEmail
            )
        )
    }

    fun toggleStripeConnect() {
        val current = _uiState.value.stripeForm
        _uiState.value = _uiState.value.copy(
            stripeForm = current.copy(
                isConnected = !current.isConnected,
                accountId = if (!current.isConnected) "acct_mp_98214" else ""
            )
        )
    }

    fun toggleSaveAsDefault(save: Boolean) {
        _uiState.value = _uiState.value.copy(saveAsDefault = save)
    }

    fun triggerBiometricAuthentication() {
        if (!_uiState.value.isEligibleToRedeem) return
        _uiState.value = _uiState.value.copy(isBiometricModalShowing = true)
    }

    fun dismissBiometricModal() {
        _uiState.value = _uiState.value.copy(isBiometricModalShowing = false)
    }

    fun confirmRedemption() {
        val state = _uiState.value
        val redeemedCoins = state.selectedCoins
        val newAvailable = (state.availableCoins - redeemedCoins).coerceAtLeast(0)

        val destinationMasked = when (state.selectedChannel) {
            PayoutChannel.UPI -> state.upiForm.upiId
            PayoutChannel.BANK_TRANSFER -> "A/C •••• ${state.bankForm.accountNumber.takeLast(4)}"
            PayoutChannel.PAYPAL -> state.paypalForm.paypalEmail
            PayoutChannel.STRIPE -> "Stripe (${state.stripeForm.accountId})"
        }

        val newRecord = TransactionRecord(
            id = "TXN-MP-${(10000..99999).random()}",
            dateIso = "Just Now",
            coinsRedeemed = redeemedCoins,
            grossAmount = "₹${state.grossAmountRupees}",
            netAmount = "₹${state.netAmountRupees}",
            payoutChannel = state.selectedChannel,
            destinationMasked = destinationMasked,
            status = TransactionStatus.PROCESSING,
            estimatedArrival = "1-2 Business Days"
        )

        _uiState.value = state.copy(
            availableCoins = newAvailable,
            selectedCoins = newAvailable.coerceAtLeast(1000),
            isBiometricModalShowing = false,
            isSuccessModalShowing = true,
            recentTransaction = newRecord,
            transactions = listOf(newRecord) + state.transactions
        )
    }

    fun dismissSuccessModal() {
        _uiState.value = _uiState.value.copy(isSuccessModalShowing = false)
    }
}
