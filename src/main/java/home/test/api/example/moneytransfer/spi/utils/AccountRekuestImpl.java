package home.test.api.example.moneytransfer.spi.utils;

import home.test.api.example.moneytransfer.spi.interfaces.AccountRekuest;

public final class AccountRekuestImpl implements AccountRekuest{

	private final String accountId;
	private final String name;
	private final String mobileNumber;
	private final double balance;
	public AccountRekuestImpl(String name, String mobileNumber) {
		this(null, name, mobileNumber, 0.0);
	}
	
	public AccountRekuestImpl(String name, String mobileNumber, double balance) {
		this(null, name, mobileNumber, balance);
	}
	
	public AccountRekuestImpl(String accountId, String name, String mobileNumber, double balance) {
		this.accountId = accountId;
		this.mobileNumber= mobileNumber;
		this.name = name;
		this.balance = balance;
	}

	@Override
	public String getMobileNumber() {
		return  mobileNumber;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getAccountId() {
		return accountId;
	}

	@Override
	public double getBalance() {
		return balance;
	}
}
