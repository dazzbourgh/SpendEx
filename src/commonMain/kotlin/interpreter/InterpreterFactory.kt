package interpreter

import account.AccountServiceImpl
import browser.BrowserLauncher
import config.Constants
import config.EnvironmentConfig
import config.ProductionEnvironmentConfig
import config.SandboxEnvironmentConfig
import dao.JsonConfigDao
import dao.JsonTokenDaoImpl
import dao.JsonTransactionDaoImpl
import plaid.HttpClientFactory
import plaid.OAuthRedirectServer
import plaid.PlaidServiceImpl
import transaction.TransactionServiceImpl

expect fun createBrowserLauncher(): BrowserLauncher

expect fun createOAuthRedirectServer(): OAuthRedirectServer

object InterpreterFactory {
    fun get(env: String): Interpreter {
        val environmentConfig = getEnvironmentConfig(env)
        return buildInterpreter(environmentConfig)
    }

    private fun getEnvironmentConfig(env: String): EnvironmentConfig =
        when (env) {
            Constants.App.ENVIRONMENT_SANDBOX -> SandboxEnvironmentConfig
            Constants.App.ENVIRONMENT_PROD -> ProductionEnvironmentConfig
            else -> throw IllegalArgumentException("${Constants.Commands.ErrorMessages.UNKNOWN_ENVIRONMENT}: $env")
        }

    private fun buildInterpreter(environmentConfig: EnvironmentConfig): Interpreter {
        val tokenDao = JsonTokenDaoImpl()
        val configDao = JsonConfigDao(environmentConfig)
        val httpClient = HttpClientFactory.create()
        val browserLauncher = createBrowserLauncher()
        val plaidService =
            PlaidServiceImpl(
                httpClient,
                configDao,
                browserLauncher,
                ::createOAuthRedirectServer,
                environmentConfig,
            )
        val accountService = AccountServiceImpl(tokenDao)
        val transactionDao = JsonTransactionDaoImpl()
        val accountCommandInterpreter =
            AccountCommandInterpreterImpl(
                tokenDao,
                plaidService,
                configDao,
                accountService,
            )
        val transactionService =
            TransactionServiceImpl(
                tokenDao,
                transactionDao,
                plaidService,
            )
        val transactionCommandInterpreter =
            TransactionCommandInterpreterImpl(
                transactionService,
                configDao,
            )
        return InterpreterImpl(
            accountCommandInterpreter,
            PlaidCommandInterpreterImpl(
                plaidService,
            ),
            transactionCommandInterpreter,
        )
    }
}
