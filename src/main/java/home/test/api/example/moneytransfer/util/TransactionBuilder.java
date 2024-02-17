package home.test.api.example.moneytransfer.util;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import home.test.api.example.moneytransfer.entities.AccountEntry;
import home.test.api.example.moneytransfer.entities.Transaction;
import home.test.api.example.moneytransfer.spi.enums.StatusResponse;
import home.test.api.example.moneytransfer.spi.enums.TransactionStatus;
import home.test.api.example.moneytransfer.spi.interfaces.TransactionRequest;
import home.test.api.example.moneytransfer.spi.interfaces.TransactionResult;

public final class TransactionBuilder {

	private static final String MESSAGE_EMPTY = "";
	private TransactionRequest request;
	private String transactionReferenceId;
	private long timestamp;
	private static final AtomicInteger idCounter = new AtomicInteger();

	private static final ThreadLocal<TransactionBuilder> transactBuilder = ThreadLocal.withInitial(TransactionBuilder::new);

	private TransactionBuilder() {
		reset();
	}

	public String getTransactionReferenceId() {
		return transactionReferenceId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public static TransactionBuilder createBuilder() {
		return transactBuilder.get().reset();
	}

	public TransactionBuilder withTransactionRekuest(TransactionRequest request) {
		this.request = request;
		return this;
	}

	public static TransactionBuilder createBuilder(TransactionRequest rekuest) {
		TransactionBuilder builder = createBuilder();
		builder.withTransactionRekuest(rekuest);
		return builder;
	}	

	public TransactionResult createTransactionResult(StatusResponse statusResponse, TransactionStatus transactionStatus,
			String message, String accountId, boolean isDebit, boolean isCash) {
		return new TransactionResultImpl(this.request.getTransactionRequestId(), transactionStatus,
				transactionReferenceId, accountId, this.request.getAmount(), statusResponse, request.getCpAccountId(),
				isDebit, isCash, request.getCashReferenceId(), timestamp, message);
	}

	public Transaction createTransaction(String debitedAccountId, String creditedAccountId, boolean isCashTransaction,
			TransactionStatus transactionStatus) {
		return new Transaction(transactionReferenceId, debitedAccountId, creditedAccountId, this.request.getAmount(),
				isCashTransaction, request.getCashReferenceId(), request.getTransactionRequestId(), transactionStatus);
	}

	public TransactionResult createAbortedTransactionResult(StatusResponse status, String accntId,
			String message) {
		TransactionResult res = new TransactionResultImpl(TransactionStatus.ABORTED, accntId, status, message);

		return (res);
	}

	public Collection<TransactionResult> createTransactionResult(StatusResponse status,
			Collection<AccountEntry> entries, String accountId) {

		return entries.stream().map((account) -> {
			return createTransactionResult(accountId, account, status);
		}).collect(Collectors.toList());
	}

	private TransactionResult createTransactionResult(String accountId, AccountEntry account, StatusResponse status) {
		return new TransactionResultImpl(account.getRekuestId(), account.getTransactionStatus(),
				account.getTransactionReferenceId(), accountId, account.getAmount(), status, account.getCpAccountId(),
				account.isDebit(), false, Optional.empty(), account.getTimestamp(), MESSAGE_EMPTY);
	}

	private TransactionBuilder reset() {
		this.request = null;
		this.timestamp = System.currentTimeMillis();
		this.transactionReferenceId = idCounter.incrementAndGet() + "_TXN";
		return this;
	}

	public TransactionResult createTransactionResult(String accountId, StatusResponse status, Transaction transaction) {
		boolean isDebitAccnt = transaction.getDebitedAccountId().equals(accountId);
		Optional<String> cpAccountId = isDebitAccnt ? transaction.getCreditAccountId():
		transaction.getCreditAccountId() ;
		
		return new TransactionResultImpl(transaction.getRequestId(), transaction.getTransactionStatus(),
				transaction.getTransactionReferenceId(),accountId , transaction.getAmount(), status, 
				cpAccountId, isDebitAccnt, false, transaction.getKioskId(), transaction.getTimestamp(), MESSAGE_EMPTY);
	}
}
