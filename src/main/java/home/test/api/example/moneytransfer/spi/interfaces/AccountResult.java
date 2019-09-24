package home.test.api.example.moneytransfer.spi.interfaces;

import home.test.api.example.moneytransfer.spi.enums.AccountStatus;
import home.test.api.example.moneytransfer.spi.enums.StatusResponse;

public interface AccountResult{
	AccountStatus getAccountStatus();
	String getAccountId();	
	Double getBalance();
	String getName();
	String getMobileNumber();
	
	
	/**
	 * overall status of the rekuest/operation
	 * @return
	 */
	StatusResponse getStatus();
	
	String getMessage();
}
