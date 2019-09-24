
package home.test.api.exampl.moneytransfer.spi;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import home.test.api.example.moneytransfer.mock.TransferServiceCallback;
import home.test.api.example.moneytransfer.spi.enums.StatusResponse;
import home.test.api.example.moneytransfer.spi.interfaces.TransactionResult;

public class TransactionServiceAsyncCallee implements Callable<Double[]> {
	final CountDownLatch latch;
	final int index;
	final TransferServiceCallback<TransactionResult> transferCall;

	public TransactionServiceAsyncCallee(CountDownLatch latch, int i, TransferServiceCallback<TransactionResult> call) {
		this.latch = latch;
		this.index = i;
		this.transferCall = call;
	}

	@Override
	public Double[] call() throws Exception {
		double transacted = 0;
		double debited = 0;
		double credited = 0;
		while (true) {
			TransactionResult result = transferCall.transfer(index);
			// System.out.println(Thread.currentThread().getName() + " AMOUNT [" +
			// result.getAmount() + "] ID "
			// + result.getTransactionRekuestId() + " ACCID " + result.getCpAccountId());

			if (result == null || result.getStatus() == StatusResponse.ERROR) {
				break;
			}
			if (result.isDebit()) {
				debited += result.getAmount();
			} else {
				credited += result.getAmount();
			}

			transacted += transacted;
		}

		latch.countDown();
		//System.out.println("----------IDX " + index + " Total transacted " + transacted + " debited " + debited
		//		+ " credted " + credited);
		return new Double[] { debited, credited };
	}
}
