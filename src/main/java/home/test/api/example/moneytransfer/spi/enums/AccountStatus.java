 
package home.test.api.example.moneytransfer.spi.enums;

import home.test.api.example.moneytransfer.spi.utils.ValuedEnum;
import home.test.api.example.moneytransfer.spi.utils.ValuedEnumDeserializer;

public enum AccountStatus implements ValuedEnum<AccountStatus>{
	
	CREATED,
	ACTIVE,
	SUSPENDED,
	DELETED,
	UNKNOWN;
	
	public static ValuedEnumDeserializer<AccountStatus> getTypeDeserializer(){
		return new ValuedEnumDeserializer<>(AccountStatus.values());
	}

	@Override
	public String getStringValue() {
		return this.name();
	}
	
	public static boolean isDebitable(AccountStatus status){
		return status == ACTIVE || status == CREATED;
	}
	
	public static boolean isCreditable(AccountStatus status){
		return status != DELETED ;
	}
}
