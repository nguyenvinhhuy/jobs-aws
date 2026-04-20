package huynv.jobservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import huynv.jobservice.domain.ResetToken;

public interface ResetTokenRepository extends JpaRepository<ResetToken, Long> {

    Optional<ResetToken> findByUserId(Long userId);

    Optional<ResetToken> findByCode(String code);

    void deleteByUserId(Long userId);
}
