package ch12.tc;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Customer {
    @Id @GeneratedValue Long id;
    String email;

    protected Customer() { }
    public Customer(String email) { this.email = email; }
    public String getEmail() { return email; }
}
