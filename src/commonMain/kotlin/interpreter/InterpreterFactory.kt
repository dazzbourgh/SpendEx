package interpreter

import browser.BrowserLauncher
import config.Constants
import config.JsonConfigDao
import dao.JsonAccountDaoImpl
import dao.JsonTokenDaoImpl
import plaid.HttpClientFactory
import plaid.OAuthRedirectServer
import plaid.PlaidServiceImpl

expect fun createBrowserLauncher(): BrowserLauncher

expect fun createOAuthRedirectServer(): OAuthRedirectServer

object InterpreterFactory {
    fun get(env: String): Interpreter =
        when (env) {
            Constants.App.ENVIRONMENT_PROD -> {
                val accountDao = JsonAccountDaoImpl()
                val tokenDao = JsonTokenDaoImpl()
                val configDao = JsonConfigDao()
                val httpClient = HttpClientFactory.create()
                val plaidService = PlaidServiceImpl(httpClient, configDao)
                val browserLauncher = createBrowserLauncher()
                InterpreterImpl(
                    AccountCommandInterpreterImpl(
                        accountDao,
                        tokenDao,
                        plaidService,
                        configDao,
                        browserLauncher,
                        ::createOAuthRedirectServer,
                    ),
                )
            }
            else -> throw IllegalArgumentException("${Constants.Commands.ErrorMessages.UNKNOWN_ENVIRONMENT}: $env")
        }
}
