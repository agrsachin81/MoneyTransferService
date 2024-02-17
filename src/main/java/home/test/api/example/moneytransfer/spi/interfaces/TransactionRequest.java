package home.test.api.example.moneytransfer.spi.interfaces;

import java.util.Optional;

import home.test.api.example.moneytransfer.spi.enums.TransactionType;

public interface TransactionRequest {

	/**
	 *
	 * @return. the Optional CounterPartyAccount Id
	 */
	public Optional<String> getCpAccountId();	
	public double getAmount();	
	
	/**
	 * Must be non null
	 * @return
	 */
	public String getTransactionRequestId();
	
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
	
	TransactionType DEFAULT_TRANSACTION_TYPE = TransactionType.DEBIT_ACCOUNT;
}
