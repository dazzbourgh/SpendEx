package config

object SandboxEnvironmentConfig : EnvironmentConfig {
    override val configFileName: String = "config-sandbox.json"
    override val plaidBaseUrl: String = "https://sandbox.plaid.com"
}
