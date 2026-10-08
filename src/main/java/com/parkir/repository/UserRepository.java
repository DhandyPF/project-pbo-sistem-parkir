package com.parkir.repository;

import com.parkir.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> findAll();
    Optional<User> findByUsername(String username);
    void save(User user);
    void delete(String username);
}
