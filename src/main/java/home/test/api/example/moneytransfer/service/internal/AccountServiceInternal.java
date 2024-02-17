package home.test.api.example.moneytransfer.service.internal;

import java.util.Collection;

import home.test.api.example.moneytransfer.entities.Account;
import home.test.api.example.moneytransfer.entities.AccountEntry;
import home.test.api.example.moneytransfer.spi.AccountService;
import home.test.api.example.moneytransfer.spi.exceptions.AccountException;

public interface AccountServiceInternal extends AccountService {

	public AccountEntry debitAccount(String accountId, double amount, String transactionReferenceId, String requestId, String cpAccountId)
			throws AccountException;

	public AccountEntry creditAccount(String accountId, double amount, String transactionReferenceId, String requestId, String cpAccountId)
			throws AccountException;

	Account getAccountInstance(String accountId) ;
	
	public Collection<AccountEntry> getAccountEntries(String accountId);
}
