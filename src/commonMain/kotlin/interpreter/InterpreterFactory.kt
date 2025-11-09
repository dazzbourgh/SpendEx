package interpreter

import browser.BrowserLauncher
import config.Constants
import config.SandboxEnvironmentConfig
import dao.JsonConfigDao
import dao.JsonTokenDaoImpl
import plaid.HttpClientFactory
import plaid.OAuthRedirectServer
import plaid.PlaidServiceImpl

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
                val accountCommandInterpreter =
                    ValidatingAccountCommandInterpreter(
                        AccountCommandInterpreterImpl(
                            tokenDao,
                            plaidService,
                            configDao,
                        ),
                        configDao,
                    )
                InterpreterImpl(
                    accountCommandInterpreter,
                    PlaidCommandInterpreterImpl(
                        plaidService,
                    ),
                )
            }
            else -> throw IllegalArgumentException("${Constants.Commands.ErrorMessages.UNKNOWN_ENVIRONMENT}: $env")
        }
}
