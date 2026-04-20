package huynv.jobservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import huynv.jobservice.domain.FollowCompany;

public interface FollowCompanyRepository extends JpaRepository<FollowCompany, Long> {

    Optional<FollowCompany> findByUserIdAndCompanyId(Long userId, Long companyId);

    List<FollowCompany> findByUserIdOrderByIdDesc(Long userId);
}
