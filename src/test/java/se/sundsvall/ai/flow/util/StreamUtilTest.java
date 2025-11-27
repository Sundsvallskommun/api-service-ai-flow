package se.sundsvall.ai.flow.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;

class StreamUtilTest {

	@Test
	void fromIterable() {
		Iterable<String> it = List.of("a", "b", "c");
		var result = StreamUtil.fromIterable(it).toList();
		assertThat(result).containsExactly("a", "b", "c");
	}

	@Test
	void fromIterator() {
		Iterator<Integer> iterator = Arrays.asList(1, 2, 3).iterator();
		var result = StreamUtil.fromIterator(iterator).map(i -> i * 2).toList();
		assertThat(result).containsExactly(2, 4, 6);
	}
}
