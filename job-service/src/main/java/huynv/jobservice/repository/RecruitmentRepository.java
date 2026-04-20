package huynv.jobservice.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import huynv.jobservice.domain.Recruitment;

public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {

    Page<Recruitment> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Recruitment> findByCompanyId(Long companyId, Pageable pageable);

    Page<Recruitment> findByOrderByViewDescIdDesc(Pageable pageable);

    List<Recruitment> findByCompanyIdOrderByIdDesc(Long companyId);

    @Query("""
        select r from Recruitment r
        where lower(r.title) like lower(concat('%', :keyword, '%'))
           or lower(r.address) like lower(concat('%', :keyword, '%'))
           or lower(r.company.companyName) like lower(concat('%', :keyword, '%'))
        order by r.view desc, r.id desc
        """)
    Page<Recruitment> search(String keyword, Pageable pageable);

    /**
     * Combined filter without category scope.
     * Pass "" for any String param to skip that filter (avoids null-type PostgreSQL errors).
     */
    @Query("""
        select r from Recruitment r
        where (:keyword = '' or (
                   lower(r.title)               like lower(concat('%', :keyword, '%'))
                or lower(r.address)             like lower(concat('%', :keyword, '%'))
                or lower(r.company.companyName) like lower(concat('%', :keyword, '%'))))
          and (:address = '' or lower(r.address) like lower(concat('%', :address, '%')))
          and (:jobType = '' or lower(r.type)    =  lower(:jobType))
        order by r.view desc, r.id desc
        """)
    Page<Recruitment> findWithFilters(
        @Param("keyword") String keyword,
        @Param("address") String address,
        @Param("jobType") String jobType,
        Pageable pageable
    );

    /**
     * Combined filter with a required category scope.
     * Pass "" for any String param to skip that filter.
     */
    @Query("""
        select r from Recruitment r
        where r.category.id = :categoryId
          and (:keyword = '' or (
                   lower(r.title)               like lower(concat('%', :keyword, '%'))
                or lower(r.address)             like lower(concat('%', :keyword, '%'))
                or lower(r.company.companyName) like lower(concat('%', :keyword, '%'))))
          and (:address = '' or lower(r.address) like lower(concat('%', :address, '%')))
          and (:jobType = '' or lower(r.type)    =  lower(:jobType))
        order by r.view desc, r.id desc
        """)
    Page<Recruitment> findWithFiltersAndCategory(
        @Param("categoryId") Long   categoryId,
        @Param("keyword")    String keyword,
        @Param("address")    String address,
        @Param("jobType")    String jobType,
        Pageable pageable
    );
}
