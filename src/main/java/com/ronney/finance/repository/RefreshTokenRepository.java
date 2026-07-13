package com.ronney.finance.repository;

import com.ronney.finance.domain.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    @Query("""
        SELECT rt
        FROM RefreshToken rt
        JOIN FETCH rt.user
        WHERE rt.token = :token
        """)
    Optional<RefreshToken> findByTokenWithUser(
            @Param("token") String token
    );

    List<RefreshToken> findByUserEmail(String email);

}