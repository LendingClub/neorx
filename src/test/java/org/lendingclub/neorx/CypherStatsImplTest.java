package org.lendingclub.neorx;

import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.lendingclub.neorx.CypherStats.ExecutionStats;

public class CypherStatsImplTest {

	@Test
	public void testMinMax() {
		CypherStatsImpl c = new CypherStatsImpl();
		
		AtomicReference<ExecutionStats> ref = new AtomicReference<CypherStats.ExecutionStats>();
		c.subscribe(it->{
			System.out.println(it.blockingFirst());
			ref.set(it.blockingFirst());
		});
		c.recordCypher("foo", 200);
		c.recordCypher("foo", 100);
		
		c.flush();
		
		Assertions.assertThat(ref.get().getCount()).isEqualTo(2);
		Assertions.assertThat(ref.get().getMaxTime()).isEqualTo(200);
		Assertions.assertThat(ref.get().getMeanTime()).isEqualTo(150);
		Assertions.assertThat(ref.get().getMinTime()).isEqualTo(100);
		
	}
}
