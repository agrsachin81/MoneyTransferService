package home.test.api.example.moneytransfer.spi;

import java.util.Collection;

import home.test.api.example.moneytransfer.spi.exceptions.AccountException;
import home.test.api.example.moneytransfer.spi.interfaces.AccountRequest;
import home.test.api.example.moneytransfer.spi.interfaces.AccountResult;

/**
 * to help create and manage accounts, i.e. CRUD operations for transactions use TransactionService
 * @author sachin
 *
 * Type AccountService, created on 24-Sep-2019 at 4:51:33 pm
 *
 */
public interface AccountService {

	/**
	 * To add Account 
	 * @param acc the account details 
	 * @return AccountResult, account id is generated by the service itself
	 */
	AccountResult addAccount(AccountRequest acc);

	/**
	 * returns all known accounts
	 * @return collection of AccounResult
	 */
	Collection<AccountResult> getAccounts();

	/**
	 * to fetch current status of the account
	 * @param accountId
	 * @return current status of account
	 */
	AccountResult getAccount(String accountId);

	/**
	 * to edit the account, accountid should be filled correctly
	 * @param account
	 * @return current status of the account
	 * @throws AccountException Account not found, already deleted if edit is not allowed
	 */
	AccountResult editAccount(AccountRequest account) throws AccountException;

	/**
	 * To delete the accounts, when account is deleted only status is changed, transaction history remains intact 
	 * @param accountId
	 * @return
	 */
	AccountResult deleteAccount(String accountId) ;

	/**
	 * if account exists for the given accountId
	 * @param accountId
	 * @return true if exists, false otherwise
	 */
	boolean accountExist(String accountId);

	
}
