package com.muscleplay.app.feature.coins.model

enum class CoinsHubTab(val label: String) {
    BUY("Buy"),
    REFERRAL("Earn"),
    CASHOUT("Cashout"),
    HISTORY("History")
}

enum class PayoutChannel(val label: String) {
    UPI("UPI (Fastest)"),
    BANK_TRANSFER("Bank Transfer"),
    PAYPAL("PayPal"),
    STRIPE("Stripe Connect")
}

enum class TransactionStatus(val label: String) {
    PROCESSING("Processing"),
    PENDING("Pending"),
    COMPLETED("Completed"),
    UNDER_REVIEW("Under Review"),
    FAILED("Failed")
}

data class TransactionRecord(
    val id: String,
    val dateIso: String,
    val coinsRedeemed: Int,
    val grossAmount: String,
    val netAmount: String,
    val payoutChannel: PayoutChannel,
    val destinationMasked: String,
    val status: TransactionStatus,
    val estimatedArrival: String
)

data class CoinPackItem(
    val id: String,
    val coinsAmount: Int,
    val bonusCoins: Int,
    val priceText: String,
    val tagText: String? = null,
    val isPopular: Boolean = false
)

data class ReferralItem(
    val id: String,
    val friendName: String,
    val dateIso: String,
    val rewardCoins: Int,
    val isCompleted: Boolean,
    val statusText: String
)

data class UpiFormData(
    val fullName: String = "",
    val upiId: String = "",
    val confirmUpiId: String = ""
)

data class BankFormData(
    val accountHolderName: String = "",
    val accountNumber: String = "",
    val confirmAccountNumber: String = "",
    val ifscCode: String = ""
)

data class PaypalFormData(
    val fullName: String = "",
    val paypalEmail: String = "",
    val confirmEmail: String = ""
)

data class StripeFormData(
    val isConnected: Boolean = false,
    val accountId: String = ""
)

val sampleCoinPacks = listOf(
    CoinPackItem(
        id = "pack_starter",
        coinsAmount = 250,
        bonusCoins = 0,
        priceText = "₹29 ($0.49)",
        tagText = null,
        isPopular = false
    ),
    CoinPackItem(
        id = "pack_player",
        coinsAmount = 600,
        bonusCoins = 100,
        priceText = "₹59 ($0.99)",
        tagText = "+100 Bonus",
        isPopular = false
    ),
    CoinPackItem(
        id = "pack_pro",
        coinsAmount = 1500,
        bonusCoins = 300,
        priceText = "₹129 ($1.99)",
        tagText = "BEST VALUE 🔥",
        isPopular = true
    ),
    CoinPackItem(
        id = "pack_champion",
        coinsAmount = 3500,
        bonusCoins = 800,
        priceText = "₹299 ($3.99)",
        tagText = "+800 Bonus",
        isPopular = false
    )
)

val sampleReferrals = listOf(
    ReferralItem(
        id = "ref_1",
        friendName = "Rahul Sharma",
        dateIso = "Today, 11:30 AM",
        rewardCoins = 150,
        isCompleted = true,
        statusText = "Completed 1st Workout — +150 MP"
    ),
    ReferralItem(
        id = "ref_2",
        friendName = "Priya Kapoor",
        dateIso = "Yesterday, 04:15 PM",
        rewardCoins = 150,
        isCompleted = false,
        statusText = "Signed Up — Pending 1st Workout"
    ),
    ReferralItem(
        id = "ref_3",
        friendName = "Ankit Mehta",
        dateIso = "14 Jan 2025",
        rewardCoins = 150,
        isCompleted = true,
        statusText = "Completed 1st Workout — +150 MP"
    ),
    ReferralItem(
        id = "ref_4",
        friendName = "Sneha Verma",
        dateIso = "10 Jan 2025",
        rewardCoins = 150,
        isCompleted = true,
        statusText = "Completed 1st Workout — +150 MP"
    )
)

val sampleTransactionHistory = listOf(
    TransactionRecord(
        id = "TXN-MP-98421",
        dateIso = "Today, 02:15 PM",
        coinsRedeemed = 1000,
        grossAmount = "₹10.00",
        netAmount = "₹10.00",
        payoutChannel = PayoutChannel.UPI,
        destinationMasked = "alex@okicici",
        status = TransactionStatus.PROCESSING,
        estimatedArrival = "Tomorrow by 6:00 PM"
    ),
    TransactionRecord(
        id = "TXN-MP-84190",
        dateIso = "15 Jan 2025",
        coinsRedeemed = 2000,
        grossAmount = "₹20.00",
        netAmount = "₹20.00",
        payoutChannel = PayoutChannel.BANK_TRANSFER,
        destinationMasked = "A/C •••• 4892 (HDFC)",
        status = TransactionStatus.COMPLETED,
        estimatedArrival = "16 Jan 2025"
    ),
    TransactionRecord(
        id = "TXN-MP-71204",
        dateIso = "02 Jan 2025",
        coinsRedeemed = 1000,
        grossAmount = "$1.00",
        netAmount = "$1.00",
        payoutChannel = PayoutChannel.PAYPAL,
        destinationMasked = "alex.f***@gmail.com",
        status = TransactionStatus.COMPLETED,
        estimatedArrival = "03 Jan 2025"
    )
)
