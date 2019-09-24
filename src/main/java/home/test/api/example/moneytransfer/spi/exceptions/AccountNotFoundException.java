
package home.test.api.example.moneytransfer.spi.exceptions;

public class AccountNotFoundException extends AccountException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AccountNotFoundException(String accountId) {
		super(accountId);
	}
	
}
