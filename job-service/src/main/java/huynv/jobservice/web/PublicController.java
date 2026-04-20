package huynv.jobservice.web;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import huynv.jobservice.service.PublicQueryService;
import huynv.jobservice.web.dto.CategorySummaryResponse;
import huynv.jobservice.web.dto.CompanyDetailResponse;
import huynv.jobservice.web.dto.CompanySummaryResponse;
import huynv.jobservice.web.dto.HomeOverviewResponse;
import huynv.jobservice.web.dto.PageResponse;
import huynv.jobservice.web.dto.RecruitmentDetailResponse;
import huynv.jobservice.web.dto.RecruitmentSummaryResponse;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final PublicQueryService publicQueryService;

    public PublicController(PublicQueryService publicQueryService) {
        this.publicQueryService = publicQueryService;
    }

    @GetMapping("/home")
    public HomeOverviewResponse getHomeOverview() {
        return publicQueryService.getHomeOverview();
    }

    @GetMapping("/categories")
    public List<CategorySummaryResponse> getCategories() {
        return publicQueryService.getCategories();
    }

    @GetMapping("/companies/top")
    public List<CompanySummaryResponse> getTopCompanies(@RequestParam(defaultValue = "4") int limit) {
        return publicQueryService.getTopCompanies(limit);
    }

    @GetMapping("/companies")
    public List<CompanySummaryResponse> searchCompanies(
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "12") int limit
    ) {
        return publicQueryService.searchCompanies(keyword, limit);
    }

    @GetMapping("/companies/{id}")
    public CompanyDetailResponse getCompanyDetail(@PathVariable Long id) {
        return publicQueryService.getCompanyDetail(id);
    }

    @GetMapping("/recruitments")
    public PageResponse<RecruitmentSummaryResponse> getRecruitments(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "6") int size,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) Long companyId,
        @RequestParam(required = false) String address,
        @RequestParam(required = false) String jobType
    ) {
        Page<RecruitmentSummaryResponse> recruitmentPage =
            publicQueryService.getRecruitments(page, size, keyword, categoryId, companyId, address, jobType);

        return new PageResponse<>(
            recruitmentPage.getContent(),
            recruitmentPage.getNumber(),
            recruitmentPage.getSize(),
            recruitmentPage.getTotalElements(),
            recruitmentPage.getTotalPages()
        );
    }

    @GetMapping("/recruitments/{id}")
    public RecruitmentDetailResponse getRecruitmentDetail(@PathVariable Long id) {
        return publicQueryService.getRecruitmentDetail(id);
    }
}
