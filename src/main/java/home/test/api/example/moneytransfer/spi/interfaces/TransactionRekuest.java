package home.test.api.example.moneytransfer.spi.interfaces;

import java.util.Optional;

import home.test.api.example.moneytransfer.spi.enums.TransactionType;

public interface TransactionRekuest {

	public Optional<String> getCpAccountId();	
	public double getAmount();	
	
	/**
	 * Must be non null
	 * @return
	 */
	public String getTransactionRekuestId();
	
	/**
	 * if null then 
	 *   in case of cpAccountId is present DEBIT_ACCOUNT is assumed
	 *   in case of cpAccountId is not present then DEBIT_CASH is assumed
	 * @return
	 */
	TransactionType getTransactionType();
	
	
	boolean isCash();
	
	Optional<String> getCashReferenceId();
	
	Optional<String> getCashLocation();
}
