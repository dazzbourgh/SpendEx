package account

enum class Bank {
    AMEX,
    BOFA,
    CHASE,
    GOLDMANSACHS;

    companion object {
        fun fromString(value: String): Bank? =
            entries.find { it.name.equals(value, ignoreCase = true) }
    }
}
