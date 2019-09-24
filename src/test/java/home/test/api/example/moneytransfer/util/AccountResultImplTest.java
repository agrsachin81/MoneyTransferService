
package home.test.api.example.moneytransfer.util;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;

import home.test.api.example.moneytransfer.spi.enums.AccountStatus;
import home.test.api.example.moneytransfer.spi.enums.StatusResponse;
import home.test.api.example.moneytransfer.spi.interfaces.AccountResult;

/**
 * to test the jsonification of the result class should remain consistent with
 * the name of the getter method inside interface
 * Very important maintains the json interface consistent
 * @author sachin
 *
 *
 */
public class AccountResultImplTest {

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

	static Gson json = GsonHelper.createJsonSerializerDeserializer();

	/**
	 * if this test case fails that means that either matching is failed for name of the field
	 */
	@Test
	public void testJsonInterface() {
		AccountResultImpl impl = new AccountResultImpl("sampleRekId", 0.0, AccountStatus.CREATED, " name",
				" mobileNumber", StatusResponse.SUCCESS, "mnkj");

		String jsonStr = json.toJson(impl);

		Map<String, String> map = json.fromJson(jsonStr, HashMap.class);
		String missing = TransactionResultImplTest.findMissingJsonProps(map, AccountResult.class);
				
		assertEquals("Properties are missing from JSONified string is " + missing, 0, missing.length());
	}

}
