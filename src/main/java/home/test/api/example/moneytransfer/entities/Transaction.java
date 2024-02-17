
package home.test.api.example.moneytransfer.entities;

import java.util.Optional;

import home.test.api.example.moneytransfer.spi.enums.TransactionStatus;

public class Transaction {
	private final boolean cash;

	private final Optional<String> creditedAccountId;
	private final Optional<String> debitedAccountId;
	private final double amount;
	private final Optional<String> kioskId;
	private final String transactionReferenceId;
	private final String requestId;
	private final TransactionStatus transactionStatus;
	
	private final Long timestamp;

	public Transaction(String transactionReferenceId, String debitedAccountId, String creditedAccountId, double amount,
			boolean isCashTransaction, Optional<String> kioskId, String requestId, TransactionStatus transactionStatus) {

		this.transactionReferenceId = transactionReferenceId;
		this.timestamp = System.currentTimeMillis();
		this.debitedAccountId = Optional.ofNullable(debitedAccountId);
		this.creditedAccountId = Optional.ofNullable(creditedAccountId);
		this.amount = amount;
		this.cash = isCashTransaction;
		this.kioskId = kioskId.isPresent() ? Optional.of(kioskId.get()) : Optional.empty();
		this.requestId = requestId;
		this.transactionStatus = transactionStatus;
	}

	public TransactionStatus getTransactionStatus() {
		return transactionStatus;
	}
	
	public double getAmount() {
		return amount;
	}

	public boolean isCashTransaction() {
		return this.cash;
	}

	public Optional<String> getCreditAccountId() {
		return creditedAccountId;
	}

	public Optional<String> getKioskId() {
		return kioskId;
	}

	public String getTransactionReferenceId() {
		return transactionReferenceId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public Optional<String> getDebitedAccountId() {
		return debitedAccountId;
	}

	public String getRequestId() {
		return requestId;
	}
}
