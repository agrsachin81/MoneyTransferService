package home.test.api.example.moneytransfer.service.impl.mem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import home.test.api.example.moneytransfer.entities.Account;
import home.test.api.example.moneytransfer.entities.AccountEntry;
import home.test.api.example.moneytransfer.entities.AccountIdUtils;
import home.test.api.example.moneytransfer.service.internal.AccountServiceInternal;
import home.test.api.example.moneytransfer.spi.enums.AccountStatus;
import home.test.api.example.moneytransfer.spi.enums.StatusResponse;
import home.test.api.example.moneytransfer.spi.enums.TransactionStatus;
import home.test.api.example.moneytransfer.spi.exceptions.AccountException;
import home.test.api.example.moneytransfer.spi.exceptions.AccountNotFoundException;
import home.test.api.example.moneytransfer.spi.interfaces.AccountRekuest;
import home.test.api.example.moneytransfer.spi.interfaces.AccountResult;
import home.test.api.example.moneytransfer.util.AccountBuilder;

public class InMemoryAccountService implements AccountServiceInternal {

	private static final String ACCOUNT_NOT_FOUND = "Account not found ";
	private static final String NO_ACCOUNT_FOUND = ACCOUNT_NOT_FOUND;
	private static final String SUCCESSFUL_CREATED = " successful created ";
	private static final String INVALID_BALANCE = "invalid balance";
	private static final String UNKNOWN_ERROR_COULD_NOT_CREDIT_ACCOUNT = "UnknownError could not credit Account ";
	private static final String NOT_ENOUGH_FUNDS = "Not enough funds ";
	private static final String ACCOUNT_IS_NO_LONGER_ACTIVE = "Account is no longer active ";
	private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, List<AccountEntry>> accountEntries = new ConcurrentHashMap<>();
	public final int numOfAttempts = 20;
	
	public static boolean isNegative(double d) {
		return Double.compare(d, 0.0) < 0;
	}

	@Override
	public AccountResult addAccount(AccountRekuest acc) {

		// TODO: create a validation chain by designing chain of responsibility
		// todo: implement checks, validation like name, mobile_number already exists
		// in case of external service might not be present etc.
		AccountBuilder builder = AccountBuilder.createAccountBuilder(acc);
		if (isNegative(acc.getBalance())) {
			return builder.createAccountResultWithRekuest(StatusResponse.ERROR, INVALID_BALANCE);
		}

		String nextId = AccountIdUtils.generateNext();
		Account accnt = builder.createNewAccount(nextId);

		Account oldAccnt = accounts.putIfAbsent(nextId, accnt);
		while (oldAccnt != null) {
			nextId = AccountIdUtils.generateNext();
			accnt = builder.createNewAccount(nextId);			
		}  
		
		accountEntries.putIfAbsent(nextId, new ArrayList<AccountEntry>());		
		return builder.createAccountResult(accnt, StatusResponse.SUCCESS, SUCCESSFUL_CREATED);
	}

	@Override
	public Collection<AccountResult> getAccounts() {
		return AccountBuilder.getAccountBuilder().createAccountResults(accounts, StatusResponse.SUCCESS);
	}

	@Override
	public AccountResult getAccount(String id) {
		AccountBuilder builder = AccountBuilder.getAccountBuilder();
		Account accnt = getAccountInstance(id);
		if (accnt != null) {
			return builder.createAccountResult(accnt, StatusResponse.SUCCESS, " FOUND ");
		}
		return builder.createAccountResult(id, StatusResponse.ERROR, NO_ACCOUNT_FOUND);
	}

	@Override
	public AccountResult editAccount(AccountRekuest accountRekuest) throws AccountException {
		AccountBuilder builder = AccountBuilder.createAccountBuilder(accountRekuest);
		
		Account accnt = getAccountInstance(accountRekuest.getAccountId());

		if (accnt == null) {
			return builder.createAccountResultWithRekuest(StatusResponse.ERROR, ACCOUNT_NOT_FOUND);
		}
		checkAccountStatus(accnt); //deleted account can not be edited
		throw new UnsupportedOperationException();
	}

	@Override
	public AccountResult deleteAccount(String id) {
		AccountBuilder builder = AccountBuilder.getAccountBuilder();
		Account accnt = getAccountInstance(id);

		if (accnt == null) {
			return builder.createAccountResult(id, StatusResponse.ERROR, ACCOUNT_NOT_FOUND);
		}
		boolean res;
		try {
			res = checkAccountStatus(accnt);
		} catch (AccountException e) {
			res = false;
		}

		if (!res) {
			builder.createAccountResult(id, StatusResponse.SUCCESS, "Account already deleted");
		}

		accnt.setAccountDeleted();
		return builder.createAccountResult(accnt, StatusResponse.SUCCESS, "Account deleted successfully ");
	}

	@Override
	public boolean accountExist(String id) {
		return accounts.containsKey(id);
	}

	public Account getAccountInstance(String accountId) {
		if (accounts.containsKey(accountId)) {
			return accounts.get(accountId);
		}
		return null;
	}

	private boolean checkAccountStatus(Account account) throws AccountException {
		if (account.getAccountStatus() == AccountStatus.DELETED) {
			throw new AccountException(ACCOUNT_IS_NO_LONGER_ACTIVE);
		}
		return true;
	}

	@Override
	public AccountEntry debitAccount(String accountId, double amount, String transactionReferenceId, String rekuestId,
			String cpAccountId) throws AccountException {

		boolean done = false;
		final Account debitedAccount = getAccountInstance(accountId);

		if (debitedAccount == null) {
			throw new AccountNotFoundException(accountId);
		}

		checkAccountStatus(debitedAccount);

		int attempts = 0;
		double currentBalance = debitedAccount.getBalance();
		double newbalance = currentBalance - amount;
		while (currentBalance >= amount && attempts < numOfAttempts) {
			done = debitedAccount.updateBalance(newbalance, currentBalance);

			if (done)
				break;

			currentBalance = debitedAccount.getBalance();
			newbalance = currentBalance - amount;
			attempts++;
		}
		if (done)
			return addAccountEntry(accountId, amount, true, transactionReferenceId, rekuestId, TransactionStatus.DONE,
					cpAccountId, currentBalance, newbalance);
		else
			throw new AccountException(NOT_ENOUGH_FUNDS + accountId);
	}

	private AccountEntry addAccountEntry(String accId, double amount, boolean debit, String transactionReferenceId,
			String rekuestId, TransactionStatus transactionStatus, String cpAccountId,  double oldBalance, double newBalance) {
		AccountEntry entry = new AccountEntry(amount, debit, transactionReferenceId, rekuestId, transactionStatus,
				cpAccountId,  oldBalance, newBalance);
		accountEntries.get(accId).add(entry);
		return entry;
	}

	@Override
	public AccountEntry creditAccount(String accountId, double amount, String transactionReferenceId, String rekuestId,
			String cpAccountId) throws AccountException {
		final Account creditedAccount = getAccountInstance(accountId);

		if (creditedAccount == null) {
			throw new AccountNotFoundException(accountId);
		}
		
		checkAccountStatus(creditedAccount);
		Double balance = creditedAccount.getBalance();
		double newbalance = balance + amount;
		boolean done = false;
		int attempts = 0;
		while (attempts < numOfAttempts) {
			done = creditedAccount.updateBalance(newbalance, balance);

			if (done)
				break;

			balance = creditedAccount.getBalance();
			newbalance = balance + amount;
			attempts++;
		}
		if (done)
			return addAccountEntry(accountId, amount, false, transactionReferenceId, rekuestId, TransactionStatus.DONE,
					cpAccountId, balance, newbalance);
		else
			throw new AccountException(UNKNOWN_ERROR_COULD_NOT_CREDIT_ACCOUNT + accountId);
	}	

	@Override
	public Collection<AccountEntry> getAccountEntries(String accntId) {
		return accountEntries.get(accntId);
	}
}
