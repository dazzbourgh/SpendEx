package config

object SandboxEnvironmentConfig : EnvironmentConfig {
    override val configFileName: String = "app-data-sandbox.json"
    override val plaidBaseUrl: String = "https://sandbox.plaid.com"
}
