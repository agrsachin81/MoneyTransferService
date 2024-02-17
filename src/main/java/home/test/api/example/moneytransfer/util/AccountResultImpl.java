package home.test.api.example.moneytransfer.util;

import home.test.api.example.moneytransfer.spi.enums.AccountStatus;
import home.test.api.example.moneytransfer.spi.enums.StatusResponse;
import home.test.api.example.moneytransfer.spi.interfaces.AccountResult;

public class AccountResultImpl implements AccountResult {

	private final StatusResponse status;
	private final double balance;
	private final AccountStatus accountStatus;
	private final String accountId;
	private final String mobileNumber;
	private final String name;
	private final String message;

	private final String requestId;
	
	public AccountResultImpl(String accountId, StatusResponse response, String message, String requestId) {
		this(accountId, 0.0, AccountStatus.UNKNOWN, null, null, response, message, requestId);
	}
	
	public AccountResultImpl(String accountId, double balance, AccountStatus status, String name, String mobileNumber,
			StatusResponse response, String message, String requestId) {
		this.status = response;
		this.balance = balance;
		this.accountStatus = status;
		this.name = name;
		this.mobileNumber = mobileNumber;
		this.accountId = accountId;
		this.message = message;
		this.requestId= requestId;
	}

	@Override
	public StatusResponse getStatus() {
		return status;
	}

	@Override
	public AccountStatus getAccountStatus() {
		return accountStatus;
	}

	@Override
	public String getAccountId() {
		return accountId;
	}

	@Override
	public Double getBalance() {
		return balance;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getMobileNumber() {
		return mobileNumber;
	}

	@Override
	public String getRequestId() {
		return requestId;
	}

	@Override
	public String getMessage() {
		return message;
	}

}
