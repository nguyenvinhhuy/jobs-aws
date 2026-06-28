package huynv.jobservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import huynv.jobservice.domain.SaveJob;

public interface SaveJobRepository extends JpaRepository<SaveJob, Long> {

    Optional<SaveJob> findByUserIdAndRecruitmentId(Long userId, Long recruitmentId);

    @Query("SELECT sj FROM SaveJob sj JOIN FETCH sj.recruitment WHERE sj.user.id = :userId ORDER BY sj.id DESC")
    List<SaveJob> findByUserIdWithRecruitmentOrderByIdDesc(@Param("userId") Long userId);

    List<SaveJob> findByUserIdOrderByIdDesc(Long userId);

    List<SaveJob> findByRecruitmentId(Long recruitmentId);
}
