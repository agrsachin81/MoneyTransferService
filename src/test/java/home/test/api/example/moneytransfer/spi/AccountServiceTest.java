package home.test.api.example.moneytransfer.spi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import home.test.api.example.moneytransfer.spi.interfaces.AccountUpdateRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import home.test.api.example.moneytransfer.entities.AccountEntry;
import home.test.api.example.moneytransfer.mock.AccountServiceAsyncCallee;
import home.test.api.example.moneytransfer.mock.CallableObjectFactory;
import home.test.api.example.moneytransfer.mock.ThreadPoolManager;
import home.test.api.example.moneytransfer.mock.TransferServiceCallbackAdapter;
import home.test.api.example.moneytransfer.mock.TransferServiceCallback;
import home.test.api.example.moneytransfer.service.impl.mem.InMemoryAccountService;
import home.test.api.example.moneytransfer.service.internal.AccountServiceInternal;
import home.test.api.example.moneytransfer.spi.enums.AccountStatus;
import home.test.api.example.moneytransfer.spi.enums.StatusResponse;
import home.test.api.example.moneytransfer.spi.exceptions.AccountException;
import home.test.api.example.moneytransfer.spi.interfaces.AccountRequest;
import home.test.api.example.moneytransfer.spi.interfaces.AccountResult;
import home.test.api.example.moneytransfer.spi.utils.AccountRequestImpl;

import static org.junit.Assert.*;

/**
 * All the multi-
 * @author sachin
 *
 *
 */
public class AccountServiceTest {

	private static final String UNKNOWN_ACCOUNT_ID = "unknownAccountId";
	private static final double EPSILON_0_000000001 = 0.000000001;

	public AccountServiceTest() {
	}

	static AccountServiceInternal service = null;

	@BeforeClass
	public static void setup() {
		service = new InMemoryAccountService();
	}

	@Before
	public void setUp() {
	}

	@After
	public void cleanUp() {
	}

	@Test
	public void testAddAccountWithZeroBalance() {

		AccountRequest acc = new AccountRequestImpl("mkm", "9589967482");
		AccountResult res = service.addAccount(acc);
		assertEquals("AccountStatus should have been CREATED", AccountStatus.CREATED, res.getAccountStatus());
		assertEquals("Simple account add should have been successful", StatusResponse.SUCCESS, res.getStatus());

		assertNotNull("Account Id must be present", res.getAccountId());
		assertEquals("Same Name should have been returned", acc.getName(), res.getName());
		assertEquals("Same MobileNUmber should have been returned", acc.getMobileNumber(), res.getMobileNumber());
		assertEquals("Account Status should have been returned CREATED", AccountStatus.CREATED, res.getAccountStatus());

		assertEquals("Account Balance should be 0", 0.0, res.getBalance(), EPSILON_0_000000001);

		AccountResult returnedAccount = service.getAccount(res.getAccountId());

		assertEquals("Same account fetch should have succeeded but failed", StatusResponse.SUCCESS,
				returnedAccount.getStatus());
		assertEquals("Same Accountid should have been returned", res.getAccountId(), returnedAccount.getAccountId());
		assertEquals("Same Accountid should have been returned", acc.getName(), returnedAccount.getName());

		assertEquals("Same MobileNUmber should have been returned", acc.getMobileNumber(),
				returnedAccount.getMobileNumber());
		assertEquals("Account Balance should be 0", 0.0, returnedAccount.getBalance(), EPSILON_0_000000001);
	}

	@Test
	public void testAddAccountWithPositiveBalance() {

		double balance = 100.0;
		AccountRequest rekuest = new AccountRequestImpl("mkm", "9589967565", balance);
		AccountResult addResult = service.addAccount(rekuest);
		assertEquals("+vebalance AccountStatus should have been CREATED", AccountStatus.CREATED,
				addResult.getAccountStatus());
		assertEquals("Simple accnt add should have been successful", StatusResponse.SUCCESS, addResult.getStatus());

		assertNotNull("Account Id must be present", addResult.getAccountId());
		assertEquals("Same Name should have been returned", rekuest.getName(), addResult.getName());
		assertEquals("Same MobileNUmber should have been returned", rekuest.getMobileNumber(),
				addResult.getMobileNumber());
		assertEquals("Account Status should have been returned CREATED", AccountStatus.CREATED,
				addResult.getAccountStatus());

		assertEquals("Account Balance should be 0", balance, addResult.getBalance(), EPSILON_0_000000001);

		AccountResult returnedAccount = service.getAccount(addResult.getAccountId());
		assertEquals("Same accnt fetch failed", StatusResponse.SUCCESS, returnedAccount.getStatus());
		assertEquals("Same Accountid should have been returned", addResult.getAccountId(),
				returnedAccount.getAccountId());
		assertEquals("Same Accountid should have been returned", rekuest.getName(), returnedAccount.getName());

		assertEquals("Same MobileNUmber should have been returned", rekuest.getMobileNumber(),
				returnedAccount.getMobileNumber());
		assertEquals("Account Balance should be 0", balance, returnedAccount.getBalance(), EPSILON_0_000000001);
	}

	@Test
	public void testAddAccountWithNegativeBalance() {

		AccountRequest acc = new AccountRequestImpl("mkmopoiu", "gygii78789", -100.0);
		AccountResult res = service.addAccount(acc);
		assertEquals("-ve Balance accnt add should have been failed", StatusResponse.ERROR, res.getStatus());

		assertNull("Account Id must be absent", res.getAccountId());

	}

	@Test
	public void testAccountIdUniqueness() {
		Map<String, AccountResult> accountIds = new HashMap<>();
		for (int i = 0; i < 1000; i++) {
			AccountRequest rekuest = new AccountRequestImpl("mkm_" + i, "0000000" + i, 100.0 + i);
			AccountResult addResult = service.addAccount(rekuest);
			assertEquals("+vebalance inloop AccountStatus should have been CREATED", AccountStatus.CREATED,
					addResult.getAccountStatus());

			assertEquals("AccountId should have been unknown", false, accountIds.containsKey(addResult.getAccountId()));

			accountIds.put(addResult.getAccountId(), addResult);

		}
	}

	@Test
	public void testGetAccount() {
		AccountResult acc2 = service.getAccount("kpopplop");

		assertEquals("Get Account Should failed", StatusResponse.ERROR, acc2.getStatus());
	}

	static class MyClassCallableFactory implements CallableObjectFactory<Double, AccountEntry> {
		public Callable<Double> createObject(CountDownLatch latch, int index, TransferServiceCallback<AccountEntry> call) {
			return new AccountServiceAsyncCallee(latch, index, call);
		}
	}

	
	@Test
	public void testUpdateBalanceConcurrentDebit() {
		final double originalBalance = 100_000.0;
		int numOfThreads_CpAccount = 5;
		double drip = 2;
		final AtomicInteger transCounter = new AtomicInteger(1000);
		final AtomicInteger requestCounter = new AtomicInteger(100);

		double cpOriginalBalance = 0;
		final double[] values = updateBalanceConcurrentlyHelper(originalBalance, cpOriginalBalance, numOfThreads_CpAccount,
				(index, cpAccountId, orignatingAccountId) -> service.debitAccount(orignatingAccountId, drip, index + "_" + transCounter.getAndIncrement(),
						index + "_" + requestCounter.getAndIncrement(), cpAccountId));

		assertEquals(" Original balance must be equal to debited sum and remaining balance ",
				originalBalance + cpOriginalBalance * numOfThreads_CpAccount, values[0] + values[1], 0.00001);
	}

	private double[] updateBalanceConcurrentlyHelper(final double originalBalance, final double cpOriginalBalance,
													 final int numOfThreads_CP, TransferServiceCallbackAdapter<AccountEntry> transferImpl) {
		final Random random = new Random();
		final AccountResult originatingAccount = service
				.addAccount(new AccountRequestImpl("BIGACCMultithreaded", random.nextInt(999_999_999) +"", originalBalance));

		// check the balance is correct after 2 threads
		// both are continuously debiting 2 each
		// till the balance is zero
		// check how much debited and how much balance reflected
		int prefix = random.nextInt(100_999_999);

		final AccountResult[] cpAccounts = new AccountResult[numOfThreads_CP];
		List<AccountResult> list = IntStream.range(0, numOfThreads_CP).mapToObj(i -> {
			cpAccounts[i] = service.addAccount(new AccountRequestImpl("MIO_" + i, prefix +""+ i, cpOriginalBalance));
			return cpAccounts[i];
		}).collect(Collectors.toList());

		for (AccountResult result: cpAccounts) {
			System.out.println("CP ACCOUNT CREATED "+result.getAccountId()+" "+result.getMobileNumber()+" "+result.getBalance());
		}

		ThreadPoolManager<MyClassCallableFactory, Double, AccountEntry> manager = new ThreadPoolManager<MyClassCallableFactory, Double, AccountEntry>(
				numOfThreads_CP, new MyClassCallableFactory(), (index) -> {
					return transferImpl.transfer(index, cpAccounts[index].getAccountId(),
							originatingAccount.getAccountId());
				});
		List<Future<Double>> futures = manager.startAll();

		double transactedSum = futures.stream().mapToDouble(future -> {
			try {
				return future.get();
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		}).reduce(0, ArithmeticUtils::add);

		AccountResult newDebiAccount = service.getAccount(originatingAccount.getAccountId());

        assertFalse("balance must be non-negative ", isNegative(newDebiAccount.getBalance()));

		service.deleteAccount(originatingAccount.getAccountId());
		list.forEach(account -> service.deleteAccount(account.getAccountId()));
		return new double[] { transactedSum, newDebiAccount.getBalance() };
	}

	public static boolean isNegative(double d) {
		return Double.compare(d, 0.0) < 0;
	}

	public static class ArithmeticUtils {
		public static double add(double a, double b) {
			return a + b;
		}
	}

	@Test(timeout = 20000)
	public void testUpdateBalanceConcurrentlyCredit() {
		// check the balance is correct after 2 threads
		// both are continuously crediting 2 each
		// till the balance is 20000
		// check how much debited and how much balance reflected

		double originalBalance = 10.0;
		int numOfThreads = 5;
		double drip = 2;
		AtomicInteger[] counter = new AtomicInteger[numOfThreads];

		IntStream.range(0, 5).forEach(i -> {
			counter[i] = new AtomicInteger(0);
		});

		double[] values = updateBalanceConcurrentlyHelper(originalBalance, 1000, numOfThreads,
				(index, cpAccountId, originatingAccountId) -> {

					int counterValue = counter[index].incrementAndGet();
					if (counterValue > (1000 / drip)) {
						// it breaks the credit loop as it never breaks
						//System.out.println(
						//		"Exiting thread " + Thread.currentThread().getName() + " COUNTER " + counterValue);
						return null;
					} else {
						return service.creditAccount(originatingAccountId, drip, index + "_" + counterValue,
								index + "_" + counterValue, cpAccountId);
					}
				});

		assertEquals(" New balance must be equal to credited sum and oldBalance ", originalBalance - values[0],
				values[1], 0.00001);
	}

	@Test
	public void testUpdateBalanceConcurrentlyDebitCredit() {
		// check the balance is correct after 2 threads
		// one is continuously crditing 2 , while other is continously debiting 3
		// till the balance is zero or till no more transactions possible
		// check how much debited and how much balance reflected and how much total
		// credited

		double originalBalance = 1000.0;
		int numOfThreads = 5;
		double drip = 2;
		AtomicInteger[] counter = new AtomicInteger[numOfThreads];

		IntStream.range(0, 5).forEach(i -> {
			counter[i] = new AtomicInteger(0);
		});

		// if 0,2,4 is debit
		// 1,3 is credit
		final AtomicReference<String> accountId =new AtomicReference<>();

		int cpOriginalBalance = 3000;
		double[] values = updateBalanceConcurrentlyHelper(originalBalance, cpOriginalBalance, numOfThreads,
				(index, cpAccountId, originatingAccountId) -> {
					accountId.set(originatingAccountId);
					int counterValue = counter[index].incrementAndGet();
					if ((index == 1 || index == 3)) {
						if ((counterValue > (cpOriginalBalance / drip))) {
							// it breaks the credit loop as it never breaks
							System.out.println(
									"Exiting thread " + Thread.currentThread().getName() + " COUNTER " + counterValue +" IDX "+index);
							return null;
						} else {
							return service.creditAccount(originatingAccountId, drip, index + "_" + counterValue,
									index + "_" + counterValue, cpAccountId);
						}
					} else {
						return service.debitAccount(originatingAccountId, drip, index + "_" + counterValue,
								index + "_" + counterValue, cpAccountId);
					}
				});

		//System.out.println(" TOTAL TRANSACTED SUM iS " + values[0]+ " NEWORIGINATING BALANCE "+values[1]);
		assertEquals(" New balance must be equal to credited sum and oldBalance ", originalBalance - values[0],
				values[1], 0.00001);
	}

	@Ignore
	@Test
	public void testEditAccount() {
	
		AccountUpdateRequest acc = new AccountRequestImpl("edit_accountTest1", "mobileNumber_edit1");
		AccountResult res = service.addAccount(acc);
		assertEquals("AccountStatus should have been CREATED", AccountStatus.CREATED, res.getAccountStatus());
		assertEquals("Simple accnt add should have been successful", StatusResponse.SUCCESS, res.getStatus());
		double origBalance = res.getBalance();
		assertNotNull("Account Id must be present", res.getAccountId());
		
		try {
			String mobileNumberEdited = "mobileNumber_edit2";
			String name_afterEdit = "edit_accountTest2";
			res = service.editAccount(new AccountRequestImpl(res.getAccountId(), name_afterEdit, mobileNumberEdited, 200.0));
		
			assertEquals("AccountStatus should have been CREATED", AccountStatus.CREATED, res.getAccountStatus());
			assertEquals("Edit account should have been succeeded", StatusResponse.SUCCESS, res.getStatus());

			assertNotNull("Account Id must be present", res.getAccountId());
			assertEquals("Name should have been changed", name_afterEdit , res.getName());
			assertEquals("MobileNumber should have been changed", mobileNumberEdited , res.getMobileNumber());
			assertEquals("balance should not be changed. Do Transaction to change balance", origBalance, res.getBalance(),0.00001);
			
		} catch (AccountException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}

	@Test
	public void testEditUnknownAccount() {
		AccountUpdateRequest acc = new AccountRequestImpl(UNKNOWN_ACCOUNT_ID,"unknwonAccountName", "mobileNumber_Sample",0.0);
		boolean exceptionThrown = false;
		AccountResult res;
		try {
			res = service.editAccount(acc);
			
		} catch (AccountException e) {
			e.printStackTrace();
			//if exception is thrown that means edit Account is working as expected passed
			//so rechecking whether it hs not created an account by mistake
			exceptionThrown = true;
		}

		res = service.getAccount(UNKNOWN_ACCOUNT_ID);

		assertEquals("AccountStatus should have been UNKNOWN", AccountStatus.UNKNOWN, res.getAccountStatus());
		assertEquals("Account get for unknown acvount should have been failed", StatusResponse.ERROR, res.getStatus());
		assertNotNull("Account Id must be present", res.getAccountId());
	}
	
	@Ignore
	@Test
	public void testDuplicateAddAccount() {
		AccountRequest acc = new AccountRequestImpl("dup_accountTest", "mobileNumber_dup");
		AccountResult res = service.addAccount(acc);
		assertEquals("AccountStatus should have been CREATED", AccountStatus.CREATED, res.getAccountStatus());
		assertEquals("Simple accnt add should have been successful", StatusResponse.SUCCESS, res.getStatus());

		assertNotNull("Account Id must be present", res.getAccountId());
		
		res = service.addAccount(acc);
		assertEquals("AccountStatus should have been UNKNOWN", AccountStatus.UNKNOWN, res.getAccountStatus());
		assertEquals("Dup account should have been failed", StatusResponse.ERROR, res.getStatus());

		assertNotNull("Account Id must be present", res.getAccountId());
	}
	
	@Test
	public void testDeleteAccount() {
		AccountRequest acc = new AccountRequestImpl("mkm_TOBEDLETED", "mobileNumber_TOBEDELETED");
		AccountResult res = service.addAccount(acc);
		assertEquals("AccountStatus should have been CREATED", AccountStatus.CREATED, res.getAccountStatus());

		res = service.deleteAccount(res.getAccountId());

		assertEquals("AccountStatus should have been DELETED", AccountStatus.DELETED, res.getAccountStatus());
		assertEquals("Status should have been SUCCESS", StatusResponse.SUCCESS, res.getStatus());
		
	}

	@Test
	public void testAccountDoesNotExist() {
		boolean res = service.accountExist("mkkmklklkl");
		assertEquals("AccountStatus should have been DELETED", false, res);
	}
}
