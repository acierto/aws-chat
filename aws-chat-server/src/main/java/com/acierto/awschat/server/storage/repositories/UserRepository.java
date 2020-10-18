package com.acierto.awschat.server.storage.repositories;

import com.acierto.awschat.server.storage.data.User;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

@EnableScan
public interface UserRepository extends CrudRepository<User, String> {
    Optional<User> findById(String id);

    User findByName(String name);

    User findBySessionToken(String sessionToken);

}
