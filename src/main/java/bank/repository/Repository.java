package bank.repository;

import java.util.Collection;

public interface Repository<T> {

    T get(long id);
    Collection<T> getAll();
    T put(T object);
    void remove(T object);
    void update(T object);
}
