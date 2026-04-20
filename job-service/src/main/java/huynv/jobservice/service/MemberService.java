package huynv.jobservice.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import huynv.jobservice.domain.ApplyPost;
import huynv.jobservice.domain.Company;
import huynv.jobservice.domain.Cv;
import huynv.jobservice.domain.FollowCompany;
import huynv.jobservice.domain.Recruitment;
import huynv.jobservice.domain.SaveJob;
import huynv.jobservice.domain.User;
import huynv.jobservice.repository.ApplyPostRepository;
import huynv.jobservice.repository.CompanyRepository;
import huynv.jobservice.repository.CvRepository;
import huynv.jobservice.repository.FollowCompanyRepository;
import huynv.jobservice.repository.RecruitmentRepository;
import huynv.jobservice.repository.SaveJobRepository;
import huynv.jobservice.repository.UserRepository;
import huynv.jobservice.web.dto.ApplyPostSummaryResponse;
import huynv.jobservice.web.dto.CompanySummaryResponse;
import huynv.jobservice.web.dto.ProfileResponse;
import huynv.jobservice.web.dto.RecruitmentSummaryResponse;
import huynv.jobservice.web.dto.RecruitmentUpsertRequest;
import huynv.jobservice.web.dto.ToggleStateResponse;
import huynv.jobservice.web.dto.UpdateCompanyRequest;
import huynv.jobservice.web.dto.UpdateUserRequest;
import huynv.jobservice.web.error.BadRequestException;
import huynv.jobservice.web.error.ConflictException;
import huynv.jobservice.web.error.ForbiddenException;
import huynv.jobservice.web.error.NotFoundException;

@Service
@Transactional
public class MemberService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CvRepository cvRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final SaveJobRepository saveJobRepository;
    private final FollowCompanyRepository followCompanyRepository;
    private final ApplyPostRepository applyPostRepository;
    private final PublicQueryService publicQueryService;
    private final Path uploadRoot;

    public MemberService(
        UserRepository userRepository,
        CompanyRepository companyRepository,
        CvRepository cvRepository,
        RecruitmentRepository recruitmentRepository,
        SaveJobRepository saveJobRepository,
        FollowCompanyRepository followCompanyRepository,
        ApplyPostRepository applyPostRepository,
        PublicQueryService publicQueryService,
        @Value("${app.storage.upload-dir:uploads}") String uploadDir
    ) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.cvRepository = cvRepository;
        this.recruitmentRepository = recruitmentRepository;
        this.saveJobRepository = saveJobRepository;
        this.followCompanyRepository = followCompanyRepository;
        this.applyPostRepository = applyPostRepository;
        this.publicQueryService = publicQueryService;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public ProfileResponse getProfile(User user) {
        user = requireManagedUser(user);
        Company company = companyRepository.findByUserId(user.getId()).orElse(null);
        return new ProfileResponse(
            user.getId(),
            user.getFullName(),
            user.getAddress(),
            user.getEmail(),
            user.getDescription(),
            user.getImage(),
            user.getPhoneNumber(),
            user.getStatus(),
            user.getRole().getRoleName(),
            user.getCv() != null ? user.getCv().getFileName() : null,
            company == null ? null : new CompanySummaryResponse(
                company.getId(),
                company.getCompanyName(),
                company.getAddress(),
                company.getDescription(),
                company.getEmail(),
                company.getLogo(),
                company.getPhoneNumber(),
                company.getRecruitments().size()
            )
        );
    }

    public ProfileResponse updateUser(User user, UpdateUserRequest request) {
        user = requireManagedUser(user);
        user.setFullName(request.fullName());
        user.setAddress(request.address());
        user.setEmail(request.email());
        user.setDescription(request.description());
        user.setPhoneNumber(request.phoneNumber());
        return getProfile(userRepository.save(user));
    }

    public CompanySummaryResponse updateCompany(User user, UpdateCompanyRequest request) {
        user = requireManagedUser(user);
        Company company = requireEmployerCompany(user);
        company.setCompanyName(request.companyName());
        company.setAddress(request.address());
        company.setEmail(request.email());
        company.setDescription(request.description());
        company.setPhoneNumber(request.phoneNumber());
        Company saved = companyRepository.save(company);
        return new CompanySummaryResponse(
            saved.getId(),
            saved.getCompanyName(),
            saved.getAddress(),
            saved.getDescription(),
            saved.getEmail(),
            saved.getLogo(),
            saved.getPhoneNumber(),
            saved.getRecruitments().size()
        );
    }

    public ProfileResponse uploadAvatar(User user, MultipartFile file) {
        user = requireManagedUser(user);
        user.setImage(store(file, "avatars"));
        return getProfile(userRepository.save(user));
    }

    public ProfileResponse uploadCv(User user, MultipartFile file) {
        user = requireManagedUser(user);
        Cv cv = user.getCv();
        if (cv == null) {
            cv = cvRepository.save(new Cv());
        }
        cv.setFileName(store(file, "cvs"));
        cv = cvRepository.save(cv);
        user.setCv(cv);
        return getProfile(userRepository.save(user));
    }

    public CompanySummaryResponse uploadLogo(User user, MultipartFile file) {
        user = requireManagedUser(user);
        Company company = requireEmployerCompany(user);
        company.setLogo(store(file, "logos"));
        Company saved = companyRepository.save(company);
        return new CompanySummaryResponse(
            saved.getId(),
            saved.getCompanyName(),
            saved.getAddress(),
            saved.getDescription(),
            saved.getEmail(),
            saved.getLogo(),
            saved.getPhoneNumber(),
            saved.getRecruitments().size()
        );
    }

    public ToggleStateResponse toggleSaveJob(User user, Long recruitmentId) {
        User managedUser = requireManagedUser(user);
        recruitmentRepository.findById(recruitmentId)
            .orElseThrow(() -> new NotFoundException("RECRUITMENT_NOT_FOUND", "Recruitment not found"));
        return saveJobRepository.findByUserIdAndRecruitmentId(managedUser.getId(), recruitmentId)
            .map(existing -> {
                saveJobRepository.delete(existing);
                return new ToggleStateResponse(false, "Job removed from saved list");
            })
            .orElseGet(() -> {
                SaveJob saveJob = new SaveJob();
                saveJob.setUser(managedUser);
                saveJob.setRecruitment(recruitmentRepository.findById(recruitmentId).orElseThrow());
                saveJobRepository.save(saveJob);
                return new ToggleStateResponse(true, "Job saved successfully");
            });
    }

    public ToggleStateResponse toggleFollowCompany(User user, Long companyId) {
        User managedUser = requireManagedUser(user);
        companyRepository.findById(companyId)
            .orElseThrow(() -> new NotFoundException("COMPANY_NOT_FOUND", "Company not found"));
        return followCompanyRepository.findByUserIdAndCompanyId(managedUser.getId(), companyId)
            .map(existing -> {
                followCompanyRepository.delete(existing);
                return new ToggleStateResponse(false, "Company unfollowed");
            })
            .orElseGet(() -> {
                FollowCompany followCompany = new FollowCompany();
                followCompany.setUser(managedUser);
                followCompany.setCompany(companyRepository.findById(companyId).orElseThrow());
                followCompanyRepository.save(followCompany);
                return new ToggleStateResponse(true, "Company followed successfully");
            });
    }

    public ApiApplyResult applyWithExistingCv(User user, Long recruitmentId, String text) {
        user = requireManagedUser(user);
        if (user.getCv() == null || user.getCv().getFileName() == null || user.getCv().getFileName().isBlank()) {
            throw new BadRequestException("CV_REQUIRED", "You must upload CV first");
        }
        return createApply(user, recruitmentId, text, user.getCv().getFileName());
    }

    public ApiApplyResult applyWithUploadedCv(User user, Long recruitmentId, String text, MultipartFile file) {
        user = requireManagedUser(user);
        String cvFileName = store(file, "cvs");
        Cv cv = user.getCv();
        if (cv == null) {
            cv = cvRepository.save(new Cv());
        }
        cv.setFileName(cvFileName);
        cv = cvRepository.save(cv);
        user.setCv(cv);
        userRepository.save(user);
        return createApply(user, recruitmentId, text, cvFileName);
    }

    public List<RecruitmentSummaryResponse> getSavedJobs(User user) {
        user = requireManagedUser(user);
        return saveJobRepository.findByUserIdOrderByIdDesc(user.getId()).stream()
            .map(saveJob -> recruitmentRepository.findById(saveJob.getRecruitment().getId()))
            .filter(java.util.Optional::isPresent)
            .map(java.util.Optional::get)
            .map(this::toRecruitmentSummary)
            .toList();
    }

    public List<CompanySummaryResponse> getFollowedCompanies(User user) {
        user = requireManagedUser(user);
        return followCompanyRepository.findByUserIdOrderByIdDesc(user.getId()).stream()
            .map(FollowCompany::getCompany)
            .map(company -> new CompanySummaryResponse(
                company.getId(),
                company.getCompanyName(),
                company.getAddress(),
                company.getDescription(),
                company.getEmail(),
                company.getLogo(),
                company.getPhoneNumber(),
                company.getRecruitments().size()
            ))
            .toList();
    }

    public List<ApplyPostSummaryResponse> getMyApplications(User user) {
        user = requireManagedUser(user);
        return applyPostRepository.findByUserIdOrderByIdDesc(user.getId()).stream()
            .map(this::toApplySummary)
            .toList();
    }

    public List<RecruitmentSummaryResponse> getEmployerRecruitments(User user) {
        user = requireManagedUser(user);
        Company company = requireEmployerCompany(user);
        return recruitmentRepository.findByCompanyIdOrderByIdDesc(company.getId()).stream()
            .map(this::toRecruitmentSummary)
            .toList();
    }

    public RecruitmentSummaryResponse createRecruitment(User user, RecruitmentUpsertRequest request) {
        user = requireManagedUser(user);
        Company company = requireEmployerCompany(user);
        Recruitment recruitment = new Recruitment();
        fillRecruitment(recruitment, request, company);
        return toRecruitmentSummary(recruitmentRepository.save(recruitment));
    }

    public RecruitmentSummaryResponse updateRecruitment(User user, Long recruitmentId, RecruitmentUpsertRequest request) {
        user = requireManagedUser(user);
        Company company = requireEmployerCompany(user);
        Recruitment recruitment = recruitmentRepository.findById(recruitmentId)
            .orElseThrow(() -> new NotFoundException("RECRUITMENT_NOT_FOUND", "Recruitment not found"));
        if (!recruitment.getCompany().getId().equals(company.getId())) {
            throw new ForbiddenException("RECRUITMENT_EDIT_FORBIDDEN", "You cannot edit this recruitment");
        }
        fillRecruitment(recruitment, request, company);
        return toRecruitmentSummary(recruitmentRepository.save(recruitment));
    }

    public void deleteRecruitment(User user, Long recruitmentId) {
        user = requireManagedUser(user);
        Company company = requireEmployerCompany(user);
        Recruitment recruitment = recruitmentRepository.findById(recruitmentId)
            .orElseThrow(() -> new NotFoundException("RECRUITMENT_NOT_FOUND", "Recruitment not found"));
        if (!recruitment.getCompany().getId().equals(company.getId())) {
            throw new ForbiddenException("RECRUITMENT_DELETE_FORBIDDEN", "You cannot delete this recruitment");
        }
        applyPostRepository.findByRecruitmentIdOrderByIdDesc(recruitmentId).forEach(applyPostRepository::delete);
        saveJobRepository.findByRecruitmentId(recruitmentId).forEach(saveJobRepository::delete);
        recruitmentRepository.delete(recruitment);
    }

    public void deleteApplication(User user, Long applyId) {
        user = requireManagedUser(user);
        ApplyPost applyPost = applyPostRepository.findById(applyId)
            .orElseThrow(() -> new NotFoundException("APPLICATION_NOT_FOUND", "Application not found"));
        if (!applyPost.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("APPLICATION_DELETE_FORBIDDEN", "You cannot delete this application");
        }
        applyPostRepository.delete(applyPost);
    }

    public List<ApplyPostSummaryResponse> getEmployerApplicants(User user) {
        user = requireManagedUser(user);
        Company company = requireEmployerCompany(user);
        return applyPostRepository.findByRecruitmentCompanyIdOrderByIdDesc(company.getId()).stream()
            .map(this::toApplySummary)
            .toList();
    }

    public ApplyPostSummaryResponse approveApplicant(User user, Long applyId) {
        user = requireManagedUser(user);
        Company company = requireEmployerCompany(user);
        ApplyPost applyPost = applyPostRepository.findById(applyId)
            .orElseThrow(() -> new NotFoundException("APPLICATION_NOT_FOUND", "Application not found"));
        if (!applyPost.getRecruitment().getCompany().getId().equals(company.getId())) {
            throw new ForbiddenException("APPLICATION_APPROVE_FORBIDDEN", "You cannot approve this application");
        }
        applyPost.setStatus(1);
        return toApplySummary(applyPostRepository.save(applyPost));
    }

    private ApiApplyResult createApply(User user, Long recruitmentId, String text, String cvFileName) {
        Recruitment recruitment = recruitmentRepository.findById(recruitmentId)
            .orElseThrow(() -> new NotFoundException("RECRUITMENT_NOT_FOUND", "Recruitment not found"));
        applyPostRepository.findByUserIdAndRecruitmentId(user.getId(), recruitmentId).ifPresent(existing -> {
            throw new ConflictException("APPLICATION_ALREADY_EXISTS", "You have already applied to this job");
        });
        ApplyPost applyPost = new ApplyPost();
        applyPost.setUser(user);
        applyPost.setRecruitment(recruitment);
        applyPost.setNameCv(cvFileName);
        applyPost.setText(text);
        applyPost.setStatus(0);
        applyPost.setCreatedAt(LocalDate.now().toString());
        ApplyPost saved = applyPostRepository.save(applyPost);
        return new ApiApplyResult(saved.getId(), "Applied successfully");
    }

    private void fillRecruitment(Recruitment recruitment, RecruitmentUpsertRequest request, Company company) {
        recruitment.setTitle(request.title());
        recruitment.setAddress(request.address());
        recruitment.setDescription(request.description());
        recruitment.setExperience(request.experience());
        recruitment.setQuantity(request.quantity());
        recruitment.setRank(request.rank());
        recruitment.setSalary(request.salary());
        recruitment.setType(request.type());
        recruitment.setDeadline(request.deadline());
        recruitment.setStatus(1);
        recruitment.setView(recruitment.getView() == null ? 0 : recruitment.getView());
        recruitment.setCreatedAt(recruitment.getCreatedAt() == null ? LocalDate.now().toString() : recruitment.getCreatedAt());
        recruitment.setCompany(company);
        recruitment.setCategory(publicQueryService.getCategoryEntity(request.categoryId()));
    }

    private Company requireEmployerCompany(User user) {
        if (!"EMPLOYER".equals(user.getRole().getRoleName())) {
            throw new ForbiddenException("EMPLOYER_ROLE_REQUIRED", "Employer account required");
        }
        return companyRepository.findByUserId(user.getId())
            .orElseThrow(() -> new NotFoundException("COMPANY_PROFILE_NOT_FOUND", "Company profile not found"));
    }

    private User requireManagedUser(User user) {
        return userRepository.findById(user.getId())
            .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found"));
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

    private ApplyPostSummaryResponse toApplySummary(ApplyPost applyPost) {
        return new ApplyPostSummaryResponse(
            applyPost.getId(),
            applyPost.getUser().getFullName(),
            applyPost.getUser().getEmail(),
            applyPost.getNameCv(),
            applyPost.getText(),
            applyPost.getStatus(),
            applyPost.getCreatedAt(),
            applyPost.getRecruitment().getId(),
            applyPost.getRecruitment().getTitle()
        );
    }

    private String store(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("FILE_REQUIRED", "File is required");
        }

        try {
            Path directory = uploadRoot.resolve(folder);
            Files.createDirectories(directory);
            String originalFileName = Path.of(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename())
                .getFileName()
                .toString()
                .replace(" ", "-");
            String fileName = UUID.randomUUID() + "-" + originalFileName;
            Path target = directory.resolve(fileName).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException exception) {
            throw new BadRequestException("FILE_STORE_FAILED", "Cannot store file");
        }
    }

    public record ApiApplyResult(Long id, String message) {
    }
}
