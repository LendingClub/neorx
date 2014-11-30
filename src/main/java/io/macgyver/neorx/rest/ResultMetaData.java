package io.macgyver.neorx.rest;

import java.util.List;

public interface ResultMetaData {
	
	public List<String> getFieldNames();
	public int getField(String input);
}
