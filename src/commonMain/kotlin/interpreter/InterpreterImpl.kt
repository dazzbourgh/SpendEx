package interpreter

class InterpreterImpl(
    override val accountCommandInterpreter: AccountCommandInterpreter,
    override val plaidCommandInterpreter: PlaidCommandInterpreter,
    override val transactionCommandInterpreter: TransactionCommandInterpreter,
) : Interpreter
