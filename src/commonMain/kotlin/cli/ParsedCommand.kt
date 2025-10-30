package cli

/**
 * Represents a parsed CLI invocation as pure data.
 * This is the output of the CLI parser before any business logic is applied.
 */
data class ParsedCommand(
    val feature: Feature,
    val flags: Map<String, String>
)

/**
 * Sealed class representing different features available in the application.
 * Each feature contains its specific command as a sealed class.
 */
sealed class Feature {
    data class Account(val command: AccountCommand) : Feature()
}

/**
 * Sealed class representing different commands for the Account feature.
 */
sealed class AccountCommand {
    data class Add(val bank: String) : AccountCommand()
}
