
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
import home.test.api.example.moneytransfer.spi.enums.TransactionType;
import home.test.api.example.moneytransfer.spi.exceptions.AccountException;
import home.test.api.example.moneytransfer.spi.interfaces.TransactionRekuest;
import home.test.api.example.moneytransfer.spi.interfaces.TransactionResult;
import home.test.api.example.moneytransfer.util.TransactionBuilder;

public class InMemoryTransactionService implements TransactionService {

	private static final String NON_CASH_TRANSACTIONS_MUST_HAVE_CP_ACCOUNT = "Non cash transactions must have cpAccount ";

	private static final String ACCOUNTID_MUST_HAVE_A_VALID_VALUE = "Accountid must have a valid value";

	private static final String ERROR_OCCURRED_WHILE_CREDITING_ACCOUNT_DEBIT_OPERATION_SUCCESSFULLY_REVERTED = "Error occurred while crediting account, debit operation successfully reverted.";

	private static final String DEBIT_REV = "_REV";

	private static final String TO_REVERT_THE_TRANSACTION_MANUALLY_BY_CUSTOMER_CARE = "] to revert the transaction manually by CustomerCare";

	private static final String ERROR_OCCURRED_WHILE_CREDITING_ACCOUNT_PLEASE_USE_DEBITED_ACCOUNT_ENTRY_ID = "Error occurred while crediting account, please use Debited AccountEntry Id[";

	private static final String TRANSACTION_SUCCESSFULL = " Transaction Successfull.";

	private final AccountServiceInternal service;

	// supposedly in memory
	// TODO: manage a very large using only a persistence mechanism, and caching
	// using LRU cache
	private final Map<String, Transaction> transactions = new HashMap<>();

	public InMemoryTransactionService(AccountServiceInternal service) {
		if(service == null) throw new NullPointerException();
		this.service = service;
	}

	@Override
	public TransactionResult transfer(TransactionRekuest transaction, String originatingAccntId) {
		TransactionBuilder transactionResultBuilder = TransactionBuilder.createBuilder(transaction);

		//TODO: add logging
		TransactionType transactionType = transaction.getTransactionType();
				
		if(originatingAccntId == null) return transactionResultBuilder.createTransactionResult(StatusResponse.ERROR, TransactionStatus.INVALID_INPUT,
					ACCOUNTID_MUST_HAVE_A_VALID_VALUE, originatingAccntId, true, false);
		
		
		if(!transaction.isCash() && !transaction.getCpAccountId().isPresent()) {
			return transactionResultBuilder.createTransactionResult(StatusResponse.ERROR, TransactionStatus.INVALID_INPUT,
					NON_CASH_TRANSACTIONS_MUST_HAVE_CP_ACCOUNT, originatingAccntId, true, false);
		}
		
		boolean updateCpAccount = false;
		String debitedAccountId = null;
		String creditedAccountId = null;
		boolean isDebit = false;
		boolean isCash = false;
		switch (transactionType) {
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
		AccountEntry debitEntry = null;

		if (debitedAccountId != null) {
			try {
				debitEntry = this.service.debitAccount(debitedAccountId, transaction.getAmount(),
						transactionResultBuilder.getTransactionReferenceId(), transaction.getTransactionRekuestId(), creditedAccountId);
			} catch (AccountException e) {
				transactions.put(transactionResultBuilder.getTransactionReferenceId(), transactionResultBuilder.createTransaction(debitedAccountId,
						creditedAccountId, isCash, TransactionStatus.DEBIT_FAILED));
				return transactionResultBuilder.createTransactionResult(StatusResponse.ERROR, TransactionStatus.DEBIT_FAILED,
						e.getMessage(), originatingAccntId, true, isCash);
			}
		}

		if (creditedAccountId != null) {
			try {
				this.service.creditAccount(creditedAccountId, transaction.getAmount(),
						transactionResultBuilder.getTransactionReferenceId(), transaction.getTransactionRekuestId(), debitedAccountId);
			} catch (AccountException e) {

				if (updateCpAccount) {
					try {
						return revertTransaction(transaction, originatingAccntId, transactionResultBuilder,
								debitedAccountId, creditedAccountId, debitEntry);
						// Some error occurred while crediting counter party account, revert success
					} catch (AccountException ex) {
						// FATAL
						// need to add transaction id and so that client can manually manage the account
						// revert was failure, we can even implement a retry mechanism to revert
						// a retry mechanism can be asynchronous
						// or add a service which rechecks the status of the transaction or retries to
						// revert
						return raiseFatalErrorUnableToRevert(originatingAccntId, transactionResultBuilder,
								debitedAccountId, creditedAccountId, debitEntry);
					}
				} else {
					transactions.put(transactionResultBuilder.getTransactionReferenceId(), transactionResultBuilder.createTransaction(debitedAccountId,
							creditedAccountId, true, TransactionStatus.CREDIT_FAILED));
					return transactionResultBuilder.createTransactionResult(StatusResponse.ERROR, TransactionStatus.CREDIT_FAILED,
							e.getMessage(), originatingAccntId, false, true);
				}
				// need to dump the information whether the account can not be debited back with
				// TransactionId
			}
		}

		transactions.put(transactionResultBuilder.getTransactionReferenceId(),
				transactionResultBuilder.createTransaction(debitedAccountId, creditedAccountId, isCash, TransactionStatus.DONE));
		return transactionResultBuilder.createTransactionResult(StatusResponse.SUCCESS, TransactionStatus.DONE,
				TRANSACTION_SUCCESSFULL, originatingAccntId, isDebit, isCash);
	}

	private TransactionResult raiseFatalErrorUnableToRevert(String originatingAccntId,
			TransactionBuilder transactionResultBuilder, String debitedAccountId, String creditedAccountId,
			AccountEntry debitEntry) {
		transactions.put(transactionResultBuilder.getTransactionReferenceId(),
				transactionResultBuilder.createTransaction(debitedAccountId, creditedAccountId, false,
						TransactionStatus.CREDIT_FAILED_DEBIT_NOT_REVERTED));
		return transactionResultBuilder.createTransactionResult(StatusResponse.ERROR,
				TransactionStatus.CREDIT_FAILED_DEBIT_NOT_REVERTED,
				ERROR_OCCURRED_WHILE_CREDITING_ACCOUNT_PLEASE_USE_DEBITED_ACCOUNT_ENTRY_ID
						+ debitEntry.getEntryId()
						+ TO_REVERT_THE_TRANSACTION_MANUALLY_BY_CUSTOMER_CARE,
				originatingAccntId, false, false);
	}

	private TransactionResult revertTransaction(TransactionRekuest transaction, String originatingAccntId,
			TransactionBuilder transactionResultBuilder, String debitedAccountId, String creditedAccountId,
			AccountEntry debitEntry) throws AccountException {
		AccountEntry revertAccountEntry = this.service.creditAccount(debitedAccountId,
				transaction.getAmount(), transactionResultBuilder.getTransactionReferenceId(),
				debitEntry.getEntryId() + DEBIT_REV, creditedAccountId);

		transactions.put(transactionResultBuilder.getTransactionReferenceId(),
				transactionResultBuilder.createTransaction(debitedAccountId, creditedAccountId, false,
						TransactionStatus.CREDIT_FAILED_DEBIT_REVERTED));
		return transactionResultBuilder.createTransactionResult(StatusResponse.ERROR,
				TransactionStatus.CREDIT_FAILED_DEBIT_REVERTED,
				ERROR_OCCURRED_WHILE_CREDITING_ACCOUNT_DEBIT_OPERATION_SUCCESSFULLY_REVERTED,
				originatingAccntId, false, false);
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
