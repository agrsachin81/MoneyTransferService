
package home.test.api.example.moneytransfer.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import home.test.api.example.moneytransfer.mock.MockAccountServiceInternal;
import home.test.api.example.moneytransfer.service.impl.MoneyTransferInMemoryServiceFactory;
import home.test.api.example.moneytransfer.service.impl.mem.AccountServiceFactory;
import home.test.api.example.moneytransfer.service.impl.mem.TransactionServiceFactory;
import home.test.api.example.moneytransfer.service.internal.AccountServiceInternal;

public class MoneyTransferAbstractServiceFactoryTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	MoneyTransferAbstractServiceFactory factory;

	@Before
	public void setUp() throws Exception {
		// setup abstract factory by providing mock factories to create AccountService
		// and TransactionService
		// then test whether onlu one object is created by
		// MoneyTransferInMemoryServiceFactory

		factory = new MoneyTransferInMemoryServiceFactory(new AccountServiceFactory() {

			@Override
			public AccountServiceInternal createAccountServiceInternal() {
				return new MockAccountServiceInternal();
			}
		}, new TransactionServiceFactory() {

			@Override
			public TransactionService createTransactionService(AccountServiceInternal accountService) {
				TransactionService tranService = Mockito.mock(TransactionService.class);
				return tranService;
			}
		});
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetAccountService() {
		AccountService accService = factory.getAccountService();

		assertNotNull("must return an object", accService);
		assertNotNull("must return an object on repeated invocation", factory.getAccountService());

		assertEquals("Must always return the same object", accService, factory.getAccountService());
	}

	@Test
	public void testGetTransactionService() {
		TransactionService transService = factory.getTransactionService();

		assertNotNull("must return an object", transService);
		assertNotNull("must return an object on repeated invocation", factory.getTransactionService());

		assertEquals("Must always return the same object", transService, factory.getTransactionService());
	}

	@Ignore
	@Test
	public void testGetJson() {
		fail("Not yet implemented");
	}

}
