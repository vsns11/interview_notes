package ch09.jdbc;

import java.util.List;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/** Spring Data REST publishes this as /api/customers (GET, POST, PUT, PATCH, DELETE) plus a search endpoint. */
@RepositoryRestResource(path = "customers")
public interface CustomerRepository extends ListCrudRepository<Customer, Long> {

    List<Customer> findByName(String name);   // -> GET /api/customers/search/findByName?name=Ben
}
