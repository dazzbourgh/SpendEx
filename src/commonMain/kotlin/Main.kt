import account.AccountCommandExecutor
import cli.CliParser
import cli.Result
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val executor = AccountCommandExecutor()

    val parseResult = CliParser.parse(args)

    runBlocking {
        when (parseResult) {
            is Result.Success -> {
                val parsedCommand = parseResult.value
                when (val execResult = executor.execute(parsedCommand)) {
                    is Result.Success -> println(execResult.value)
                    is Result.Failure -> {
                        printError("Error: ${execResult.message}")
                        exitProcess(1)
                    }
                }
            }
            is Result.Failure -> {
                printError("Error: ${parseResult.message}")
                exitProcess(1)
            }
        }
    }
}

private fun printError(message: String) {
    println(message)
}