package home.test.api.example.moneytransfer.mock;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import home.test.api.example.moneytransfer.entities.AccountEntry;
import home.test.api.example.moneytransfer.spi.exceptions.AccountException;

public class AccountServiceAsyncCallee implements Callable<Double> {
	final CountDownLatch latch;
	final int index;
	final TransferServiceCallback<AccountEntry> transferCall;
	
	

	public AccountServiceAsyncCallee(CountDownLatch latch, int i, TransferServiceCallback<AccountEntry> call) {
		this.latch = latch;
		this.index = i;
		this.transferCall = call;
	}
	
	

	@Override
	public Double call() throws Exception {
		double transacted = 0;
		while (true) {
			try {
				AccountEntry result = transferCall.transfer(index);
				
				if(result==null) {
					break;
				}
				System.out.println(Thread.currentThread().getName() + " AMOUNT [" + result.getAmount() + "] ID "
						+ result.getRekuestId() + " ACCID " + result.getCpAccountId() + " OldBal["
						+ result.getOldBalance() + "] newBal[" + result.getNewBalance() + "]");
				if(result.isDebit())
					transacted += result.getAmount();
				else {
					transacted -=result.getAmount();
				}
			} catch (AccountException e) {
				// e.printStackTrace();
				break;
			}
		}
		latch.countDown();
		System.out.println( "----------IDX "+index +" Total Debuted "+transacted);
		return transacted;
	}
}