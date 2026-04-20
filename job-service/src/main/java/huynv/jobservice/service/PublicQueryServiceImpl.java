package huynv.jobservice.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import huynv.jobservice.domain.Category;
import huynv.jobservice.domain.Company;
import huynv.jobservice.domain.Recruitment;
import huynv.jobservice.repository.CategoryRepository;
import huynv.jobservice.repository.CompanyRepository;
import huynv.jobservice.repository.RecruitmentRepository;
import huynv.jobservice.repository.UserRepository;
import huynv.jobservice.web.error.NotFoundException;
import huynv.jobservice.web.dto.CategorySummaryResponse;
import huynv.jobservice.web.dto.CompanyDetailResponse;
import huynv.jobservice.web.dto.CompanySummaryResponse;
import huynv.jobservice.web.dto.HomeOverviewResponse;
import huynv.jobservice.web.dto.RecruitmentDetailResponse;
import huynv.jobservice.web.dto.RecruitmentSummaryResponse;

@Service
public class PublicQueryServiceImpl implements PublicQueryService {

    private final CategoryRepository categoryRepository;
    private final CompanyRepository companyRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final UserRepository userRepository;

    public PublicQueryServiceImpl(
        CategoryRepository categoryRepository,
        CompanyRepository companyRepository,
        RecruitmentRepository recruitmentRepository,
        UserRepository userRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.companyRepository = companyRepository;
        this.recruitmentRepository = recruitmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public HomeOverviewResponse getHomeOverview() {
        return new HomeOverviewResponse(
            companyRepository.count(),
            recruitmentRepository.count(),
            userRepository.countCandidates(),
            getCategories().stream().limit(4).toList(),
            getTopCompanies(4),
            getRecruitments(0, 6, null, null, null, null, null).getContent()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategorySummaryResponse> getCategories() {
        return categoryRepository.findByOrderByNumberChooseDescNameAsc(PageRequest.of(0, 20)).stream()
            .map(this::toCategorySummary)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanySummaryResponse> getTopCompanies(int limit) {
        return companyRepository.findTopCompanies(PageRequest.of(0, limit)).stream()
            .map(this::toCompanySummary)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanySummaryResponse> searchCompanies(String keyword, int limit) {
        if (keyword == null || keyword.isBlank()) {
            return getTopCompanies(limit);
        }

        return companyRepository.search(keyword.trim(), PageRequest.of(0, limit)).stream()
            .map(this::toCompanySummary)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecruitmentSummaryResponse> getRecruitments(int page, int size, String keyword, Long categoryId, Long companyId, String address, String jobType) {
        Pageable pageable = PageRequest.of(page, size);

        // Normalize to empty-string sentinel ("") so the JPQL `= ''` null-checks work
        // correctly with PostgreSQL (avoids the lower(bytea) type error from null params).
        String kw   = (keyword  != null && !keyword.isBlank())  ? keyword.trim()  : "";
        String addr = (address  != null && !address.isBlank())  ? address.trim()  : "";
        String jt   = (jobType  != null && !jobType.isBlank())  ? jobType.trim()  : "";

        Page<Recruitment> recruitments;

        // companyId is used exclusively on the company-detail page; take a fast path when
        // no other filter is active so the company-scoped query stays efficient.
        if (companyId != null && kw.isEmpty() && categoryId == null && addr.isEmpty() && jt.isEmpty()) {
            recruitments = recruitmentRepository.findByCompanyId(companyId, pageable);
        } else if (categoryId != null) {
            recruitments = recruitmentRepository.findWithFiltersAndCategory(categoryId, kw, addr, jt, pageable);
        } else {
            recruitments = recruitmentRepository.findWithFilters(kw, addr, jt, pageable);
        }

        List<RecruitmentSummaryResponse> content = recruitments.getContent().stream()
            .map(this::toRecruitmentSummary)
            .toList();

        return new PageImpl<>(content, pageable, recruitments.getTotalElements());
    }

    @Override
    @Transactional
    public RecruitmentDetailResponse getRecruitmentDetail(Long id) {
        Recruitment recruitment = recruitmentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("RECRUITMENT_NOT_FOUND", "Recruitment not found: " + id));
        recruitment.setView(recruitment.getView() == null ? 1 : recruitment.getView() + 1);
        Recruitment savedRecruitment = recruitmentRepository.save(recruitment);

        List<RecruitmentSummaryResponse> relatedRecruitments = recruitmentRepository
            .findByCategoryId(savedRecruitment.getCategory().getId(), PageRequest.of(0, 6))
            .stream()
            .filter(candidate -> !candidate.getId().equals(savedRecruitment.getId()))
            .map(this::toRecruitmentSummary)
            .limit(4)
            .toList();

        return new RecruitmentDetailResponse(
            savedRecruitment.getId(),
            savedRecruitment.getTitle(),
            savedRecruitment.getAddress(),
            savedRecruitment.getDescription(),
            savedRecruitment.getSalary(),
            savedRecruitment.getType(),
            savedRecruitment.getRank(),
            savedRecruitment.getExperience(),
            savedRecruitment.getQuantity(),
            savedRecruitment.getDeadline(),
            savedRecruitment.getView(),
            savedRecruitment.getCreatedAt(),
            savedRecruitment.getCompany().getId(),
            savedRecruitment.getCompany().getCompanyName(),
            savedRecruitment.getCompany().getDescription(),
            savedRecruitment.getCompany().getEmail(),
            savedRecruitment.getCompany().getPhoneNumber(),
            savedRecruitment.getCategory().getId(),
            savedRecruitment.getCategory().getName(),
            relatedRecruitments
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyDetailResponse getCompanyDetail(Long id) {
        Company company = companyRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("COMPANY_NOT_FOUND", "Company not found: " + id));

        List<RecruitmentSummaryResponse> recruitments = recruitmentRepository.findByCompanyIdOrderByIdDesc(company.getId()).stream()
            .map(this::toRecruitmentSummary)
            .toList();

        return new CompanyDetailResponse(
            company.getId(),
            company.getCompanyName(),
            company.getAddress(),
            company.getDescription(),
            company.getEmail(),
            company.getLogo(),
            company.getPhoneNumber(),
            recruitments.size(),
            recruitments
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryEntity(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("CATEGORY_NOT_FOUND", "Category not found: " + id));
    }

    private CategorySummaryResponse toCategorySummary(Category category) {
        return new CategorySummaryResponse(category.getId(), category.getName(), category.getNumberChoose());
    }

    private CompanySummaryResponse toCompanySummary(Company company) {
        return new CompanySummaryResponse(
            company.getId(),
            company.getCompanyName(),
            company.getAddress(),
            company.getDescription(),
            company.getEmail(),
            company.getLogo(),
            company.getPhoneNumber(),
            company.getRecruitments().size()
        );
    }

    private RecruitmentSummaryResponse toRecruitmentSummary(Recruitment recruitment) {
        return new RecruitmentSummaryResponse(
            recruitment.getId(),
            recruitment.getTitle(),
            recruitment.getAddress(),
            recruitment.getDescription(),
            recruitment.getSalary(),
            recruitment.getType(),
            recruitment.getRank(),
            recruitment.getExperience(),
            recruitment.getQuantity(),
            recruitment.getDeadline(),
            recruitment.getView(),
            recruitment.getCreatedAt(),
            recruitment.getCompany().getId(),
            recruitment.getCompany().getCompanyName(),
            recruitment.getCategory().getId(),
            recruitment.getCategory().getName()
        );
    }
}
