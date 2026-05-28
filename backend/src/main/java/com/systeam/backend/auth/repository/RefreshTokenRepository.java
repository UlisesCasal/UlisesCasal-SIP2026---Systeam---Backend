package com.systeam.backend.auth.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.systeam.backend.auth.model.RefreshToken;
import com.systeam.shared.model.Usuario;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenId(String tokenId);

    Optional<RefreshToken> findByTokenIdAndRevokedFalse(String tokenId);

    boolean existsByTokenIdAndRevokedFalse(String tokenId);

    void deleteByUser(Usuario user);

    void deleteByExpiryDateBefore(Instant now);
}
