package home.test.api.example.moneytransfer.spi.interfaces;

public interface AccountRequest {
	public String getMobileNumber();

	public String getName();

	String getAccountId();

	double getBalance();
}
