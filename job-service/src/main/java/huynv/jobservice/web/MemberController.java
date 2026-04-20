package huynv.jobservice.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import huynv.jobservice.domain.User;
import huynv.jobservice.service.AuthService;
import huynv.jobservice.service.MemberService;
import huynv.jobservice.web.dto.ApiMessageResponse;
import huynv.jobservice.web.dto.ApplyJobRequest;
import huynv.jobservice.web.dto.ApplyPostSummaryResponse;
import huynv.jobservice.web.dto.CompanySummaryResponse;
import huynv.jobservice.web.dto.ProfileResponse;
import huynv.jobservice.web.dto.RecruitmentSummaryResponse;
import huynv.jobservice.web.dto.RecruitmentUpsertRequest;
import huynv.jobservice.web.dto.ToggleStateResponse;
import huynv.jobservice.web.dto.UpdateCompanyRequest;
import huynv.jobservice.web.dto.UpdateUserRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    private final AuthService authService;
    private final MemberService memberService;

    public MemberController(AuthService authService, MemberService memberService) {
        this.authService = authService;
        this.memberService = memberService;
    }

    @GetMapping("/profile")
    public ProfileResponse profile(HttpSession session) {
        return memberService.getProfile(currentUser(session));
    }

    @PutMapping("/profile")
    public ProfileResponse updateProfile(@Validated @RequestBody UpdateUserRequest request, HttpSession session) {
        return memberService.updateUser(currentUser(session), request);
    }

    @PutMapping("/company")
    public CompanySummaryResponse updateCompany(@Validated @RequestBody UpdateCompanyRequest request, HttpSession session) {
        return memberService.updateCompany(currentUser(session), request);
    }

    @PostMapping("/profile/avatar")
    public ProfileResponse uploadAvatar(@RequestParam("file") MultipartFile file, HttpSession session) {
        return memberService.uploadAvatar(currentUser(session), file);
    }

    @PostMapping("/profile/cv")
    public ProfileResponse uploadCv(@RequestParam("file") MultipartFile file, HttpSession session) {
        return memberService.uploadCv(currentUser(session), file);
    }

    @PostMapping("/company/logo")
    public CompanySummaryResponse uploadLogo(@RequestParam("file") MultipartFile file, HttpSession session) {
        return memberService.uploadLogo(currentUser(session), file);
    }

    @PostMapping("/saved-jobs/{recruitmentId}")
    public ToggleStateResponse toggleSave(@PathVariable Long recruitmentId, HttpSession session) {
        return memberService.toggleSaveJob(currentUser(session), recruitmentId);
    }

    @PostMapping("/followed-companies/{companyId}")
    public ToggleStateResponse toggleFollow(@PathVariable Long companyId, HttpSession session) {
        return memberService.toggleFollowCompany(currentUser(session), companyId);
    }

    @PostMapping("/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiMessageResponse applyWithExistingCv(@Validated @RequestBody ApplyJobRequest request, HttpSession session) {
        return new ApiMessageResponse(
            memberService.applyWithExistingCv(currentUser(session), request.recruitmentId(), request.text()).message()
        );
    }

    @PostMapping("/applications/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiMessageResponse applyWithUploadedCv(
        @RequestParam("recruitmentId") Long recruitmentId,
        @RequestParam("text") String text,
        @RequestParam("file") MultipartFile file,
        HttpSession session
    ) {
        return new ApiMessageResponse(
            memberService.applyWithUploadedCv(currentUser(session), recruitmentId, text, file).message()
        );
    }

    @GetMapping("/saved-jobs")
    public List<RecruitmentSummaryResponse> savedJobs(HttpSession session) {
        return memberService.getSavedJobs(currentUser(session));
    }

    @GetMapping("/followed-companies")
    public List<CompanySummaryResponse> followedCompanies(HttpSession session) {
        return memberService.getFollowedCompanies(currentUser(session));
    }

    @GetMapping("/applications")
    public List<ApplyPostSummaryResponse> applications(HttpSession session) {
        return memberService.getMyApplications(currentUser(session));
    }

    @DeleteMapping("/applications/{applyId}")
    public ApiMessageResponse deleteApplication(@PathVariable Long applyId, HttpSession session) {
        memberService.deleteApplication(currentUser(session), applyId);
        return new ApiMessageResponse("Application deleted");
    }

    @GetMapping("/employer/recruitments")
    public List<RecruitmentSummaryResponse> employerRecruitments(HttpSession session) {
        return memberService.getEmployerRecruitments(currentUser(session));
    }

    @PostMapping("/employer/recruitments")
    @ResponseStatus(HttpStatus.CREATED)
    public RecruitmentSummaryResponse createRecruitment(
        @Validated @RequestBody RecruitmentUpsertRequest request,
        HttpSession session
    ) {
        return memberService.createRecruitment(currentUser(session), request);
    }

    @PutMapping("/employer/recruitments/{recruitmentId}")
    public RecruitmentSummaryResponse updateRecruitment(
        @PathVariable Long recruitmentId,
        @Validated @RequestBody RecruitmentUpsertRequest request,
        HttpSession session
    ) {
        return memberService.updateRecruitment(currentUser(session), recruitmentId, request);
    }

    @DeleteMapping("/employer/recruitments/{recruitmentId}")
    public ApiMessageResponse deleteRecruitment(@PathVariable Long recruitmentId, HttpSession session) {
        memberService.deleteRecruitment(currentUser(session), recruitmentId);
        return new ApiMessageResponse("Recruitment deleted");
    }

    @GetMapping("/employer/applicants")
    public List<ApplyPostSummaryResponse> employerApplicants(HttpSession session) {
        return memberService.getEmployerApplicants(currentUser(session));
    }

    @PostMapping("/employer/applicants/{applyId}/approve")
    public ApplyPostSummaryResponse approveApplicant(@PathVariable Long applyId, HttpSession session) {
        return memberService.approveApplicant(currentUser(session), applyId);
    }

    private User currentUser(HttpSession session) {
        return authService.requireCurrentUser(session);
    }
}
