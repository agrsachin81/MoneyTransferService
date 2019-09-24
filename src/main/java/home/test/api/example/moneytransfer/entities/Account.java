
package home.test.api.example.moneytransfer.entities;

import java.util.concurrent.atomic.AtomicReference;

import com.google.common.util.concurrent.AtomicDouble;

import home.test.api.example.moneytransfer.spi.enums.AccountStatus;

public class Account{
	
	private AtomicReference<String> name = new AtomicReference<>();
	private AtomicReference<String> mobileNumber = new AtomicReference<>();
	private final String accountId ;
	private final AtomicDouble balance = new AtomicDouble(0.0);
	
	private final AtomicReference<AccountStatus> status = new AtomicReference<>(AccountStatus.CREATED);
	
	public Account(String name, String mobileNumber, String accountId){
		this(name, mobileNumber, accountId, 0.0);
	}
	
	public Account(String name, String mobileNumber, String accountId, Double balance){
		this.name.set(name);
		this.mobileNumber.set(mobileNumber);
		this.accountId = accountId;
		this.balance.set(balance);
	}
	

	public String getAccountId() {
		return accountId;
	}
	
	public String getMobileNumber() {
		return mobileNumber.get();
	}
	
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber.set(mobileNumber);
	}
	
	public void setName(String name) {
		this.name.set(name);
	}
	
	public String getName() {
		return name.get();
	}
	
	public Double getBalance() {
		return balance.doubleValue();
	}
		
	public boolean updateBalance(double newBalance, double expected){
		return balance.compareAndSet(expected, newBalance);
	}	
	
		
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Account) {
			Account oth= (Account)obj;
			
			if(oth.accountId.equals(this.accountId)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.accountId.hashCode();
	}

	public AccountStatus getAccountStatus() {
		return status.get();
	}
	
	public void setAccountDeleted(){
		this.status.set(AccountStatus.DELETED);
	}
}

