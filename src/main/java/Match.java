import java.math.BigDecimal;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Match<T> {

    private final T value;

    private Match(T value) {
        this.value = value;
    }

    public interface Casable<T, V> {
        boolean test(T value);

        V apply(T value);

        default boolean isDefault() {
            return false;
        }
    }

    private static class PredicateCase<T, V> implements Casable<T, V> {
        private Predicate<T> predicate;
        private Function<T, V> function;

        private PredicateCase(Predicate<T> predicate, Function<T, V> function) {
            this.predicate = predicate;
            this.function = function;
        }

        @Override
        public boolean test(T value) {
            return this.predicate.test(value);
        }

        @Override
        public V apply(T value) {
            return function.apply(value);
        }
    }

    private static class Default<T, V> implements Casable<T, V> {
        private Supplier<V> defaultSupplier;

        private Default(Supplier<V> supplier) {
            this.defaultSupplier = supplier;
        }

        @Override
        public boolean test(Object value) {
            return false;
        }

        @Override
        public V apply(T value) {
            return defaultSupplier.get();
        }

        @Override
        public boolean isDefault() {
            return true;
        }
    }

    public static <V> Match<V> match(V value) {
        return new Match<>(value);
    }

    public static <V, T> Casable<V, T> is(Class<?> cl, Function<V, T> function) {
        return new PredicateCase<>(v -> v.getClass().equals(cl), function);
    }

    public static <V, T> Casable<V, T> Case(Predicate<V> pred, Function<V, T> function) {
        return new PredicateCase<>(pred, function);
    }

    public static <V, T> Casable<V, T> Default(Supplier<T> supplier) {
        return new Default<>(supplier);
    }

    public static <V> Predicate<V> negate(Predicate<V> pred) {
        return pred.negate();
    }

    public static <V> Predicate<V> $(V value) {
        return v -> v.equals(value);
    }

    public static Predicate<BigDecimal> $(BigDecimal value) {
        return v -> v.compareTo(value) == 0;
    }

    @SafeVarargs
    public final <V> V of(Casable<T, V>... cases) {
        Casable<T, V> defaultCase = null;
        for (Casable<T, V> testCase : cases) {
            if (testCase.isDefault()) {
                defaultCase = testCase;
                continue;
            }
            boolean result = testCase.test(value);
            if (result) {
                return testCase.apply(value);
            }
        }
        if (defaultCase != null) {
            return defaultCase.apply(null);
        }
        return null;
    }

}
