package com.acierto.awschat.server.storage.repositories;

import com.acierto.awschat.server.storage.data.Message;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

@EnableScan
public interface MessageRepository extends CrudRepository<Message, String> {
    Optional<Message> findById(String id);
}
