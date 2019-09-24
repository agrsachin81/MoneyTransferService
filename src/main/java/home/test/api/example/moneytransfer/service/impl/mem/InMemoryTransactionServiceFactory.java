
package home.test.api.example.moneytransfer.service.impl.mem;

import home.test.api.example.moneytransfer.service.internal.AccountServiceInternal;
import home.test.api.example.moneytransfer.spi.TransactionService;

public class InMemoryTransactionServiceFactory implements TransactionServiceFactory {
	@Override
	public TransactionService createTransactionService(AccountServiceInternal service) {
		return new InMemoryTransactionService(service);
	}
}
