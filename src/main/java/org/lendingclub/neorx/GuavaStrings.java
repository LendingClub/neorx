package org.lendingclub.neorx;

/**
 * Copied from Guava
 *
 */
class GuavaStrings {

	
	  /**
	   * Returns {@code true} if the given string is null or is the empty string.
	   *
	   * <p>Consider normalizing your string references with nullToEmpty.
	   * If you do, you can use {@link String#isEmpty()} instead of this
	   * method, and you won't need special null-safe forms of methods like {@link
	   * String#toUpperCase} either. Or, if you'd like to normalize "in the other
	   * direction," converting empty strings to {@code null}, you can use emptyToNull.
	   *
	   * @param string a string reference to check
	   * @return {@code true} if the string is null or is the empty string
	   */
	  public static boolean isNullOrEmpty(String string) {
	    return string == null || string.length() == 0; // string.isEmpty() in Java 6
	  }

}
