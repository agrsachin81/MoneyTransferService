package home.test.api.example.moneytransfer.util;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import home.test.api.example.moneytransfer.entities.AccountEntry;
import home.test.api.example.moneytransfer.entities.Transaction;
import home.test.api.example.moneytransfer.spi.enums.StatusResponse;
import home.test.api.example.moneytransfer.spi.enums.TransactionStatus;
import home.test.api.example.moneytransfer.spi.interfaces.TransactionRekuest;
import home.test.api.example.moneytransfer.spi.interfaces.TransactionResult;

public final class TransactionBuilder {

	private static final String MESSAGE_EMPTY = "";
	private TransactionRekuest rekuest;
	private String transactionReferenceId;
	private long timestamp;
	private static final AtomicInteger idCounter = new AtomicInteger();

	private static ThreadLocal<TransactionBuilder> transactBuilder = new ThreadLocal<TransactionBuilder>() {
		public TransactionBuilder initialValue() {
			return new TransactionBuilder();
		}
	};

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

	public TransactionBuilder withTransactionRekuest(TransactionRekuest rekuest) {
		this.rekuest = rekuest;
		return this;
	}

	public static TransactionBuilder createBuilder(TransactionRekuest rekuest) {
		TransactionBuilder builder = createBuilder();
		builder.withTransactionRekuest(rekuest);
		return builder;
	}	

	public TransactionResult createTransactionResult(StatusResponse statusResponse, TransactionStatus transactionStatus,
			String message, String accountId, boolean isDebit, boolean isCash) {
		return new TransactionResultImpl(this.rekuest.getTransactionRekuestId(), transactionStatus,
				transactionReferenceId, accountId, this.rekuest.getAmount(), statusResponse, rekuest.getCpAccountId(),
				isDebit, isCash, rekuest.getCashReferenceId(), timestamp, message);
	}

	public Transaction createTransaction(String debitedAccountId, String creditedAccountId, boolean isCashTransaction,
			TransactionStatus transactionStatus) {
		return new Transaction(transactionReferenceId, debitedAccountId, creditedAccountId, this.rekuest.getAmount(),
				isCashTransaction, rekuest.getCashReferenceId(), rekuest.getTransactionRekuestId(), transactionStatus);
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
		this.rekuest = null;
		this.timestamp = System.currentTimeMillis();
		this.transactionReferenceId = idCounter.incrementAndGet() + "_TXN";
		return this;
	}

	public TransactionResult createTransactionResult(String accountId, StatusResponse status, Transaction transaction) {
		boolean isDebitAccnt = transaction.getDebitedAccountId().equals(accountId);
		Optional<String> cpAccountId = isDebitAccnt ? transaction.getCreditAccountId():
		transaction.getCreditAccountId() ;
		
		return new TransactionResultImpl(transaction.getRekuestId(), transaction.getTransactionStatus(),
				transaction.getTransactionReferenceId(),accountId , transaction.getAmount(), status, 
				cpAccountId, isDebitAccnt, false, transaction.getKioskId(), transaction.getTimestamp(), MESSAGE_EMPTY);
	}
}
