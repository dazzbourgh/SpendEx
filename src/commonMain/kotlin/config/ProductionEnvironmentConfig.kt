package config

object ProductionEnvironmentConfig : EnvironmentConfig {
    override val configFileName: String = "config-prod.json"
    override val plaidBaseUrl: String = "https://production.plaid.com"
}
