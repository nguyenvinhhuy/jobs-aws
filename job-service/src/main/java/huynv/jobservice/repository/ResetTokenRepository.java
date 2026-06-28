package huynv.jobservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import huynv.jobservice.domain.ResetToken;

public interface ResetTokenRepository extends JpaRepository<ResetToken, Long> {

    Optional<ResetToken> findByUserIdAndType(Long userId, String type);

    Optional<ResetToken> findByCodeAndType(String code, String type);

    void deleteByUserIdAndType(Long userId, String type);
}
