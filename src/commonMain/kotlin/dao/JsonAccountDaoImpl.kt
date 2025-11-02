package dao

import arrow.core.Either
import command.BankDetails
import kotlinx.serialization.json.Json

expect class JsonAccountDaoImpl() : AccountDao {
    override suspend fun save(bankDetails: BankDetails): Either<String, Unit>

    override suspend fun list(): Either<String, Iterable<BankDetails>>
}

internal object JsonConfig {
    val json = Json { prettyPrint = true }
}
