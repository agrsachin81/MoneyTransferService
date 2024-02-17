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
import home.test.api.example.moneytransfer.spi.interfaces.AccountRequest;
import home.test.api.example.moneytransfer.spi.interfaces.AccountResult;
import home.test.api.example.moneytransfer.spi.interfaces.AccountUpdateRequest;
import home.test.api.example.moneytransfer.util.AccountBuilder;

public class InMemoryAccountService implements AccountServiceInternal {

	private static final String ACCOUNT_NOT_FOUND = "Account not found ";
	private static final String NO_ACCOUNT_FOUND = ACCOUNT_NOT_FOUND;
	private static final String SUCCESSFUL_CREATED = " successful created ";
	private static final String CREATION_FAILED = " already exists ";
	private static final String INVALID_BALANCE = "invalid balance";
	private static final String UNKNOWN_ERROR_COULD_NOT_CREDIT_ACCOUNT = "UnknownError could not credit Account ";
	private static final String NOT_ENOUGH_FUNDS = "Not enough funds ";
	private static final String ACCOUNT_IS_NO_LONGER_ACTIVE = "Account is no longer active ";
	public static final String ATTEMPT = "Attempt";
	public static final String ATTEMPT_DELETE = "ATTEMPT DELETE";
	private final ConcurrentMap<String, Account> idAccountsMap = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, Account> mobileAccountsMap = new ConcurrentHashMap<>();

	private static final ThreadLocal<Account.AccountUpdateResult> ACCOUNT_UPDATE_RESULT_THREAD_LOCAL = ThreadLocal.withInitial(Account.AccountUpdateResult::new);
	private final ConcurrentMap<String, List<AccountEntry>> accountEntries = new ConcurrentHashMap<>();
	public final int numOfAttempts = 20;

	static String washMobileNumber(String number){
		// return a 10 digit numeric only;
		// throw if does not confirm to after removing non digits like "-" "," ":" etc.
		// if 12 digits remove first two digits
		return number;
	}

	public static boolean isNegative(double d) {
		return Double.compare(d, 0.0) < 0;
	}

	@Override
	public AccountResult addAccount(final AccountRequest accountRequest) {

		// TODO: create a validation chain by designing chain of responsibility
		// todo: implement checks, validation like name, mobile_number already exists
		// in case of external service might not be present etc.
		AccountBuilder builder = AccountBuilder.createAccountBuilder(accountRequest);
		if (isNegative(accountRequest.getBalance())) {
			return builder.createAccountResultWithRequest(StatusResponse.ERROR, INVALID_BALANCE);
		}

		// fix no need to use putif Absent as nextId will always be unique
		// so the real check is we are having similar AccountOpeningRequest at the same time
		final String mobile = washMobileNumber(accountRequest.getMobileNumber());
	   	Account account = mobileAccountsMap.computeIfAbsent(mobile, mob-> {
		   String nextId = AccountIdUtils.generateNext();
		   // nextId is always unique for any thread
		   return builder.createNewAccount(nextId);
		});

	    if(idAccountsMap.containsKey(account.getAccountId())) {
			return builder.createAccountOpResult(account, StatusResponse.ERROR,CREATION_FAILED, accountRequest.getRequestId() );
		}
		idAccountsMap.put(account.getAccountId(), account);
		accountEntries.putIfAbsent(account.getAccountId(), new ArrayList<>());
		return builder.createAccountOpResult(account, StatusResponse.SUCCESS, SUCCESSFUL_CREATED, accountRequest.getRequestId());
	}

	@Override
	public Collection<AccountResult> getIdAccountsMap() {
		return AccountBuilder.getAccountBuilderThreadLocal().createAccountResults(idAccountsMap, StatusResponse.SUCCESS, "ListOfAccounts ");
	}

	@Override
	public AccountResult getAccount(String id) {
		AccountBuilder builder = AccountBuilder.getAccountBuilderThreadLocal();
		Account account = getAccountInstance(id);
		if (account != null) {
			return builder.createAccountOpResult(account, StatusResponse.SUCCESS, " FOUND ", "getAccount "+id);
		}
		return builder.createAccountOpResult(id, StatusResponse.ERROR, NO_ACCOUNT_FOUND, "getAccount "+id);
	}

	@Override
	public AccountResult editAccount(AccountUpdateRequest accountRequest) throws AccountException {
		AccountBuilder builder = AccountBuilder.createAccountBuilder(accountRequest);
		
		Account account = getAccountInstance(accountRequest.getAccountId());

		if (account == null) {
			return builder.createAccountResultWithRequest(StatusResponse.ERROR, ACCOUNT_NOT_FOUND);
		}
		checkAccountStatus(account); //deleted account can not be edited
		throw new UnsupportedOperationException();
	}

	@Override
	public AccountResult deleteAccount(final String id) {
		AccountBuilder builder = AccountBuilder.getAccountBuilderThreadLocal();
		final Account account = getAccountInstance(id);
		if (account == null) {
			return builder.createAccountOpResult(id, StatusResponse.ERROR, ACCOUNT_NOT_FOUND, "Delete "+id);
		}
		boolean res;
		try {
			res = checkAccountStatus(account);
		} catch (AccountException e) {
			res = false;
		}

		if (!res) {
			return builder.createAccountOpResult(id, StatusResponse.SUCCESS, "Account already deleted", "Delete "+id);
		}
		//in real life the accounts will be archived
		//a separate service will be able to fetch deleted account information from archived storage
		Account.AccountUpdateResult outResult = ACCOUNT_UPDATE_RESULT_THREAD_LOCAL.get();
		outResult.fillFailure(ATTEMPT_DELETE);
		if (account.setAccountDeleted(outResult))
			return builder.createAccountOpResult(account, StatusResponse.SUCCESS, "Account deleted successfully ", "Delete "+id);
		return builder.createAccountOpResult(account, StatusResponse.ERROR, "Account not deleted "+outResult.getMessage() +" "+id, "Delete "+id);
	}

	@Override
	public boolean accountExist(String id) {
		return idAccountsMap.containsKey(id);
	}

	public Account getAccountInstance(String accountId) {
		if (idAccountsMap.containsKey(accountId)) {
			return idAccountsMap.get(accountId);
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
	public AccountEntry debitAccount(final String accountId,final double amount,final String transactionReferenceId, String requestId,
			String cpAccountId) throws AccountException {
		
		final Account debitedAccount = getAccountInstance(accountId);
		if (debitedAccount == null) {
			throw new AccountNotFoundException(accountId);
		}

		checkAccountStatus(debitedAccount);
		final Account.AccountUpdateResult outResult = ACCOUNT_UPDATE_RESULT_THREAD_LOCAL.get();
		outResult.fillFailure(ATTEMPT);
		boolean done = debitedAccount.debit(amount, outResult);
		if (done)
			return addAccountEntry(accountId, amount, true, transactionReferenceId, requestId, TransactionStatus.DONE,
					cpAccountId, outResult.getOldBalance().doubleValue(), outResult.getNewBalance().doubleValue());
		else
			throw new AccountException( outResult.getMessage() +"-"+ accountId);
	}

	private AccountEntry addAccountEntry(String accId, double amount, boolean debit, String transactionReferenceId,
			String requestId, TransactionStatus transactionStatus, String cpAccountId,  double oldBalance, double newBalance) {
		AccountEntry entry = new AccountEntry(amount, debit, transactionReferenceId, requestId, transactionStatus,
				cpAccountId,  oldBalance, newBalance);
		accountEntries.get(accId).add(entry);
		return entry;
	}

	@Override
	public AccountEntry creditAccount(String accountId, double amount, String transactionReferenceId, String requestId,
			String cpAccountId) throws AccountException {
		final Account creditedAccount = getAccountInstance(accountId);

		if (creditedAccount == null) {
			throw new AccountNotFoundException(accountId);
		}
		
		checkAccountStatus(creditedAccount);
		Account.AccountUpdateResult outResult = ACCOUNT_UPDATE_RESULT_THREAD_LOCAL.get();
		outResult.fillFailure(ATTEMPT);
		if (creditedAccount.credit(amount, outResult))
			return addAccountEntry(accountId, amount, false, transactionReferenceId, requestId, TransactionStatus.DONE,
					cpAccountId, outResult.getOldBalance().doubleValue(), outResult.getNewBalance().doubleValue());
		else
			throw new AccountException(outResult.getMessage() + "- Unable to credit Account -"+ accountId);
	}	

	@Override
	public Collection<AccountEntry> getAccountEntries(String accountId) {
		return accountEntries.get(accountId);
	}

	@Override
	public Collection<AccountResult> getAccounts() {
		return AccountBuilder.getAccountBuilderThreadLocal().createAccountResults(idAccountsMap, StatusResponse.SUCCESS, " list accounts ");
	}
}
