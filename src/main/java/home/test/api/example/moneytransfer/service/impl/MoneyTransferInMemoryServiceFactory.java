
package home.test.api.example.moneytransfer.service.impl;

import com.google.gson.Gson;

import home.test.api.example.moneytransfer.service.impl.mem.AccountServiceFactory;
import home.test.api.example.moneytransfer.service.impl.mem.InMemoryTransactionServiceFactory;
import home.test.api.example.moneytransfer.service.impl.mem.InMemoryAccountServiceFactory;
import home.test.api.example.moneytransfer.service.impl.mem.TransactionServiceFactory;
import home.test.api.example.moneytransfer.service.internal.AccountServiceInternal;
import home.test.api.example.moneytransfer.spi.AccountService;
import home.test.api.example.moneytransfer.spi.MoneyTransferAbstractServiceFactory;
import home.test.api.example.moneytransfer.spi.TransactionService;
import home.test.api.example.moneytransfer.util.GsonHelper;

public class MoneyTransferInMemoryServiceFactory implements MoneyTransferAbstractServiceFactory {

	private static final AccountServiceFactory accountServiceFactory;
	private static final TransactionServiceFactory transactionServiceFactory;

	static {
		accountServiceFactory = new InMemoryAccountServiceFactory();
		transactionServiceFactory = new InMemoryTransactionServiceFactory();
	}

	private final AccountServiceInternal accountService;
	private final TransactionService transactionService;

	private MoneyTransferInMemoryServiceFactory() {
		this(accountServiceFactory, transactionServiceFactory);
	}

	public MoneyTransferInMemoryServiceFactory(AccountServiceFactory accountServiceFactory,
			TransactionServiceFactory transactionServiceFactory) {
		this.accountService = accountServiceFactory.createAccountServiceInternal();
		this.transactionService = transactionServiceFactory.createTransactionService(accountService);
	}

	@Override
	public AccountService getAccountService() {
		return accountService;
	}

	@Override
	public TransactionService getTransactionService() {
		return transactionService;
	}

	public Gson getJson() {
		return GsonHelper.createJsonSerializerDeserializer();
	}

	public static MoneyTransferInMemoryServiceFactory getInstance() {
		return INSTANCE;
	}

	private static final MoneyTransferInMemoryServiceFactory INSTANCE = new MoneyTransferInMemoryServiceFactory();
}
