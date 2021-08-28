package home.test.api.example.moneytransfer.spi.utils;

import home.test.api.example.moneytransfer.spi.interfaces.AccountRequest;

public final class AccountRequestImpl implements AccountRequest{

	private final String accountId;
	private final String name;
	private final String mobileNumber;
	private final double balance;
	public AccountRequestImpl(String name, String mobileNumber) {
		this(null, name, mobileNumber, 0.0);
	}
	
	public AccountRequestImpl(String name, String mobileNumber, double balance) {
		this(null, name, mobileNumber, balance);
	}
	
	public AccountRequestImpl(String accountId, String name, String mobileNumber, double balance) {
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
