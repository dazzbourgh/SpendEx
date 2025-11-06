package interpreter

class InterpreterImpl(
    override val accountCommandInterpreter: AccountCommandInterpreter,
    override val plaidCommandInterpreter: PlaidCommandInterpreter,
) : Interpreter
