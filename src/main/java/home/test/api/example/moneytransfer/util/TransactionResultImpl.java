
package home.test.api.example.moneytransfer.util;

import java.util.Optional;

import home.test.api.example.moneytransfer.spi.enums.StatusResponse;
import home.test.api.example.moneytransfer.spi.enums.TransactionStatus;
import home.test.api.example.moneytransfer.spi.interfaces.TransactionResult;

public class TransactionResultImpl implements TransactionResult {

	private final boolean debit;
	private final boolean cash;
	private final TransactionStatus transactionStatus;
	private final StatusResponse status;
	private final long timeStamp;
	private final Optional<String> cpAccountId;
	private final String transactionRekuestId;
	private final String transactionReferenceId;
	private final String accountId;
	private final double amount;
	private final Optional<String> cashReferenceId;
	private final String message;

	public TransactionResultImpl(TransactionStatus transactStatus, String accountId, StatusResponse status,  String message) {
		this("", transactStatus, "", accountId, 0.0, status, Optional.empty(), true, true, Optional.empty(),
				System.currentTimeMillis(), message);
	}

	public TransactionResultImpl(String rekId, TransactionStatus transactStatus, String transRefeId, String accountId,
			double balance, StatusResponse status, Optional<String> cpAccountId, boolean isDebit, boolean isCash,
			Optional<String> cashRefId, long time, String message) {
		this.debit = isDebit;
		this.cash = isCash;
		this.status = status;
		this.transactionStatus = transactStatus;
		this.timeStamp = time;
		this.cpAccountId = cpAccountId;
		this.transactionRekuestId = rekId;
		this.transactionReferenceId = transRefeId;
		this.accountId = accountId;
		this.amount = balance;
		this.cashReferenceId = cashRefId;
		this.message = message;
	}

	@Override
	public String getTransactionRekuestId() {
		return this.transactionRekuestId;
	}

	@Override
	public TransactionStatus getTransactionStatus() {
		return transactionStatus;
	}

	@Override
	public String getTransactionReferenceId() {
		return transactionReferenceId;
	}

	@Override
	public String getAccountId() {
		return this.accountId;
	}

	@Override
	public Optional<String> getCpAccountId() {
		return cpAccountId;
	}

	@Override
	public boolean isDebit() {
		return debit;
	}

	@Override
	public boolean isCash() {
		return cash;
	}

	@Override
	public Optional<String> getCashReferenceId() {
		return this.cashReferenceId;
	}

	@Override
	public long getTimeStamp() {
		return timeStamp;
	}

	@Override
	public StatusResponse getStatus() {
		return status;
	}

	@Override
	public double getAmount() {
		return amount;
	}

	@Override
	public String getMessage() {
		return message;
	}

}
