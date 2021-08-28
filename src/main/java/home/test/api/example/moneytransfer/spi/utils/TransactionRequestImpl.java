package home.test.api.example.moneytransfer.spi.utils;

import java.util.Optional;

import home.test.api.example.moneytransfer.spi.enums.TransactionType;
import home.test.api.example.moneytransfer.spi.interfaces.TransactionRekuest;

public final class TransactionRekuestImpl implements TransactionRekuest {

	private final Optional<String> cpAccountId;

	public TransactionRekuestImpl(double amount, String transactionRekuestId) {
		this(null, amount, transactionRekuestId, TransactionType.DEBIT_CASH, null, null);
	}

	public TransactionRekuestImpl(double amount, String transactionRekuestId, TransactionType transactionType) {
		this(null, amount, transactionRekuestId, transactionType, null, null);
	}

	public TransactionRekuestImpl(String cpAccountId, double amount, String transactionRekuestId) {
		this(cpAccountId, amount, transactionRekuestId, TransactionType.DEBIT_ACCOUNT, null, null);
	}

	public TransactionRekuestImpl(String cpAccountId, double amount, String transactionRekuestId,
			TransactionType transactionType) {
		this(cpAccountId, amount, transactionRekuestId, transactionType, null, null);
	}

	public TransactionRekuestImpl(String cpAccountId, double amount, String transactionRekuestId,
			TransactionType transactionType, String cashReferenceId, String cashLocationId) {
		super();
		this.cpAccountId = Optional.ofNullable(cpAccountId);
		this.amount = amount;
		this.transactionRekuestId = transactionRekuestId;

		this.transactionType = transactionType;

		this.cashReferenceId = cashReferenceId;
		this.cashLocation = cashLocationId;
	}

	private final double amount;
	private final String transactionRekuestId;
	private final TransactionType transactionType;

	@Override
	public Optional<String> getCpAccountId() {
		if(cpAccountId==null) {
			return Optional.empty();
		}
		return cpAccountId;
	}

	@Override
	public double getAmount() {
		return amount;
	}

	@Override
	public String getTransactionRekuestId() {
		return transactionRekuestId;
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

	private final String cashReferenceId;
	private final String cashLocation;

	@Override
	public boolean isCash() {
		return TransactionType.CASH_TRANSACTIONS.contains(transactionType);
	}

}
