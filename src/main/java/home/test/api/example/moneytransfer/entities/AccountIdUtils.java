package home.test.api.example.moneytransfer.entities;

import java.util.concurrent.atomic.AtomicLong;

public final class AccountIdUtils {

	//need an elaborate method to generate account Id
	// need to persist the current seed
	private static final AtomicLong CURRENT_SEQUENCE = new AtomicLong(100000);
	
	public static final String PREFIX  ="PA_";
	
	
	public static String generateNext() {
		return PREFIX+ CURRENT_SEQUENCE.incrementAndGet();
	}
}
