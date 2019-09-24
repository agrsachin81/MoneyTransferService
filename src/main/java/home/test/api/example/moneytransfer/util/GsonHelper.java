
package home.test.api.example.moneytransfer.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import home.test.api.example.moneytransfer.spi.enums.TransactionType;
import home.test.api.example.moneytransfer.spi.utils.OptionalTypeAdapter;

public class GsonHelper {
	
	public static Gson createJsonSerializerDeserializer(){
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.registerTypeAdapter(TransactionType.class, TransactionType.getTypeDeserializer())
				.registerTypeAdapterFactory(OptionalTypeAdapter.FACTORY).create();
		return gson;
	}
}
