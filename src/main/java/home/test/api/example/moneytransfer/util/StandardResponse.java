package home.test.api.example.moneytransfer.util;

import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import home.test.api.example.moneytransfer.spi.enums.StatusResponse;

public class StandardResponse {
	private StatusResponse status;
	private String message;
	private JsonElement data;
	private Object retValues;
	private final int objId = objCounter.incrementAndGet();
	
	private static AtomicInteger objCounter = new AtomicInteger(0);
	
	public StandardResponse(StatusResponse status) {
		this.status = status;
	}
	
	public int getObjId() {
		return objId;
	}

	public StandardResponse(StatusResponse status, String message) {
		this.status = status;
		this.message = message;
	}

	public StandardResponse(StatusResponse status, JsonElement data) {
		this.status = status;
		this.data = data;
	}

	public StatusResponse getStatus() {
		return status;
	}

	public void setStatus(StatusResponse status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public JsonElement getData() {
		return data;
	}

	public void setData(JsonElement data) {
        this.data = data;
    }
    
	public StandardResponse setRetValues(Object retValues) {
		this.retValues = retValues;
		return this;
	}
	
	public Object getRetValues() {
		return retValues;
	}
	
	/**
	 * clears only data and message
	 */
	public StandardResponse clear() {
		this.data = null;
		this.message = null;
		this.retValues = null;
		return this;
	}
    
    public static final Gson GSON_INSTANCE =new Gson();
    
    public static final ThreadLocal<StandardResponse> SUCCESS_RESPONSE = new ThreadLocal<StandardResponse>() {
       public StandardResponse initialValue() {
    	   return new StandardResponse(StatusResponse.SUCCESS);
    	} 	
    };
    
    public static final ThreadLocal<StandardResponse> ERROR_RESPONSE = new ThreadLocal<StandardResponse>() {
    	public StandardResponse initialValue() {	
    		return new StandardResponse(StatusResponse.ERROR);
    	}
    };
    
	public static final String SUCESS_RESPONSE_JSON = GSON_INSTANCE.toJson(SUCCESS_RESPONSE);

	public static final String ERROR_RESPONSE_JSON = GSON_INSTANCE.toJson(SUCCESS_RESPONSE);
}