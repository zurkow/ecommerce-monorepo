package com.ecommerce.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Seeds the in-memory database so the admin panel has something to show. */
@Component
class DemoData implements CommandLineRunner {

    private final UserRepository users;

    DemoData(UserRepository users) {
        this.users = users;
    }

    @Override
    public void run(String... args) {
        users.save(new User("admin@shop.local", "Ada", "Admin", Role.ADMIN));
        users.save(new User("jan@shop.local", "Jan", "Kowalski", Role.CUSTOMER));
        users.save(new User("anna@shop.local", "Anna", "Nowak", Role.CUSTOMER));
    }
}
