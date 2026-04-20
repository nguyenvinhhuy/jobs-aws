package huynv.jobservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import huynv.jobservice.domain.SaveJob;

public interface SaveJobRepository extends JpaRepository<SaveJob, Long> {

    Optional<SaveJob> findByUserIdAndRecruitmentId(Long userId, Long recruitmentId);

    List<SaveJob> findByUserIdOrderByIdDesc(Long userId);

    List<SaveJob> findByRecruitmentId(Long recruitmentId);
}
