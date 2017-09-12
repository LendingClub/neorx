package org.lendingclub.neorx;

import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public interface CypherStats {

	public static interface ExecutionStats {
		public long getCount();
		public String getCypher();
		public long getMaxTime();
		public long getMinTime();
		public long getMeanTime();
		public long getFirstTimestamp();
		public long getLastTimestamp();
		public long getRequestsPerMinute();
	}
	
	public void flush();
	public void subscribe(Consumer<Observable<ExecutionStats>> subscription);
	public Set<Consumer<Observable<ExecutionStats>>> getSubscribers();
}
