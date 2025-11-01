package cli

import arrow.core.Either
import arrow.core.left
import arrow.core.right

/**
 * Parser for CLI arguments following the format: "program feature command --flag1 value1 --flag2 value2"
 * Returns parsed data structures without executing any business logic.
 */
object CliParser {

    /**
     * Parses command line arguments and returns a ParsedCommand data structure.
     * @param args Array of command line arguments
     * @return Either with Left containing error message or Right containing ParsedCommand
     */
    fun parse(args: Array<String>): Either<String, ParsedCommand> {
        if (args.isEmpty()) {
            return "Usage: financial-advisor <feature> <command> [flags]\nAvailable features: account".left()
        }

        if (args.size < 2) {
            return "Usage: financial-advisor <feature> <command> [flags]".left()
        }

        val featureName = args[0]
        val commandName = args[1]
        val flags = parseFlags(args.drop(2))

        return when (featureName.lowercase()) {
            "account" -> parseAccountFeature(commandName, flags)
            else -> "Unknown feature: $featureName. Available features: account".left()
        }
    }

    private fun parseAccountFeature(commandName: String, flags: Map<String, String>): Either<String, ParsedCommand> {
        return when (commandName.lowercase()) {
            "add" -> {
                val bank = flags["bank"]
                    ?: return "Missing required flag: --bank".left()

                ParsedCommand(
                    feature = Feature.Account(AccountCommand.Add(bank)),
                    flags = flags
                ).right()
            }
            else -> "Unknown command: $commandName. Available commands: add".left()
        }
    }

    /**
     * Parses flags from arguments in the format: --flag1 value1 --flag2 value2
     */
    private fun parseFlags(args: List<String>): Map<String, String> {
        val flags = mutableMapOf<String, String>()
        var i = 0

        while (i < args.size) {
            val arg = args[i]

            if (arg.startsWith("--")) {
                val flagName = arg.removePrefix("--")

                if (i + 1 < args.size && !args[i + 1].startsWith("--")) {
                    flags[flagName] = args[i + 1]
                    i += 2
                } else {
                    // Flag without value, treat as boolean flag with empty value
                    flags[flagName] = ""
                    i += 1
                }
            } else {
                // Skip non-flag arguments
                i += 1
            }
        }

        return flags
    }
}
