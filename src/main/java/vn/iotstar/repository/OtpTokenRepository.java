package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.OtpToken;

import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findTopByEmailAndTokenTypeAndIsUsedFalseOrderByCreatedAtDesc(
        String email, OtpToken.TokenType tokenType
    );
}
