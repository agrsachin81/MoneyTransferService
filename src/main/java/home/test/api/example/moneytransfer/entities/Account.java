
package home.test.api.example.moneytransfer.entities;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

import home.test.api.example.moneytransfer.spi.enums.AccountStatus;

public class Account{

	public static final String INVALID_AMOUNT = "Invalid amount";
	private final AtomicReference<String> name = new AtomicReference<>();
	private final AtomicReference<String> mobileNumber = new AtomicReference<>();
	private final String accountId ;	
	private final static ThreadLocal<AccountState> swapStateReference = ThreadLocal.withInitial(()->{
		return new AccountState(0, AccountStatus.CREATED);
	});
	
	
	private final static ThreadLocal<int[]> staticStampHolder = ThreadLocal.withInitial(()->{
		return new int[1];
	});
	
	private final AtomicStampedReference<AccountState> state ;
	private final AtomicInteger stateStampGenerator = new AtomicInteger(0);
	
	public Account(String name, String mobileNumber, String accountId){
		this(name, mobileNumber, accountId, 0.0);
	}
	
	public Account(String name, String mobileNumber, String accountId, Double balance){
		this.name.set(name);
		this.mobileNumber.set(mobileNumber);
		this.accountId = accountId;		
		state = new AtomicStampedReference<>(new AccountState(balance, AccountStatus.CREATED), stateStampGenerator.getAndIncrement());
	}
	

	public String getAccountId() {
		return accountId;
	}
	
	public String getMobileNumber() {
		return mobileNumber.get();
	}	
		
	public String getName() {
		return name.get();
	}
	
	public Double getBalance() {
		return this.state.getReference().balance.doubleValue();
	}
		
	public boolean credit(double amount, AccountUpdateResult outResult) {
		if(amount <0) {
			outResult.fillFailure(INVALID_AMOUNT);
			return false;
		}
		return modifyBalance(amount, outResult);
	}
	
	public boolean debit(double amount, AccountUpdateResult outResult) {
		if(amount <0) {
			outResult.fillFailure(INVALID_AMOUNT);
			return false;
		}
		return modifyBalance(-1* amount, outResult);
	}
	
	
	private boolean modifyBalance(double delta,AccountUpdateResult outResult){
		final int[] stampHolder = staticStampHolder.get();
		final AccountState newState = swapStateReference.get();
		final int transStamp = stateStampGenerator.getAndIncrement();
		outResult.fillFailure("Unable to modify Balance; check status and balance.");
		while(true) {
			final AccountState state = this.state.get(stampHolder);
			newState.init(state);
			final boolean result = newState.updateBalance(delta);
			if(!result) return false;
			
			if(this.state.compareAndSet(state, newState, stampHolder[0], transStamp)) {
				swapStateReference.set(state);
				outResult.fillSuccess( state, newState);
				return true;
			}
		}
	}
	
		
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Account) {
			Account oth= (Account)obj;
            return oth.accountId.equals(this.accountId);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.accountId.hashCode();
	}

	public AccountStatus getAccountStatus() {
		return this.state.getReference().status;
	}
	
	public boolean setAccountDeleted(AccountUpdateResult outResult){
		final int[] stampHolder = staticStampHolder.get();
		final AccountState newState = swapStateReference.get();
		outResult.fillFailure("Unable to change status");
		while(true) {
			final AccountState state = this.state.get(stampHolder);
			newState.init(state);
			newState.updateStatus(AccountStatus.DELETED);						
			if(this.state.compareAndSet(state, newState, stampHolder[0], stateStampGenerator.getAndIncrement())) {
				swapStateReference.set(state);
				outResult.fillSuccess(state, newState);
				return true;
			}
		}
	}
	
	private static class AccountState {
		private AccountStatus status;
		private BigDecimal balance;
		private static BigDecimal ZERO = new BigDecimal(0);
		
		AccountState(double balance, AccountStatus status){
			this.status = status;
			this.balance = new BigDecimal(balance);
		}
		
		void init(AccountState state){
			this.status = state.status;
			this.balance = state.balance;
		}
		
		boolean updateBalance(double amount) {
			
			if(amount < 0 && !AccountStatus.isDebitable(status)) return false;
			if(amount> 0 && !AccountStatus.isCreditable(status)) return false;

			final BigDecimal added = this.balance.add(new BigDecimal(amount));
			if(amount < 0 && added.compareTo(ZERO) < 0) {
				return false;
			}			
			this.balance = added;
			return true;
		}
		
		void updateStatus(AccountStatus newStatus) {
			this.status = newStatus;
		}
	}
	
	public static class AccountUpdateResult {		
		private final AtomicBoolean updateStatus = new AtomicBoolean(false);
		private AccountStatus oldStatus;
		private AccountStatus newStatus;
		private BigDecimal oldBalance;
		private BigDecimal newBalance;

		private String message;

		public AccountUpdateResult(){

		}

		AccountUpdateResult(AccountStatus oldStatus, BigDecimal oldBalance, AccountStatus newStatus, BigDecimal newBalance, boolean updateStatus){			
			this.updateStatus.set(updateStatus);
			this.oldStatus = oldStatus;
			this.newStatus = newStatus;
			this.oldBalance = oldBalance;
			this.newBalance = newBalance;			
		}
		
		AccountUpdateResult(AccountStatus oldStatus, AccountStatus newStatus, boolean updateStatus){			
			this.updateStatus.set(updateStatus);
			this.oldStatus = oldStatus;
			this.newStatus = newStatus;
			this.oldBalance = this.newBalance = new BigDecimal(0);			
		}
		
		public AccountUpdateResult(boolean b) {
			this.updateStatus.set(b);
			this.newBalance = this.oldBalance = new BigDecimal(0);
			this.oldStatus = this.newStatus = AccountStatus.UNKNOWN;			
		}

		public BigDecimal getNewBalance() {
			return newBalance;
		}
		
		public AccountStatus getNewStatus() {
			return newStatus;
		}
		
		public BigDecimal getOldBalance() {
			return oldBalance;
		}
		
		public AccountStatus getOldStatus() {
			return oldStatus;
		}
		
		public boolean isUpdateSucceed() {
			return updateStatus.get();
		}


		private void fillSuccess(AccountState state, AccountState newState) {
			this.updateStatus.set(true);
			this.newBalance = newState.balance;
			this.oldBalance = state.balance;
			this.oldStatus = state.status;
			this.newStatus = newState.status;
			this.message = "success";
		}

		public void fillFailure(String message) {
			this.updateStatus.set(false);
			this.newBalance = this.oldBalance = new BigDecimal(0);
			this.oldStatus = this.newStatus = AccountStatus.UNKNOWN;
			this.message = message;
		}

		public String getMessage() {
			return message;
		}
	}
	
	private static final AccountUpdateResult FAILED = new AccountUpdateResult(false);
}

