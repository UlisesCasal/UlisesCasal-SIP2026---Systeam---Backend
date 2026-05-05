package com.systeam.backend.UserAdministration.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.systeam.backend.UserAdministration.model.User;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>{
    Optional<RefreshToken> findByTokenId(String tokenId);

    Optional<RefreshToken> findByTokenIdAndRevokedFalse(String tokneId);

    Boolean existsByTokenIdAndRevokedFalse(String tokneId);

    void deleteByUser(User user);

    void deleteByExpiryDateBefore(Instant now);
}
