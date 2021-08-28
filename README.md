# MoneyTransferService
This is the required dependency for MoneyTransferSpark project which is pushed separately. https://github.com/agrsachin81/MoneyTransferSpark

To run integration tests "mvn clean verify -P integration-test"

To run unit tests "mvn clean test -P dev"

Exposes service factory, which can be used to fetch 2 Services. 
	1 AccountService aims to manage Account CRUD operations.
	2 TransactionService, which allow to transfer money from one account to other account.
