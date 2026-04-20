package huynv.jobservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import huynv.jobservice.domain.Cv;

public interface CvRepository extends JpaRepository<Cv, Long> {
}
