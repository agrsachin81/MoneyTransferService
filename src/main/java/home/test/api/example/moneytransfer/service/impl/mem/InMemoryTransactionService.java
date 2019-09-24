
package home.test.api.example.moneytransfer.service.impl.mem;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import home.test.api.example.moneytransfer.entities.AccountEntry;
import home.test.api.example.moneytransfer.entities.Transaction;
import home.test.api.example.moneytransfer.service.internal.AccountServiceInternal;
import home.test.api.example.moneytransfer.spi.TransactionService;
import home.test.api.example.moneytransfer.spi.enums.StatusResponse;
import home.test.api.example.moneytransfer.spi.enums.TransactionStatus;
import home.test.api.example.moneytransfer.spi.exceptions.AccountException;
import home.test.api.example.moneytransfer.spi.interfaces.TransactionRekuest;
import home.test.api.example.moneytransfer.spi.interfaces.TransactionResult;
import home.test.api.example.moneytransfer.util.TransactionBuilder;

public class InMemoryTransactionService implements TransactionService {

	private final AccountServiceInternal service;

	// supposedly in memory
	// TODO: manage a very large using only a persistence mechanism, and caching
	// using LRU cache
	Map<String, Transaction> transactions = new HashMap<>();

	public InMemoryTransactionService(AccountServiceInternal service) {
		this.service = service;
	}

	@Override
	public TransactionResult transfer(TransactionRekuest transaction, String originatingAccntId) {
		TransactionBuilder builder = TransactionBuilder.createBuilder(transaction);

		//TODO: add logging
		//System.out.println("Originating Accnt "+originatingAccntId +" type " + transaction.getTransactionType());
		
		if(originatingAccntId==null) {
			return builder.createTransactionResult(StatusResponse.ERROR, TransactionStatus.INVALID_INPUT,
					"Accountid must have a valid value", originatingAccntId, true, false);
		}
		
		if(!transaction.isCash() && !transaction.getCpAccountId().isPresent()) {
			return builder.createTransactionResult(StatusResponse.ERROR, TransactionStatus.INVALID_INPUT,
					"non cash transactions must have cpAccount ", originatingAccntId, true, false);
		}
		
		boolean updateCpAccount = false;
		String debitedAccountId = null;
		String creditedAccountId = null;
		boolean isDebit = false;
		boolean isCash = false;
		switch (transaction.getTransactionType()) {
		case DEBIT_ACCOUNT:
			debitedAccountId = originatingAccntId;
			creditedAccountId = transaction.getCpAccountId().get();
			updateCpAccount = true;
			isDebit = true;
			break;
		case DEBIT_CASH:
			debitedAccountId = originatingAccntId;
			updateCpAccount = false;
			creditedAccountId = null;
			isDebit = true;
			isCash = true;
			break;
		case CREDIT_ACCOUNT:
			creditedAccountId = originatingAccntId;
			debitedAccountId = transaction.getCpAccountId().get();
			updateCpAccount = true;
			break;
		case CREDIT_CASH:
			creditedAccountId = originatingAccntId;
			updateCpAccount = false;
			debitedAccountId = null;
			isCash = true;
			break;
		default:
			break;
		}
		AccountEntry debitDone = null;

		if (debitedAccountId != null) {
			try {
				debitDone = this.service.debitAccount(debitedAccountId, transaction.getAmount(),
						builder.getTransactionReferenceId(), transaction.getTransactionRekuestId(), creditedAccountId);
			} catch (AccountException e) {
				transactions.put(builder.getTransactionReferenceId(), builder.createTransaction(debitedAccountId,
						creditedAccountId, isCash, TransactionStatus.DEBIT_FAILED));
				return builder.createTransactionResult(StatusResponse.ERROR, TransactionStatus.DEBIT_FAILED,
						e.getMessage(), originatingAccntId, true, isCash);
			}
		}

		if (creditedAccountId != null) {
			try {
				this.service.creditAccount(creditedAccountId, transaction.getAmount(),
						builder.getTransactionReferenceId(), transaction.getTransactionRekuestId(), debitedAccountId);
			} catch (AccountException e) {

				if (updateCpAccount) {
					try {
						AccountEntry revertAccountEntry = this.service.creditAccount(debitedAccountId,
								transaction.getAmount(), builder.getTransactionReferenceId(),
								debitDone.getEntryId() + "_REV", creditedAccountId);

						transactions.put(builder.getTransactionReferenceId(),
								builder.createTransaction(debitedAccountId, creditedAccountId, false,
										TransactionStatus.CREDIT_FAILED_DEBIT_REVERTED));
						return builder.createTransactionResult(StatusResponse.ERROR,
								TransactionStatus.CREDIT_FAILED_DEBIT_REVERTED,
								"Error occurred while crediting account, debit operation successfully reverted.",
								originatingAccntId, false, false);
						// Some error occurred while crediting counterparty account, revert Successfull
					} catch (AccountException ex) {
						// FATAL
						// need to add transaction id and so that client can manually manage the account
						// revert was unsuccessfull, we can even implement a retry mechanism to revert
						// a retry mechanism can be asynchronous
						// or add a service which rechecks the status of the transaction or retries to
						// revert
						transactions.put(builder.getTransactionReferenceId(),
								builder.createTransaction(debitedAccountId, creditedAccountId, false,
										TransactionStatus.CREDIT_FAILED_DEBIT_NOT_REVERTED));
						return builder.createTransactionResult(StatusResponse.ERROR,
								TransactionStatus.CREDIT_FAILED_DEBIT_NOT_REVERTED,
								"Error occurred while crediting account, please use Debited AccountEntry Id["
										+ debitDone.getEntryId()
										+ "] to revert the transaction manually by CustomerCare",
								originatingAccntId, false, false);
					}
				} else {
					transactions.put(builder.getTransactionReferenceId(), builder.createTransaction(debitedAccountId,
							creditedAccountId, true, TransactionStatus.CREDIT_FAILED));
					return builder.createTransactionResult(StatusResponse.ERROR, TransactionStatus.CREDIT_FAILED,
							e.getMessage(), originatingAccntId, false, true);
				}
				// need to dump the information whether the account can not be debited back with
				// TransactionId
			}
		}

		transactions.put(builder.getTransactionReferenceId(),
				builder.createTransaction(debitedAccountId, creditedAccountId, isCash, TransactionStatus.DONE));
		return builder.createTransactionResult(StatusResponse.SUCCESS, TransactionStatus.DONE,
				" Transaction Successfull.", originatingAccntId, isDebit, isCash);
	}

	@Override
	public Collection<TransactionResult> getTransactions(String accntId, int lastn) {
		TransactionBuilder builder = TransactionBuilder.createBuilder();

		if (!service.accountExist(accntId)) {
			return Collections.singleton(
					builder.createAbortedTransactionResult(StatusResponse.ERROR, accntId, " AccountId not found"));
		}

		Collection<AccountEntry> entries = service.getAccountEntries(accntId);
		return builder.createTransactionResult(StatusResponse.SUCCESS, entries, accntId);
	}

	@Override
	public TransactionResult getTransaction(String accountId, String transactionid) {
		TransactionBuilder builder = TransactionBuilder.createBuilder();
		if (transactions.containsKey(transactionid) && service.accountExist(accountId)) {
			return builder.createTransactionResult(accountId, StatusResponse.SUCCESS, transactions.get(transactionid));
		}
		return builder.createAbortedTransactionResult(StatusResponse.ERROR, accountId, "The details do not match ");
	}
	
	//TODO: add basic tests to complete test end 2 test for transaction service
}
