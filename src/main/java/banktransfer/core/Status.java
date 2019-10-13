package banktransfer.core;

public interface Status<E, T> {
    static <E, T> Status<E, T> ok(T value) {
        return new Ok<>(value);
    }

    static <E, T> Status<E, T> error(E error) {
        return new Error<>(error);
    }

    static <T> Status<Failure, T> failure(String error) {
        return error(new Failure(error));
    }

    boolean succeeded();

    E error();

    T value();

    class Ok<E, T> implements Status<E, T> {

        private final T value;

        private Ok(T value) {
            this.value = value;
        }

        @Override
        public boolean succeeded() {
            return true;
        }

        @Override
        public E error() {
            throw new IllegalStateException();
        }

        @Override
        public T value() {
            return value;
        }
    }


    class Error<E, T> implements Status<E, T> {

        private final E value;

        private Error(E value) {
            this.value = value;
        }

        @Override
        public boolean succeeded() {
            return false;
        }

        @Override
        public E error() {
            return value;
        }

        @Override
        public T value() {
            throw new IllegalStateException();
        }
    }
}
