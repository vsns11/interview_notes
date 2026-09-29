package ch09.jdbc;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)     // not every repository should become a public API
public interface ProductRepository extends ListCrudRepository<Product, String> {}
