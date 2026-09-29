package lld.ch11.di;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A toy IoC container: register how to build each type once; ask for the type later.
 * Real containers (Spring, Guice, Dagger) add scanning, scopes, proxies and lifecycles.
 */
public final class Container {
    private final Map<Class<?>, Function<Container, ?>> recipes = new HashMap<>();
    private final Map<Class<?>, Object> singletons = new HashMap<>();

    public <T> void register(Class<T> type, Function<Container, ? extends T> recipe) {
        recipes.put(type, recipe);
    }

    public <T> T get(Class<T> type) {
        Object existing = singletons.get(type);
        if (existing != null) return type.cast(existing);
        Function<Container, ?> recipe = recipes.get(type);
        if (recipe == null) throw new IllegalStateException("no binding for " + type.getSimpleName());
        T created = type.cast(recipe.apply(this));   // the recipe may ask the container for its own deps
        singletons.put(type, created);
        return created;
    }
}
