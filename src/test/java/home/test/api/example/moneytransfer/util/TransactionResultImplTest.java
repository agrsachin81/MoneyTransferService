
package home.test.api.example.moneytransfer.util;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;

import home.test.api.example.moneytransfer.spi.enums.StatusResponse;
import home.test.api.example.moneytransfer.spi.enums.TransactionStatus;
import home.test.api.example.moneytransfer.spi.interfaces.TransactionResult;

/**
 * to test the jsonification of the result class should remain consistent with
 * the name of the getter method inside interface
 *  Very important maintains the json interface consistent
 * @author sachin
 *
 *
 */
public class TransactionResultImplTest {

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
	public void testJsonInterface() {

		TransactionResultImpl impl = new TransactionResultImpl("sampleRekId", TransactionStatus.DONE, "transRefeId","accountId",
				900.0, StatusResponse.SUCCESS, Optional.of("cpAcc"), true, false,
				Optional.of("cbjbj"), 89809087l, "message");

		String jsonStr = json.toJson(impl);

		Map<String,String> map = json.fromJson(jsonStr, HashMap.class);
		
		String missing = findMissingJsonProps(map, TransactionResult.class);

		assertEquals("Missing props are  " + missing, 0, missing.length());
	}

	public static String findMissingJsonProps(Map<String,String> map, Class<?> class1) {
		String missing= "";
		for (Method method : class1.getMethods()) {
			String name = method.getName();
			String jsonName = null;
			if (name.startsWith("get")) {
				jsonName = name.substring(3);

			} else if (name.startsWith("is")) {
				jsonName = name.substring(2);
			}

			if (jsonName != null) {
				char charAtZero = jsonName.charAt(0);
				String remianingString = jsonName.substring(1);
				jsonName = Character.toLowerCase(charAtZero) + remianingString;
				if (!map.containsKey(jsonName)) {
					missing += jsonName + ", ";
				}
			}
		}
		return missing;
	}
	
	

}
