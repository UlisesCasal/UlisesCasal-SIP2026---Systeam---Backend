package com.systeam.backend.auth.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.systeam.backend.auth.model.RefreshToken;
import com.systeam.backend.UserAdministration.model.User;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenId(String tokenId);

    Optional<RefreshToken> findByTokenIdAndRevokedFalse(String tokenId);

    boolean existsByTokenIdAndRevokedFalse(String tokenId);

    void deleteByUser(User user);

    void deleteByExpiryDateBefore(Instant now);
}
