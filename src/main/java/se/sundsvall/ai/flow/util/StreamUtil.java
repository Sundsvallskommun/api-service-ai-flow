package se.sundsvall.ai.flow.util;

import java.util.Iterator;
import java.util.stream.Stream;

import static java.util.Spliterator.NONNULL;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

public final class StreamUtil {

	private StreamUtil() {}

	/**
	 * Creates a {@code Stream<T>} from the provided {@code Iterable<T>}.
	 *
	 * @param  iterable an iterable
	 * @return          a stream
	 * @param  <T>      the type of both the iterable and the resulting stream
	 */
	public static <T> Stream<T> fromIterable(final Iterable<T> iterable) {
		return stream(iterable.spliterator(), false);
	}

	/**
	 * Creates a {@code Stream<T>} from the provided {@code Iterator<T>}.
	 *
	 * @param  iterator an iterator
	 * @return          a stream
	 * @param  <T>      the type of both the iterator and the resulting stream
	 */
	public static <T> Stream<T> fromIterator(final Iterator<T> iterator) {
		return stream(spliteratorUnknownSize(iterator, NONNULL), false);
	}
}
