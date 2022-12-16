package com.aceliq.frankfurt.database;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.aceliq.frankfurt.models.User;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

}
