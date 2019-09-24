package home.test.api.example.moneytransfer.spi;

import java.util.Collection;

import home.test.api.example.moneytransfer.spi.interfaces.TransactionRekuest;
import home.test.api.example.moneytransfer.spi.interfaces.TransactionResult;

/**
 * Use to transfer funds in account, get Transaction status
 * @author sachin
 *
 */
public interface TransactionService {
	
	/**
	 * transfer funds from one account to another account or cash transaction logging
	 * @param transaction
	 * @param originatingAccntId to which debit or credit shall be done
	 * @return TransactionResult
	 */
	TransactionResult transfer(TransactionRekuest transaction, String originatingAccntId);	
	
	/**
	 * to fetch last n Transaction for the given account Id
	 * @param accntId, the account id for which transactions are fetched
	 * @param lastn, number of last transactions
	 * @return
	 */
	Collection<TransactionResult> getTransactions(String accntId, int lastn);
	
	/**
	 * to fetch the status of the transaction
	 * @param accountId
	 * @param transactionid transaction reference id returned by {@link #transfer(TransactionRekuest, String)} 
	 * @return TransactionResult
	 */
	TransactionResult getTransaction(String accountId, String transactionid);
	
	
	
}
