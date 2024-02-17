package home.test.api.example.moneytransfer.mock;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ThreadPoolManager<V extends CallableObjectFactory<T, P>, T, P> {

	private final List<Callable<T>> list;

	private final ExecutorService service;
	private final CountDownLatch latch;

	public ThreadPoolManager(int numOfThreads, V task, TransferServiceCallback<P> transferCall) {
		service = Executors.newFixedThreadPool(numOfThreads);
		latch = new CountDownLatch(numOfThreads);
		list = IntStream.range(0, 5).mapToObj(i -> task.createObject(latch, i, transferCall))
				.collect(Collectors.toList());
	}

	public List<Future<T>> startAll() {
		try {
			List<Future<T>> retFutures = service.invokeAll(list);
			
			latch.await();

			service.shutdown();

			return retFutures;

		} catch (InterruptedException e) {
			e.printStackTrace();
			service.shutdown();			
			return null;
		}
	}

}