 
package home.test.api.example.moneytransfer.mock;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public interface CallableObjectFactory<T,P> {
	Callable<T> createObject(CountDownLatch latch, int index, TransferServiceCallback<P> transferCall);
}
