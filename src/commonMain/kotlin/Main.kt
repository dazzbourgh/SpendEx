import account.AccountCommandExecutor
import arrow.core.Either
import cli.CliParser
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val executor = AccountCommandExecutor()

    val parseResult = CliParser.parse(args)

    runBlocking {
        when (parseResult) {
            is Either.Right -> {
                val parsedCommand = parseResult.value
                when (val execResult = executor.execute(parsedCommand)) {
                    is Either.Right -> println(execResult.value)
                    is Either.Left -> {
                        printError("Error: ${execResult.value}")
                        exitProcess(1)
                    }
                }
            }
            is Either.Left -> {
                printError("Error: ${parseResult.value}")
                exitProcess(1)
            }
        }
    }
}

private fun printError(message: String) {
    println(message)
}