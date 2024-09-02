package com.example.pizzapaybackend.repositories;

import com.example.pizzapaybackend.entities.UserEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserRepository extends PagingAndSortingRepository<UserEntity, UUID>, CrudRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {

}
