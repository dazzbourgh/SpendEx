package interpreter

interface Interpreter {
    val accountCommandInterpreter: AccountCommandInterpreter
    val plaidCommandInterpreter: PlaidCommandInterpreter
    val transactionCommandInterpreter: TransactionCommandInterpreter
}
