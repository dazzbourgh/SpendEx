package account

import arrow.core.Either
import arrow.core.right
import dao.InstitutionConnectionRepository
import model.BankDetails

/**
 * Default account query service backed by stored institution connections.
 *
 * @property institutionConnectionRepository Local storage for linked institution connections
 */
class AccountServiceImpl(
    private val institutionConnectionRepository: InstitutionConnectionRepository,
) : AccountService {
    override suspend fun listAccounts(): Either<String, Iterable<BankDetails>> =
        institutionConnectionRepository.list().map { connection ->
            BankDetails(
                name = connection.institutionName,
                dateAdded = connection.createdAt,
            )
        }.right()
}
