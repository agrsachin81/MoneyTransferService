
package home.test.api.example.moneytransfer.mock;

import home.test.api.example.moneytransfer.entities.AccountEntry;
import home.test.api.example.moneytransfer.service.impl.mem.InMemoryAccountService;
import home.test.api.example.moneytransfer.service.internal.AccountServiceInternal;
import home.test.api.example.moneytransfer.spi.exceptions.AccountException;

//we can safely use the default implementation as it is InMemory when we move to external or database based 
//we need to change this impl
public class MockAccountServiceInternal extends InMemoryAccountService implements AccountServiceInternal {

	// implement only extra methods to simulate the behavior of AccountService and
	// test the behavior of TransactionService

	@Override
	public AccountEntry debitAccount(String accountId, double amount, String transactionReferenceId, String requestId,
			String cpAccountId) throws AccountException {
		if (acceptDebit) {
			return super.debitAccount(accountId, amount, transactionReferenceId, requestId, cpAccountId);
		}
		throw new AccountException("Unable to debit Account");
	}

	@Override
	public AccountEntry creditAccount(String accountId, double amount, String transactionReferenceId, String requestId,
			String cpAccountId) throws AccountException {
		if (acceptCredit) {

			return super.creditAccount(accountId, amount, transactionReferenceId, requestId, cpAccountId);
		}
		if (enableCreditAfterRejectingOne) {
			acceptCredit = true;
		}
		throw new AccountException("Unable to credit Account");
	}

	public void acceptAllAccountcreditDebit() {
		acceptCredit = true;
		acceptDebit = true;
		enableCreditAfterRejectingOne = false;
	}

	public void rejectDebitOnly() {
		acceptDebit = false;
		acceptCredit = true;
		enableCreditAfterRejectingOne = false;
	}

	public void rejectCreditOnly() {
		acceptDebit = true;
		acceptCredit = false;
		enableCreditAfterRejectingOne = false;
	}

	public void rejectAllCreditDebit() {
		acceptCredit = false;
		acceptDebit = false;
		enableCreditAfterRejectingOne = false;
	}

	public void rejectOnlyOneCredit() {
		acceptCredit = false;
		acceptDebit = true;
		enableCreditAfterRejectingOne = true;
	}

	private boolean acceptCredit = false;
	private boolean acceptDebit = false;
	private boolean enableCreditAfterRejectingOne = false;
}
