
package home.test.api.example.moneytransfer.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Collection;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import home.test.api.example.moneytransfer.entities.Account;
import home.test.api.example.moneytransfer.mock.MockAccountServiceInternal;
import home.test.api.example.moneytransfer.service.impl.mem.InMemoryTransactionService;
import home.test.api.example.moneytransfer.spi.enums.StatusResponse;
import home.test.api.example.moneytransfer.spi.enums.TransactionStatus;
import home.test.api.example.moneytransfer.spi.enums.TransactionType;
import home.test.api.example.moneytransfer.spi.interfaces.AccountResult;
import home.test.api.example.moneytransfer.spi.interfaces.TransactionRekuest;
import home.test.api.example.moneytransfer.spi.interfaces.TransactionResult;
import home.test.api.example.moneytransfer.spi.utils.AccountRequestImpl;
import home.test.api.example.moneytransfer.spi.utils.TransactionRequestImpl;

/**
 * Here we need to mock the account Service, and since we the internal design, we known the multi-threaded aspects are not relevant here
 * because it is not maintaining a shared state, other then transactions cache
 * @author sachin
 *
 *
 */
public class TransactionServiceTest {

	static TransactionService transactService;
	static MockAccountServiceInternal mockAccountService;
	static String emptyAccId1;
	static String emptyAccId2;
	static String accIdwith200_000;

	@BeforeClass
	public static void setup() {
		mockAccountService = new MockAccountServiceInternal();
		AccountRequestImpl acc = new AccountRequestImpl("ACC1", "+91989098089");
		emptyAccId1 = mockAccountService.addAccount(acc).getAccountId();
		AccountRequestImpl acc2 = new AccountRequestImpl("ACC2", "8u9877655466");
		emptyAccId2 = mockAccountService.addAccount(acc2).getAccountId();

		accIdwith200_000 = mockAccountService.addAccount(new AccountRequestImpl("ACC3", "000000000", 200_000.0))
				.getAccountId();

		System.out.println("accIdWith200 " + accIdwith200_000);
		System.out.println("emptyAccId1 " + emptyAccId1);
		System.out.println("emptyAccId2 " + emptyAccId2);

		transactService = new InMemoryTransactionService(mockAccountService);
	}

	@After
	public void afterEach() {
		mockAccountService.acceptAllAccountcreditDebit();
	}

	@Test
	public void testNullValuePassed() {
		TransactionRekuest transaction = new TransactionRequestImpl(null, 100.0, "908980");

		TransactionResult response = transactService.transfer(transaction, null);

		assertEquals("Null Account should throw error", TransactionStatus.INVALID_INPUT,
				response.getTransactionStatus());
	}

	@Test
	public void testEmptyAccountTransaction() {
		TransactionRekuest transaction = new TransactionRequestImpl(emptyAccId2, 100.0, "909090");

		TransactionResult response = transactService.transfer(transaction, emptyAccId1);

		assertEquals("Empty Account shoudl throw error", TransactionStatus.DEBIT_FAILED,
				response.getTransactionStatus());
	}

	@Test
	public void testBothSideTransactionStatus() {

		mockAccountService.acceptAllAccountcreditDebit();

		TransactionRekuest transaction = new TransactionRequestImpl(emptyAccId1, 100.0, "909090");
		Account accnt = mockAccountService.getAccountInstance(accIdwith200_000);
		Account accnt2 = mockAccountService.getAccountInstance(emptyAccId1);
		
		double oldBalance = accnt.getBalance();
		TransactionResult response = transactService.transfer(transaction, accIdwith200_000);

		assertEquals("Empty Account must succedd", TransactionStatus.DONE, response.getTransactionStatus());
		assertEquals("Empty Account must increase", 100.0,accnt2.getBalance(),0.00001 );
		assertEquals("Account must decrease", oldBalance-100.0,accnt.getBalance(),0.00001 );
		String transId = response.getTransactionReferenceId();
		// check transactions for both accounts and they should match
		// use a method to verify transactions

		Collection<TransactionResult> trans = transactService.getTransactions(accIdwith200_000, 1);
		Collection<TransactionResult> trans2 = transactService.getTransactions(emptyAccId1, 1);
		TransactionResult debitAccntTrans = trans.stream().filter(result -> {
			return result.getTransactionReferenceId().equals(transId);
		}).findAny().orElse(null);

		TransactionResult creditAccntTrans = trans2.stream().filter(result -> {
			return result.getTransactionReferenceId().equals(transId);
		}).findAny().orElse(null);

		System.out.println("END transfer ========");
		assertNotNull("must have found an transactin Entry for debited Account", debitAccntTrans);
		assertNotNull("must have found an transactin Entry for credited Account ", creditAccntTrans);
	}

	

	@Test
	public void testDebitisFailed() {
		mockAccountService.rejectDebitOnly();
		Account accnt = mockAccountService.getAccountInstance(accIdwith200_000);
		Account accnt2 = mockAccountService.getAccountInstance(emptyAccId1);
		double oldBalance = accnt.getBalance();
		double accnt2OldBalance = accnt2.getBalance();
		TransactionRekuest transaction = new TransactionRequestImpl(emptyAccId1, 100.0, "909090");
		TransactionResult response = transactService.transfer(transaction, accIdwith200_000);

		assertEquals("Debite is failed", TransactionStatus.DEBIT_FAILED, response.getTransactionStatus());
		assertEquals("Overall status must be failure", StatusResponse.ERROR, response.getStatus());
		// check transactions for both accounts and they should match
		// use a method to verify transactions
		assertEquals("Balance should remain same", oldBalance, accnt.getBalance(), 0.0001);
		assertEquals("Balance should remain same", accnt2OldBalance, accnt2.getBalance(),0.00001 );
	}

	@Test
	public void testCreditFailedRollBackNotSuccessfull() {
		mockAccountService.rejectCreditOnly();

		Account accnt = mockAccountService.getAccountInstance(accIdwith200_000);
		Account accnt2 = mockAccountService.getAccountInstance(emptyAccId1);
		double oldBalance = accnt.getBalance();
		double accnt2OldBalance = accnt2.getBalance();
		TransactionRekuest transaction = new TransactionRequestImpl(emptyAccId1, 100.0, "909090");
		TransactionResult response = transactService.transfer(transaction, accIdwith200_000);

		assertEquals("Empty Account must succedd", TransactionStatus.CREDIT_FAILED_DEBIT_NOT_REVERTED,
				response.getTransactionStatus());
		assertEquals("Overall status must be failure", StatusResponse.ERROR, response.getStatus());

		assertEquals("Debited Balance should decrease", oldBalance - 100.0, accnt.getBalance(), 0.0001);
		assertEquals("Account2 Credited Balance should remain same", accnt2OldBalance, accnt2.getBalance(),0.00001 );
	}

	@Test
	public void testCreditFailedRollBackSuccessfull() {
		mockAccountService.rejectOnlyOneCredit();

		Account accnt = mockAccountService.getAccountInstance(accIdwith200_000);
		double oldBalance = accnt.getBalance();
		TransactionRekuest transaction = new TransactionRequestImpl(emptyAccId1, 100.0, "909090");
		TransactionResult response = transactService.transfer(transaction, accIdwith200_000);

		assertEquals("Empty Account must succedd", TransactionStatus.CREDIT_FAILED_DEBIT_REVERTED,
				response.getTransactionStatus());
		assertEquals("Overall status must be failure", StatusResponse.ERROR, response.getStatus());

		assertEquals("Balance should be reverted", oldBalance , accnt.getBalance(), 0.0001);
	}

	
	@Test
	public void testCreditedCashTransaction() {
		AccountResult accRes = mockAccountService.addAccount(new AccountRequestImpl("DebitCashAccId", "989897997"));
		double amount_credited = 100.0;
		TransactionRekuest transaction = new TransactionRequestImpl(amount_credited, "9089809090",TransactionType.CREDIT_CASH);
		TransactionResult response = transactService.transfer(transaction, accRes.getAccountId());
		assertEquals("Overall status must be success", StatusResponse.SUCCESS, response.getStatus());
		assertEquals("credit cash should have passed on EmptyAccount", TransactionStatus.DONE,	response.getTransactionStatus());
		AccountResult newAcctRes = mockAccountService.getAccount(accRes.getAccountId());
		assertEquals("credit cash balance does not match", amount_credited ,newAcctRes.getBalance(),0.00001);
	}
	
	
	@Test
	public void testDebitCashTransaction() {
		AccountResult accRes = mockAccountService.addAccount(new AccountRequestImpl("DebitCashAccId", "989897997",900.0));
		double amount_debited = 100.0;
		TransactionRekuest transaction = new TransactionRequestImpl(amount_debited, "9089809090",TransactionType.DEBIT_CASH);
		TransactionResult response = transactService.transfer(transaction, accRes.getAccountId());
		assertEquals("Overall status must be Success", StatusResponse.SUCCESS, response.getStatus());
		assertEquals("Debit cash should have passed on EmptyAccount", TransactionStatus.DONE,	response.getTransactionStatus());
		AccountResult newAcctRes = mockAccountService.getAccount(accRes.getAccountId());
		assertEquals("Debit cash balance should match", accRes.getBalance()- amount_debited ,newAcctRes.getBalance(),0.00001);
	}
	
	
	@Test
	public void testcpAcountIdMissingWhileAccountTransaction() {
		AccountResult accRes = mockAccountService.addAccount(new AccountRequestImpl("DebitAccId_invalidInput", "989897997",900.0));
		double amount_debited = 100.0;
		TransactionRekuest transaction = new TransactionRequestImpl(null,amount_debited, "9089809090");
		TransactionResult response = transactService.transfer(transaction, accRes.getAccountId());
		assertEquals("Overall status must be Success", StatusResponse.ERROR, response.getStatus());
		assertEquals("Debit cash should have passed on EmptyAccount", TransactionStatus.INVALID_INPUT,	response.getTransactionStatus());
	}
	
	@Ignore
	@Test
	public void testDeleteTransactionisNotAllowed() {

	}
	
	@Ignore
	@Test
	public void testTransactionGetter() {
		fail("to be implemented");
	}

	@Ignore
	@Test
	public void testUpdateTransactionisNotAllowed() {

	}
}
