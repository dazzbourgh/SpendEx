package dao

import command.BankDetails
import kotlinx.serialization.json.Json

expect class JsonAccountDaoImpl() : AccountDao {
    override suspend fun save(bankDetails: BankDetails)

    override suspend fun list(): Iterable<BankDetails>
}

internal object JsonConfig {
    val json = Json { prettyPrint = true }
}
