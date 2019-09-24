 
package home.test.api.example.moneytransfer.service.impl.mem;

import home.test.api.example.moneytransfer.service.internal.AccountServiceInternal;

public class InMemoryAccountServiceFactory implements AccountServiceFactory {
	public AccountServiceInternal createAccountServiceInternal(){
		return new InMemoryAccountService();
	}
}
