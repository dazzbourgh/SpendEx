package interpreter

import account.AccountLinkingServiceImpl
import account.AccountServiceImpl
import browser.BrowserLauncher
import config.Constants
import config.EnvironmentConfig
import config.ProductionEnvironmentConfig
import config.SandboxEnvironmentConfig
import dao.JsonConfigDao
import dao.JsonInstitutionConnectionRepository
import dao.JsonTransactionDaoImpl
import plaid.OAuthRedirectServer
import plaid.PlaidConfigurationServiceImpl
import plaid.PlaidFinancialProviderModule
import provider.FinancialProviderRegistryImpl
import provider.ProviderIds
import provider.SharedProviderDependencies
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
        val configDao = JsonConfigDao(environmentConfig)
        val institutionConnectionRepository = JsonInstitutionConnectionRepository()
        val transactionDao = JsonTransactionDaoImpl()
        val browserLauncher = createBrowserLauncher()
        val sharedProviderDependencies =
            SharedProviderDependencies(
                environmentConfig = environmentConfig,
                browserLauncher = browserLauncher,
                oauthRedirectServerFactory = ::createOAuthRedirectServer,
            )
        val plaidProviderRuntime = PlaidFinancialProviderModule(configDao).createRuntime(sharedProviderDependencies)
        val providerRegistry = FinancialProviderRegistryImpl(listOf(plaidProviderRuntime))
        val accountLinkingService =
            AccountLinkingServiceImpl(
                defaultProviderId = ProviderIds.PLAID,
                providerRegistry = providerRegistry,
                institutionConnectionRepository = institutionConnectionRepository,
            )
        val accountService = AccountServiceImpl(institutionConnectionRepository)
        val accountCommandInterpreter =
            AccountCommandInterpreterImpl(
                accountLinkingService,
                accountService,
            )
        val transactionService =
            TransactionServiceImpl(
                institutionConnectionRepository,
                transactionDao,
                providerRegistry,
            )
        val transactionCommandInterpreter = TransactionCommandInterpreterImpl(transactionService)
        return InterpreterImpl(
            accountCommandInterpreter,
            PlaidCommandInterpreterImpl(
                PlaidConfigurationServiceImpl(configDao),
            ),
            transactionCommandInterpreter,
        )
    }
}
