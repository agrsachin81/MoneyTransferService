package home.test.api.example.moneytransfer.util;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import home.test.api.example.moneytransfer.entities.Account;
import home.test.api.example.moneytransfer.spi.enums.AccountStatus;
import home.test.api.example.moneytransfer.spi.enums.StatusResponse;
import home.test.api.example.moneytransfer.spi.interfaces.AccountRequest;
import home.test.api.example.moneytransfer.spi.interfaces.AccountResult;

public final class AccountBuilder {

	private AccountRequest request;

	private static final ThreadLocal<AccountBuilder> ACCOUNT_BUILDER_THREAD_LOCAL = ThreadLocal.withInitial(AccountBuilder::new);

	private AccountBuilder() {

	}

	public static AccountBuilder createAccountBuilder(AccountRequest request) {
		AccountBuilder builder = getAccountBuilderThreadLocal();
		return builder.withAccountRequest(request);
	}

	public static AccountBuilder getAccountBuilderThreadLocal() {
		return ACCOUNT_BUILDER_THREAD_LOCAL.get().reset();
	}

	public AccountBuilder withAccountRequest(AccountRequest rekuest) {
		this.request = rekuest;
		return this;
	}

	private AccountBuilder reset() {
		this.request = null;
		return this;
	}

	public Account createNewAccount(String accId) {
		return new Account(request.getName(), request.getMobileNumber(), accId, request.getBalance());
	}

	public AccountResult createAccountResultWithRequest(StatusResponse response, String message) {
		return new AccountResultImpl(null, request.getBalance(), AccountStatus.UNKNOWN,
				request.getName(), request.getMobileNumber(), response, message, request.getRequestId());
	}
	
	public AccountResult createAccountOpResult(Account account, StatusResponse response, String message, String requestId) {
		return new AccountResultImpl(account.getAccountId(), account.getBalance(), account.getAccountStatus(),
				account.getName(), account.getMobileNumber(), response, message, requestId);
	}

	public Collection<AccountResult> createAccountResults(ConcurrentMap<String,Account> accounts, StatusResponse status, String requestId) {
		//the performance can be improved
		return accounts.values().stream().map(( account) -> createAccountOpResult(account, status,"", requestId)).collect(Collectors.toList());
	}

	public AccountResult createAccountOpResult(String id, StatusResponse status, String message, String requestId) {
		return new AccountResultImpl(id, status, message, requestId);
	}
}
