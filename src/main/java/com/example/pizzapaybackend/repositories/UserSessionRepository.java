package com.example.pizzapaybackend.repositories;

import com.example.pizzapaybackend.entities.UserSessionEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserSessionRepository extends PagingAndSortingRepository<UserSessionEntity, UUID>, CrudRepository<UserSessionEntity, UUID>, JpaSpecificationExecutor<UserSessionEntity> {

}
