package org.lendingclub.neorx;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import io.reactivex.Observable;

public class RxTest {

	@Test
	public void teest() {

		AtomicInteger i = new AtomicInteger(0);
		Iterator<Integer> t = new Iterator<Integer>() {

			@Override
			public boolean hasNext() {
				// TODO Auto-generated method stub
				return i.get() < 10;
			}

			@Override
			public Integer next() {
				// TODO Auto-generated method stub
				System.out.println("next " + i);
				if (i.get() == 7) {
					throw new RuntimeException("problem!");
				}
				return i.getAndIncrement();
			}

		};

		Iterable<Integer> iterable = new Iterable<Integer>() {

			@Override
			public Iterator<Integer> iterator() {
				// TODO Auto-generated method stub
				return t;
			}
		};

		Observable.fromIterable(iterable).subscribe(it -> {
			System.out.println("subscriber " + it);
			if (it.equals(3)) {
				throw new RuntimeException("subscriber exception1");
			}
		}, error->{
			System.out.println("onError "+error);
		});

		Assertions.assertThat(i.get()).isEqualTo(4);

	}
}
