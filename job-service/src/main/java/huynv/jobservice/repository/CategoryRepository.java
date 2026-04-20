package huynv.jobservice.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import huynv.jobservice.domain.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByOrderByNumberChooseDescNameAsc(Pageable pageable);
}
