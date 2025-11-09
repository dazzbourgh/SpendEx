package account

import arrow.core.Either
import arrow.core.right
import command.BankDetails
import dao.TokenDao

class AccountServiceImpl(
    private val tokenDao: TokenDao,
) : AccountService {
    override suspend fun listAccounts(): Either<String, Iterable<BankDetails>> =
        tokenDao.list().map { token ->
            BankDetails(
                name = token.bankName,
                dateAdded = token.createdAt,
            )
        }.right()
}
