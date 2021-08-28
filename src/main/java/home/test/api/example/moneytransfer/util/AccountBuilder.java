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

	private AccountRequest rekuest;

	private static ThreadLocal<AccountBuilder> accountBuilder = new ThreadLocal<AccountBuilder>() {
		public AccountBuilder initialValue() {
			return new AccountBuilder();
		}
	};

	private AccountBuilder() {

	}

	public static AccountBuilder createAccountBuilder(AccountRequest rekuest) {
		AccountBuilder builder = getAccountBuilder();
		builder.withAccountRekuest(rekuest);
		return builder;
	}

	public static AccountBuilder getAccountBuilder() {
		AccountBuilder builder = accountBuilder.get().reset();
		return builder;
	}

	public AccountBuilder withAccountRekuest(AccountRequest rekuest) {
		this.rekuest = rekuest;
		return this;
	}

	private AccountBuilder reset() {
		this.rekuest = null;
		return this;
	}

	public Account createNewAccount(String accId) {
		return new Account(rekuest.getName(), rekuest.getMobileNumber(), accId, rekuest.getBalance());
	}

	public AccountResult createAccountResultWithRekuest(StatusResponse response, String message) {
		return new AccountResultImpl(null, rekuest.getBalance(), AccountStatus.UNKNOWN,
				rekuest.getName(), rekuest.getMobileNumber(), response, message);
		
	}
	
	public AccountResult createAccountResult(Account account, StatusResponse response, String message) {
		return new AccountResultImpl(account.getAccountId(), account.getBalance(), account.getAccountStatus(),
				account.getName(), account.getMobileNumber(), response, message);
	}

	public Collection<AccountResult> createAccountResults(ConcurrentMap<String,Account> accounts, StatusResponse status) {
		
		//the performance can be improved
		return accounts.values().stream().map(( account) -> {
			return createAccountResult(account, status,"");
		}).collect(Collectors.toList());
	}

	public AccountResult createAccountResult(String id, StatusResponse status, String message) {
		return new AccountResultImpl(id, status, message);
	}
}
