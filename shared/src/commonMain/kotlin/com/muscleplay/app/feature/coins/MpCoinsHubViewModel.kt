package com.muscleplay.app.feature.coins

import androidx.lifecycle.ViewModel
import com.muscleplay.app.feature.coins.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MpCoinsHubUiState(
    val activeTab: CoinsHubTab = CoinsHubTab.CASHOUT,
    val availableCoins: Int = 999,
    val selectedCoins: Int = 1000,
    val userReferralCode: String = "FIT-ALEX-99",
    val totalFriendsInvited: Int = 5,
    val referralCoinsEarned: Int = 750,
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
    val purchaseSuccessMessage: String? = null,
    val recentTransaction: TransactionRecord? = null,
    val coinPacks: List<CoinPackItem> = sampleCoinPacks,
    val referrals: List<ReferralItem> = sampleReferrals,
    val transactions: List<TransactionRecord> = sampleTransactionHistory
) {
    val upiRegex = Regex("^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$")
    val ifscRegex = Regex("^[A-Z]{4}0[A-Z0-9]{6}$")
    val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")

    val referralShareText: String
        get() = "Join me on MusclePlay to track workouts and earn real cash rewards! Use my code $userReferralCode for 100 free MP Coins: https://muscleplay.app/ref/$userReferralCode"

    val isUpiValid: Boolean
        get() = upiForm.fullName.isNotBlank() &&
            upiForm.upiId.matches(upiRegex) &&
            upiForm.upiId == upiForm.confirmUpiId

    val isBankValid: Boolean
        get() = bankForm.accountHolderName.isNotBlank() &&
            bankForm.accountNumber.length in 9..18 &&
            bankForm.accountNumber == bankForm.confirmAccountNumber &&
            bankForm.ifscCode.uppercase().matches(ifscRegex)

    val isPaypalValid: Boolean
        get() = paypalForm.fullName.isNotBlank() &&
            paypalForm.paypalEmail.matches(emailRegex) &&
            paypalForm.paypalEmail == paypalForm.confirmEmail

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
        get() = selectedCoins in 1000..availableCoins &&
            !is24HourLimitReached &&
            isFormValid

    val grossAmountRupees: Double
        get() = (selectedCoins / 100.0)

    val taxAmountRupees: Double
        get() = if (selectedCoins >= 10000) grossAmountRupees * 0.10 else 0.0

    val netAmountRupees: Double
        get() = (grossAmountRupees - taxAmountRupees).coerceAtLeast(0.0)
}

class MpCoinsHubViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MpCoinsHubUiState())
    val uiState: StateFlow<MpCoinsHubUiState> = _uiState.asStateFlow()

    fun selectTab(tab: CoinsHubTab) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }

    fun buyCoinPack(pack: CoinPackItem) {
        val current = _uiState.value
        val totalCredited = pack.coinsAmount + pack.bonusCoins
        val newAvailable = current.availableCoins + totalCredited

        val depositRecord = TransactionRecord(
            id = "BUY-MP-${(10000..99999).random()}",
            dateIso = "Just Now",
            coinsRedeemed = totalCredited,
            grossAmount = pack.priceText,
            netAmount = "+$totalCredited MP",
            payoutChannel = PayoutChannel.UPI,
            destinationMasked = "In-App Store Credit",
            status = TransactionStatus.COMPLETED,
            estimatedArrival = "Instant Credit"
        )

        _uiState.value = current.copy(
            availableCoins = newAvailable,
            purchaseSuccessMessage = "Successfully purchased ${pack.coinsAmount} MP Coins! (+${pack.bonusCoins} Bonus)",
            transactions = listOf(depositRecord) + current.transactions
        )
    }

    fun clearPurchaseMessage() {
        _uiState.value = _uiState.value.copy(purchaseSuccessMessage = null)
    }

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
