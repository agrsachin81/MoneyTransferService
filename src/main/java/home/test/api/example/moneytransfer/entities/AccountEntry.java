
package home.test.api.example.moneytransfer.entities;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import home.test.api.example.moneytransfer.enums.AccountEntryType;
import home.test.api.example.moneytransfer.spi.enums.TransactionStatus;

public class AccountEntry {

	private final String entryId;
	private final AccountEntryType entryType;
	private final Long timestamp;
	private final String rekuestId;

	static AtomicInteger entryIdSek = new AtomicInteger(0);
	private final String transactionReferenceId;
	private final TransactionStatus transactionStatus;
	private final Optional<String> cpAccountId;
	private final boolean debit;
	private final double oldBalance;
	private final double newBalance;

	public AccountEntry(double amount, boolean debit, String transactionReferenceId, String rekuestId,
			TransactionStatus transStatus, String cpAccountId, double oldBalance, double newBalance) {
		this.amount = amount;
		this.entryType = debit ? AccountEntryType.DEBIT : AccountEntryType.CREDIT;
		this.entryId = entryIdSek.incrementAndGet() + "_TY";
		this.transactionReferenceId = transactionReferenceId;
		this.timestamp = System.currentTimeMillis();
		this.rekuestId = rekuestId;
		this.transactionStatus = transStatus;
		this.cpAccountId = cpAccountId == null ? Optional.empty() : Optional.of(cpAccountId);
		this.debit = debit;
		this.oldBalance = oldBalance;
		this.newBalance = newBalance;
	}

	public double getOldBalance() {
		return oldBalance;
	}

	public double getNewBalance() {
		return newBalance;
	}

	public boolean isDebit() {
		return debit;
	}

	public String getTransactionReferenceId() {
		return transactionReferenceId;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public String getRekuestId() {
		return rekuestId;
	}

	public TransactionStatus getTransactionStatus() {
		return transactionStatus;
	}

	public Optional<String> getCpAccountId() {
		return cpAccountId;
	}

	public String getEntryId() {
		return entryId;
	}

	private double amount;

	public double getAmount() {
		return amount;
	}

	public AccountEntryType getEntryType() {
		return entryType;
	}
}
