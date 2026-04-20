package huynv.jobservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import huynv.jobservice.domain.ApplyPost;

public interface ApplyPostRepository extends JpaRepository<ApplyPost, Long> {

    Optional<ApplyPost> findByUserIdAndRecruitmentId(Long userId, Long recruitmentId);

    List<ApplyPost> findByUserIdOrderByIdDesc(Long userId);

    List<ApplyPost> findByRecruitmentCompanyIdOrderByIdDesc(Long companyId);

    List<ApplyPost> findByRecruitmentIdOrderByIdDesc(Long recruitmentId);
}
