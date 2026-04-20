package huynv.jobservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import huynv.jobservice.domain.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    @Query("select c from Company c left join c.recruitments r group by c order by count(r) desc, c.companyName asc")
    List<Company> findTopCompanies(Pageable pageable);

    Optional<Company> findByUserId(Long userId);

    @Query("""
        select c from Company c
        where lower(c.companyName) like lower(concat('%', :keyword, '%'))
           or lower(c.address) like lower(concat('%', :keyword, '%'))
        order by c.companyName asc
        """)
    List<Company> search(String keyword, Pageable pageable);
}
