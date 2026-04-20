package huynv.jobservice.service;

import java.util.List;

import org.springframework.data.domain.Page;

import huynv.jobservice.domain.Category;
import huynv.jobservice.web.dto.CategorySummaryResponse;
import huynv.jobservice.web.dto.CompanyDetailResponse;
import huynv.jobservice.web.dto.CompanySummaryResponse;
import huynv.jobservice.web.dto.HomeOverviewResponse;
import huynv.jobservice.web.dto.RecruitmentDetailResponse;
import huynv.jobservice.web.dto.RecruitmentSummaryResponse;

public interface PublicQueryService {

    HomeOverviewResponse getHomeOverview();

    List<CategorySummaryResponse> getCategories();

    List<CompanySummaryResponse> getTopCompanies(int limit);

    List<CompanySummaryResponse> searchCompanies(String keyword, int limit);

    Page<RecruitmentSummaryResponse> getRecruitments(int page, int size, String keyword, Long categoryId, Long companyId, String address, String jobType);

    RecruitmentDetailResponse getRecruitmentDetail(Long id);

    CompanyDetailResponse getCompanyDetail(Long id);

    Category getCategoryEntity(Long id);
}
