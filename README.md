# MoneyTransferService
This is the rekuired dependency for MoneyTransferSpark project which is pushed separately. https://github.com/agrsachin81/MoneyTransferSpark

To run integration tests "mvn clean verify -P integration-test"

To run unit tests "mvn clean test -P dev"

Exposes service factory, which can be used to fetch 2 Services. One is AccountService aims to manage Account CRUD operations

Second is TransactionService, which allow to transfer money from one account to other account.
