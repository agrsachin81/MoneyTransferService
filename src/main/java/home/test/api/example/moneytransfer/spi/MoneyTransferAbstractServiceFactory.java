
package home.test.api.example.moneytransfer.spi;

import com.google.gson.Gson;

/**
 * Main Entry point of the MoneyTransferService
 * @author sachin
 *
 */
public interface MoneyTransferAbstractServiceFactory {

	/**
	 * Use to return the instance of AccountService
	 * @return
	 */
	AccountService getAccountService();
	
	/**
	 * returns the instance of Transaction service used to manage transaction
	 * @return
	 */
	TransactionService getTransactionService();
	
	/**
	 * create a GSON serializer/deserializer, that have many adapters for Optional, and Enums etc.
	 *  It is recommended to use this for a smoother experience
	 * @return Gson serializer
	 */
	Gson getJson() ;
}
