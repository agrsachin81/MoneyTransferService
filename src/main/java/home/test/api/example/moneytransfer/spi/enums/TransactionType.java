
package home.test.api.example.moneytransfer.spi.enums;

import java.util.EnumSet;

import home.test.api.example.moneytransfer.spi.utils.ValuedEnum;
import home.test.api.example.moneytransfer.spi.utils.ValuedEnumDeserializer;

public enum TransactionType implements ValuedEnum<TransactionType> {
	
	DEBIT_CASH("debit"),
	
	CREDIT_CASH("credit"),
	DEBIT_ACCOUNT("transferDebit"),
	CREDIT_ACCOUNT("transferCredit"),
	UNKNOWN("unknown");
	
	
	private final String stringPresentation;
	private TransactionType(String string) {
		this.stringPresentation = string;
	}
	
	public String toString(){
		return stringPresentation;
	}
	
	
	public static final EnumSet<TransactionType> cashTransactions = EnumSet.of(DEBIT_CASH, CREDIT_CASH);
	@Override
	public String getStringValue() {
		return stringPresentation;
	}
	
	public static ValuedEnumDeserializer<TransactionType> getTypeDeserializer(){
		return new ValuedEnumDeserializer<>(TransactionType.values());
	}

		
}