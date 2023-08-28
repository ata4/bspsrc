package info.ata4.bspsrc.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class Collectors {
	/**
	 * @see <a href=https://en.wikipedia.org/wiki/Mode_(statistics)>https://en.wikipedia.org/wiki/Mode_(statistics)</a>
	 */
	public static <T> Collector<T, ?, Optional<T>> mode() {
		return new Collector<T, Map<T, Integer>, Optional<T>>() {
			@Override
			public Supplier<Map<T, Integer>> supplier() {
				return HashMap::new;
			}

			@Override
			public BiConsumer<Map<T, Integer>, T> accumulator() {
				return (occurrences, val) -> occurrences.merge(val, 1, Integer::sum);
			}

			@Override
			public BinaryOperator<Map<T, Integer>> combiner() {
				return (m0, m1) -> {
					for (var e : m1.entrySet())
						m1.merge(e.getKey(), e.getValue(), Integer::sum);

					return m1;
				};
			}

			@Override
			public Function<Map<T, Integer>, Optional<T>> finisher() {
				return occurrences -> occurrences.entrySet().stream()
						.max(Map.Entry.comparingByValue())
						.map(Map.Entry::getKey);
			}

			@Override
			public Set<Characteristics> characteristics() {
				return Set.of(Characteristics.UNORDERED);
			}
		};
	}
}
