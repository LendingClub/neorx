package org.lendingclub.neorx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class NeoRxException extends RuntimeException {


	static ObjectMapper mapper = new ObjectMapper();
	
	public NeoRxException(String message) {
		super(message);

	}
	public NeoRxException(String message, Exception e) {
		super(message,e);
	}
	public NeoRxException(Exception e) {
		super(e);
		
	}

}
