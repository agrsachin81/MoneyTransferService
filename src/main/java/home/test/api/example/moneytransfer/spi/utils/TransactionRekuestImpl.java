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
	
	public TransactionRekuestImpl(String cpAccountId, double amount, String transactionRekuestId, TransactionType transactionType) {
		this(cpAccountId, amount, transactionRekuestId, transactionType, null, null);
	}

	public TransactionRekuestImpl(String cpAccountId, double amount, String transactionRekuestId,
			TransactionType transactionType, String cashReferenceId, String cashLocationId) {
		super();
		this.cpAccountId = Optional.ofNullable(cpAccountId);
		this.amount = amount;
		this.transactionRekuestId = transactionRekuestId;

		if (transactionType != null)
			this.transactionType = transactionType;
		else
			this.transactionType = TransactionType.DEBIT_ACCOUNT;
		
		this.cashReferenceId = Optional.ofNullable(cashLocationId);
		this.cashLocation = Optional.ofNullable(cashLocationId);
	}

	private final double amount;
	private final String transactionRekuestId;
	private final TransactionType transactionType;

	@Override
	public Optional<String> getCpAccountId() {
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
		return transactionType;
	}

	@Override
	public Optional<String> getCashReferenceId() {
		return cashReferenceId;
	}

	@Override
	public Optional<String> getCashLocation() {
		return cashLocation;
	}

	private final Optional<String> cashReferenceId;
	private final Optional<String> cashLocation;

	@Override
	public boolean isCash() {
		return TransactionType.cashTransactions.contains(transactionType);
	}
	
}
