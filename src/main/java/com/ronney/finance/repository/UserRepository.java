package com.ronney.finance.repository;

import com.ronney.finance.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    @Query("""
        SELECT u
        FROM User u
        JOIN FETCH u.household
        WHERE u.email = :email
        """)
    Optional<User> findByEmailWithHousehold(
            @Param("email") String email
    );

    boolean existsByEmail(String email);
}
