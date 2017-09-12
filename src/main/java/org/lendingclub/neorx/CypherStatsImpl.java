package org.lendingclub.neorx;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class CypherStatsImpl implements CypherStats {

	Logger logger = LoggerFactory.getLogger(CypherStatsImpl.class);

	Cache<String, ExecutionStats> cache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES)
			.maximumSize(1000).removalListener(new CacheRemovalListener()).build();

	Set<Consumer<Observable<ExecutionStats>>> consumers = Sets.newCopyOnWriteArraySet();

	protected CypherStatsImpl() {

	}

	public class ExecutionStatsImpl implements ExecutionStats {
		String cypher;
		AtomicLong firstTimestamp = new AtomicLong(System.currentTimeMillis());
		AtomicLong lastTimestamp = new AtomicLong(firstTimestamp.get());
		AtomicLong count = new AtomicLong();
		AtomicLong min = new AtomicLong(-1);
		AtomicLong max = new AtomicLong();
		AtomicLong mean = new AtomicLong();

		public String getCypher() {
			return cypher;
		}

		public long getCount() {
			return count.get();
		}

		public long getMinTime() {
			long val = min.get();
			return val >= 0 ? val : 0;
		}

		public long getMaxTime() {
			return max.get();
		}

		public long getMeanTime() {
			return mean.get();
		}

		public long getRequestsPerMinute() {
			double duration = Math.max(1, getLastTimestamp() - getFirstTimestamp()); // prevent
																						// div-by-zero
			if (duration < TimeUnit.SECONDS.toMillis(30)) {
				// if the sampling interval is too small, the rate can be
				// nonsensical, so we
				// just return the count. This will magnify the rate, but it
				// will magnify it far less

				return count.get();
			}

			return Math.round((count.get() / duration) * (60d * 1000d));
		}

		public long getFirstTimestamp() {
			return firstTimestamp.get();
		}

		public long getLastTimestamp() {
			return lastTimestamp.get();
		}

		public String toString() {
			return MoreObjects.toStringHelper(this).add("cypher", cypher.substring(0, Math.min(cypher.length(), 50)))
					.add("count", count.get()).add("meanTime", mean.get()).add("minTime", getMinTime())
					.add("maxTime", max.get()).add("rpm", getRequestsPerMinute()).toString();
		}
	}

	class CacheRemovalListener implements RemovalListener<String, ExecutionStats> {

		@Override
		public void onRemoval(RemovalNotification<String, ExecutionStats> notification) {
			consumers.forEach(it -> {
				try {
					it.accept(Observable.just(notification.getValue()));
				} catch (Exception e) {
					logger.info("problem", e);
				}
			});
		}

	}

	/**
	 * Register a consumer to receive stats info. The consumer is responsible
	 * for actually subscribing to the observable. We could expose a
	 * PublishSubject and emit an infinite stream. This would be OK, except that
	 * any uncaught exceptions in the consumer will blow up the Observable. This
	 * is much more reliable.
	 * 
	 * @param c
	 */
	public void subscribe(Consumer<Observable<ExecutionStats>> c) {

		consumers.add(c);
	}

	void recordCypher(String cypher, long time) {
		String hash = Hashing.md5().hashString(cypher, Charsets.UTF_8).toString();
		ExecutionStats info = cache.getIfPresent(hash);
		ExecutionStatsImpl x = (ExecutionStatsImpl) info;
		if (info == null) {
			x = new ExecutionStatsImpl();
			x.cypher = cypher;
			x.count.set(1);
			cache.put(hash, x);
			info = x;
		} else {
			ExecutionStatsImpl.class.cast(info).count.incrementAndGet();
		}

		if (x.min.get() < 0) {
			x.min.set(time);
		} else {
			x.min.set(Math.min(x.min.get(), time));
		}

		x.max.set(Math.max(x.max.get(), time));
		
		x.mean.set(((x.mean.get() * (x.count.get()-1)) + time) / (x.count.get()));
		
		x.lastTimestamp.set(System.currentTimeMillis());

	}

	public void flush() {
		cache.invalidateAll();
	}

	@Override
	public Set<Consumer<Observable<ExecutionStats>>> getSubscribers() {
		return consumers;
	}

}
