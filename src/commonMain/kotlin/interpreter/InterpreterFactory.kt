package interpreter

import account.AccountServiceImpl
import browser.BrowserLauncher
import config.Constants
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
    fun get(env: String): Interpreter =
        when (env) {
            Constants.App.ENVIRONMENT_SANDBOX -> {
                val environmentConfig = SandboxEnvironmentConfig
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
                        accountService,
                        tokenDao,
                        transactionDao,
                        plaidService,
                    )
                val transactionCommandInterpreter =
                    TransactionCommandInterpreterImpl(
                        transactionService,
                        configDao,
                    )
                InterpreterImpl(
                    accountCommandInterpreter,
                    PlaidCommandInterpreterImpl(
                        plaidService,
                    ),
                    transactionCommandInterpreter,
                )
            }
            else -> throw IllegalArgumentException("${Constants.Commands.ErrorMessages.UNKNOWN_ENVIRONMENT}: $env")
        }
}
