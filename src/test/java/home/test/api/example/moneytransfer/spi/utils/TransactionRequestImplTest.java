
package home.test.api.example.moneytransfer.spi.utils;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;

import home.test.api.example.moneytransfer.spi.interfaces.TransactionRequest;
import home.test.api.example.moneytransfer.util.GsonHelper;

public class TransactionRequestImplTest {

	static Gson json = GsonHelper.createJsonSerializerDeserializer();
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testJsonSerializationNotNullTransfer() {

		String jsonStr = "{amount:" + 900.0
		+ ",cpAccountId:" + "someAccid" + ",transactionRequestId:trasnRekId_9 }";

		TransactionRequestImpl objFromJson = json.fromJson(jsonStr, TransactionRequestImpl.class);
		
		String missing = findNullObjectProps(objFromJson, TransactionRequest.class);

		assertEquals("Null props are  " + missing, 0, missing.length());
	}
	
	@Test
	public void testJsonSerializationNotNullCash() {

		String jsonStr = "{amount:" + 900.0
		+ ",transactionRequestId:trasnRekId_9,transactionType:debit}";

		TransactionRequestImpl objFromJson = json.fromJson(jsonStr, TransactionRequestImpl.class);
		
		String missing = findNullObjectProps(objFromJson, TransactionRequest.class);

		assertEquals("Null props are  " + missing, 0, missing.length());
	}
	
	public static String findNullObjectProps(TransactionRequestImpl obj, Class<?> class1) {
		String missing= "";
		for (Method method : class1.getMethods()) {
			String name = method.getName();
			String jsonName = null;
			if (name.startsWith("get")) {
				jsonName = name.substring(3);
			} else if (name.startsWith("is")) {
				jsonName = name.substring(2);
			}
			
			Object retObject =null;
			try {
				retObject = method.invoke(obj, null);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}

			if (retObject == null) {
					missing += jsonName + ", ";
			}
		}
		return missing;
	}

}
