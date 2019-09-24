package home.test.api.exampl.moneytransfer.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import home.test.api.example.moneytransfer.service.impl.MoneyTransferInMemoryServiceFactory;
import home.test.api.example.moneytransfer.spi.MoneyTransferAbstractServiceFactory;

public class ITMoneyTransferAbstractServiceFactoryTest {

	
	private MoneyTransferAbstractServiceFactory factory;
	private static MoneyTransferAbstractServiceFactory staticFactory;
	@BeforeClass
	public static void createClass() {
		staticFactory = MoneyTransferInMemoryServiceFactory.getInstance();
	}
	
	@Before
	public void create() {
		factory = MoneyTransferInMemoryServiceFactory.getInstance();
	}	
	
	@Test
	public void testMoneyTransferInMemoryServiceFactoryInstances() {
		assertEquals("must return a not null object  of AbstractFactory ", staticFactory, factory);
	}
	
	@Test
	public void testCreateAccountService() {
		
		//to test how many time someone asks for AccountService it must return the same object
		assertNotNull("must return a not null object  of AccountService", staticFactory.getAccountService());
		assertNotNull("must return a not null object  of AccountService", factory.getAccountService());
		
		assertEquals("must be a single object of AccountService", staticFactory.getAccountService(), factory.getAccountService());
	}

	@Test
	public void testCreateTransactionService() {
		
		//to test how many time someone asks for TransactionService it must return the same object
		assertNotNull("must return a not null object  of TransactionService", staticFactory.getTransactionService());
		assertNotNull("must return a not null object  of TransactionService", factory.getTransactionService());
		
		assertEquals("must be a single object of TransactionService", staticFactory.getTransactionService(), factory.getTransactionService());
	}

}
