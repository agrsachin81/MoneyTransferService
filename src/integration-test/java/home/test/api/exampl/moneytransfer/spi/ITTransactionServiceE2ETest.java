
package home.test.api.exampl.moneytransfer.spi;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import home.test.api.example.moneytransfer.mock.CallableObjectFactory;
import home.test.api.example.moneytransfer.mock.ThreadPoolManager;
import home.test.api.example.moneytransfer.mock.TransferServiceCalbackAdapter;
import home.test.api.example.moneytransfer.mock.TransferServiceCallback;
import home.test.api.example.moneytransfer.service.impl.MoneyTransferInMemoryServiceFactory;
import home.test.api.example.moneytransfer.spi.AccountService;
import home.test.api.example.moneytransfer.spi.AccountServiceTest.ArithmeticUtils;
import home.test.api.example.moneytransfer.spi.MoneyTransferAbstractServiceFactory;
import home.test.api.example.moneytransfer.spi.TransactionService;
import home.test.api.example.moneytransfer.spi.enums.TransactionType;
import home.test.api.example.moneytransfer.spi.interfaces.AccountResult;
import home.test.api.example.moneytransfer.spi.interfaces.TransactionResult;
import home.test.api.example.moneytransfer.spi.utils.AccountRequestImpl;
import home.test.api.example.moneytransfer.spi.utils.TransactionRequestImpl;

/**
 * MOCK shall not be used while executing integration tests, end 2 end tests As
 * the services will be used concurrently hence it is important to test the
 * service concurrently
 * 
 * @author sachin
 *
 *
 */
public class ITTransactionServiceE2ETest {

	static TransactionService transactService;
	static AccountService service;
	static String emptyAccId1;
	static String emptyAccId2;
	static String accIdwith200_000;
	static String accIdWith_2_000_000;

	@BeforeClass
	public static void setup() {
		MoneyTransferAbstractServiceFactory factory = MoneyTransferInMemoryServiceFactory.getInstance();
		service = factory.getAccountService();
		AccountRequestImpl acc = new AccountRequestImpl("ACC1", "+91989098089");
		emptyAccId1 = service.addAccount(acc).getAccountId();
		AccountRequestImpl acc2 = new AccountRequestImpl("ACC2", "8u9877655466");
		emptyAccId2 = service.addAccount(acc2).getAccountId();

		accIdwith200_000 = service.addAccount(new AccountRequestImpl("ACC3", "000000000", 200_000.0)).getAccountId();

		// System.out.println("accIdWith200 " + accIdwith200_000);
		// System.out.println("emptyAccId1 " + emptyAccId1);
		// System.out.println("emptyAccId2 " + emptyAccId2);

		transactService = factory.getTransactionService();
	}

	@Test
	public void testConcurrentTransactionDebitSameAccount() {
		// Single account (10000) is debit from multiple threads say 5 to five
		// accounts(zero) by a drip of 2 each
		// check till it stops transferring further
		// check the balance of all other accounts sum of which should be amount debit
		// check the balance is not less then zero
		double originalBalance = 80000.0;
		int numOfThreads = 5;
		AtomicInteger counter = new AtomicInteger(1);
		double[] retValues = testConcurrentTransactionHelper(numOfThreads, originalBalance, 0,
				(index, cpAccounId, originAccounId) -> {

					int counterValue = counter.getAndIncrement();
					return transactService.transfer(
							new TransactionRequestImpl(cpAccounId, 2, index + "_" + counterValue), originAccounId);
				});

		System.out.println("TotalDebited " + retValues[0] + " total credited " + retValues[1] + " OrigaccountBalance "
				+ retValues[2] + " totalCpBalance " + retValues[3]);
		assertEquals(" original balance must be ekual to debited sum and remaining balance ", originalBalance,
				retValues[0] + retValues[2], 0.00001);

		assertEquals("reduced balance must be ekual to sum of the balances of all the credited accounts",
				originalBalance - retValues[2], retValues[3], 0.00001);
	}

	private double[] testConcurrentTransactionHelper(int numOfThreads_CP, double originalBalance, double cpOrigBalance,
			TransferServiceCalbackAdapter<TransactionResult> transferCall) {

		AccountResult debitedAccount = service
				.addAccount(new AccountRequestImpl("BIGACCMulti)TRANSFER", "0000000", originalBalance));

		System.out.println("DEBITED ACCNT ID " + debitedAccount.getAccountId() + " BALANCE " + originalBalance
				+ " objeRef " + debitedAccount.toString());

		AccountResult[] creditedAccounts = new AccountResult[numOfThreads_CP];
		List<AccountResult> list = IntStream.range(0, numOfThreads_CP).mapToObj(i -> {
			creditedAccounts[i] = service
					.addAccount(new AccountRequestImpl("MIO_" + i, "123456798" + i, cpOrigBalance));
			System.out.println("CP ACCNT ID " + creditedAccounts[i].getAccountId());
			return creditedAccounts[i];
		}).collect(Collectors.toList());

		ThreadPoolManager<TransactionAsyncCallableFactory, Double[], TransactionResult> manager = new ThreadPoolManager<TransactionAsyncCallableFactory, Double[], TransactionResult>(
				numOfThreads_CP, new TransactionAsyncCallableFactory(), (index) -> {
					String cpAccountId = creditedAccounts[index].getAccountId();
					return transferCall.transfer(index, cpAccountId, debitedAccount.getAccountId());
				});
		List<Future<Double[]>> futures = manager.startAll();

		double debitedSum = futures.stream().mapToDouble(future -> {
			try {
				return future.get()[0];
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			return 0;
		}).reduce(0, ArithmeticUtils::add);

		double creditedSum = futures.stream().mapToDouble(future -> {
			try {
				return future.get()[1];
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			return 0;
		}).reduce(0, ArithmeticUtils::add);

		AccountResult newDebiAccount = service.getAccount(debitedAccount.getAccountId());

		System.out.println(
				"DEBITED ACCNT NEW BALANCE " + newDebiAccount.getBalance() + " objeRef " + newDebiAccount.toString());

		assertEquals("balance must be non-negative ", false, isNegative(newDebiAccount.getBalance()));

		// cp Account balance
		double cpTotalBalance = list.stream().mapToDouble(account -> {
			AccountResult cpAccount = service.getAccount(account.getAccountId());
			assertEquals("balance must be non-negative ", false, isNegative(cpAccount.getBalance()));
			return cpAccount.getBalance();
		}).reduce(0, ArithmeticUtils::add);

		service.deleteAccount(debitedAccount.getAccountId());
		list.stream().forEach(account -> {
			service.deleteAccount(account.getAccountId());
		});
		return new double[] { debitedSum, creditedSum, newDebiAccount.getBalance(), cpTotalBalance };
	}

	@Test
	public void testConcurrentTransactionCreditSameAccount() {
		// Single account(zero) is credited from mulitple threads say 5 from five
		// accounts (each having 1000) by a drip of 2 each
		// stop threads till it stops transferring further
		// check the balance of all other accounts should be zero, single account should
		// be at balance 5000
		// check the balance is not less then zero

		double originalBalance = 80.0;
		int numOfThreads = 5;
		double drip = 2;

		AtomicInteger[] counter = new AtomicInteger[numOfThreads];

		IntStream.range(0, numOfThreads).forEach(i -> {
			counter[i] = new AtomicInteger(0);
		});
		int cpOrigBalance = 1000;
		double[] retValues = testConcurrentTransactionHelper(numOfThreads, originalBalance, cpOrigBalance,
				(index, cpAccounId, originAccounId) -> {
					int counterValue = counter[index].getAndIncrement();
					return transactService.transfer(new TransactionRequestImpl(cpAccounId, drip,
							index + "_" + counterValue, TransactionType.CREDIT_ACCOUNT), originAccounId);
				});

		System.out.println("TotalDebited " + retValues[0] + " total credited " + retValues[1]
				+ " OrigaccountCurrentBalance " + retValues[2] + " totalCpBalance " + retValues[3]);
		assertEquals(" New balance must be ekual to debited sum and old balance ", originalBalance + retValues[1],
				retValues[2], 0.00001);

		assertEquals(" Total Credit must be ekual to cpAccountBalance ", cpOrigBalance * numOfThreads, retValues[1],
				0.00001);

		assertEquals("Total balance must be zero for all the cp accounts", 0, retValues[3], 0.00001);

	}

	@Test
	public void testConcurrentTransactionCreditDebitSameAccount() {
		// Single account(zero) is credited/debited from multiple threads say 5
		// among 2 are doing debit and 3 are crediting
		// accounts (each having 1000) by a drip of 2 each
		// stop threads till it stops transferring further
		// check the balance of all other accounts should be zero, single account should
		// be at balance 5000
		// check the balance is not less then zero

		double originalBalance = 5000.0;
		int numOfThreads = 5;
		double drip = 2;

		AtomicInteger[] counter = new AtomicInteger[numOfThreads];

		IntStream.range(0, numOfThreads).forEach(i -> {
			counter[i] = new AtomicInteger(0);
		});
		int cpOrigBalance = 3000;
		double[] retValues = testConcurrentTransactionHelper(numOfThreads, originalBalance, cpOrigBalance,
				(index, cpAccounId, originAccounId) -> {
					int counterValue = counter[index].getAndIncrement();

					if (index == 1 || index == 3) {
						return transactService.transfer(
								new TransactionRequestImpl(cpAccounId, 2, index + "_" + counterValue), originAccounId);
					}
					return transactService.transfer(new TransactionRequestImpl(cpAccounId, drip,
							index + "_" + counterValue, TransactionType.CREDIT_ACCOUNT), originAccounId);
				});

		System.out.println("TotalDebited " + retValues[0] + " total credited " + retValues[1]
				+ " OrigaccountCurrentBalance " + retValues[2] + " totalCpBalance " + retValues[3]);
		
		assertEquals(" New balance must be ekual to debited sum and old balance ", originalBalance + cpOrigBalance* numOfThreads,
				retValues[2] + retValues[3], 0.00001);

		
	}

	@Ignore
	@Test
	public void testGetTransactions() {

	}

	@Ignore
	@Test
	public void testGetUnknownAccountIdTransactions() {

	}

	@Ignore
	@Test
	public void testGetTransactionStatus() {

	}

	static class TransactionAsyncCallableFactory implements CallableObjectFactory<Double[], TransactionResult> {
		public Callable<Double[]> createObject(CountDownLatch latch, int i,
				TransferServiceCallback<TransactionResult> call) {
			return new TransactionServiceAsyncCallee(latch, i, call);
		}
	}

	public static boolean isNegative(double d) {
		return Double.compare(d, 0.0) < 0;
	}
}
