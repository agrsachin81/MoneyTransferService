package home.test.api.example.moneytransfer.spi.interfaces;

import java.util.Optional;

import home.test.api.example.moneytransfer.spi.enums.StatusResponse;
import home.test.api.example.moneytransfer.spi.enums.TransactionStatus;

public interface TransactionResult {
		
	String getTransactionRekuestId();
	/**
	 *  current transaction status 
	 * @return
	 */
	TransactionStatus getTransactionStatus();	
	
	String getTransactionReferenceId();
	String getAccountId();
	Optional<String> getCpAccountId();
	
	boolean isDebit();
	boolean isCash();
	
	Optional<String> getCashReferenceId();
	
	/**
	 * Epoch timestamp
	 * @return
	 */
	long getTimeStamp();
	
	/**
	 * overall status of the rekuest/operation
	 * @return
	 */
	StatusResponse getStatus();
	
	double getAmount();
	
	String getMessage();
}
