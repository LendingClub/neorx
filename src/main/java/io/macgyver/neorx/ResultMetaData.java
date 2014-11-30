package io.macgyver.neorx;

import java.util.List;

public interface ResultMetaData {
	
	public List<String> getFieldNames();
	public int getField(String input);
}
