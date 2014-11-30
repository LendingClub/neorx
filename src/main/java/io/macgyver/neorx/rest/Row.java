package io.macgyver.neorx.rest;

import com.fasterxml.jackson.databind.JsonNode;

public interface Row {
	ResultMetaData getMetaData();
	JsonNode getField(String s);
	String getString(String s);
	
}
