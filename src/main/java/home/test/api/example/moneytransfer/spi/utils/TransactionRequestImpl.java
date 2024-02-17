package home.test.api.example.moneytransfer.spi.utils;

import java.util.Optional;

import home.test.api.example.moneytransfer.spi.enums.TransactionType;
import home.test.api.example.moneytransfer.spi.interfaces.TransactionRequest;

public final class TransactionRequestImpl implements TransactionRequest {

	private final Optional<String> cpAccountId;
	private final String cashReferenceId;
	private final String cashLocation;
	private final double amount;
	private final String transactionRequestId;
	private final TransactionType transactionType;

	public TransactionRequestImpl(double amount, String transactionRequestId) {
		this(null, amount, transactionRequestId, TransactionType.DEBIT_CASH, null, null);
	}

	public TransactionRequestImpl(double amount, String transactionRequestId, TransactionType transactionType) {
		this(null, amount, transactionRequestId, transactionType, null, null);
	}

	public TransactionRequestImpl(String cpAccountId, double amount, String transactionRequestId) {
		this(cpAccountId, amount, transactionRequestId, TransactionType.DEBIT_ACCOUNT, null, null);
	}

	public TransactionRequestImpl(String cpAccountId, double amount, String transactionRequestId,
			TransactionType transactionType) {
		this(cpAccountId, amount, transactionRequestId, transactionType, null, null);
	}

	public TransactionRequestImpl(String cpAccountId, double amount, String transactionRequestId,
			TransactionType transactionType, String cashReferenceId, String cashLocationId) {
		super();
		this.cpAccountId = Optional.ofNullable(cpAccountId);
		this.amount = amount;
		this.transactionRequestId = transactionRequestId;

		this.transactionType = transactionType;

		this.cashReferenceId = cashReferenceId;
		this.cashLocation = cashLocationId;
	}

	@Override
	public Optional<String> getCpAccountId() {
		if(cpAccountId==null || !cpAccountId.isPresent()) {
			return Optional.empty();
		}
		return cpAccountId;
	}

	@Override
	public double getAmount() {
		return amount;
	}

	@Override
	public String getTransactionRequestId() {
		return transactionRequestId;
	}

	@Override
	public TransactionType getTransactionType() {
		if (transactionType != null)
			return transactionType;
		else
			return DEFAULT_TRANSACTION_TYPE;
	}

	@Override
	public Optional<String> getCashReferenceId() {
		return Optional.ofNullable(cashReferenceId);
	}

	@Override
	public Optional<String> getCashLocation() {
		return Optional.ofNullable(cashLocation);
	}

	@Override
	public boolean isCash() {
		return TransactionType.CASH_TRANSACTIONS.contains(transactionType);
	}
}
