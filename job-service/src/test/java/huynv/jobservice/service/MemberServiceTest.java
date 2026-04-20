package huynv.jobservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import huynv.jobservice.domain.ApplyPost;
import huynv.jobservice.domain.Category;
import huynv.jobservice.domain.Company;
import huynv.jobservice.domain.Cv;
import huynv.jobservice.domain.Recruitment;
import huynv.jobservice.domain.Role;
import huynv.jobservice.domain.SaveJob;
import huynv.jobservice.domain.User;
import huynv.jobservice.repository.ApplyPostRepository;
import huynv.jobservice.repository.CompanyRepository;
import huynv.jobservice.repository.CvRepository;
import huynv.jobservice.repository.FollowCompanyRepository;
import huynv.jobservice.repository.RecruitmentRepository;
import huynv.jobservice.repository.SaveJobRepository;
import huynv.jobservice.repository.UserRepository;
import huynv.jobservice.web.dto.RecruitmentUpsertRequest;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private CvRepository cvRepository;
    @Mock
    private RecruitmentRepository recruitmentRepository;
    @Mock
    private SaveJobRepository saveJobRepository;
    @Mock
    private FollowCompanyRepository followCompanyRepository;
    @Mock
    private ApplyPostRepository applyPostRepository;
    @Mock
    private PublicQueryService publicQueryService;

    @TempDir
    java.nio.file.Path tempDir;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(
            userRepository,
            companyRepository,
            cvRepository,
            recruitmentRepository,
            saveJobRepository,
            followCompanyRepository,
            applyPostRepository,
            publicQueryService,
            tempDir.toString()
        );
    }

    @Test
    void uploadCvStoresFileAndUpdatesUserCv() throws Exception {
        User user = createUser("USER");
        Cv cv = new Cv();
        cv.setId(11L);
        user.setCv(cv);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(cvRepository.save(any(Cv.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var file = new MockMultipartFile("file", "resume.pdf", "application/pdf", "cv".getBytes());

        var profile = memberService.uploadCv(user, file);

        assertThat(profile.cvFileName()).contains("resume.pdf");
        assertThat(Files.walk(tempDir).anyMatch(path -> path.getFileName().toString().contains("resume.pdf"))).isTrue();
    }

    @Test
    void createRecruitmentRequiresEmployerRole() {
        User user = createUser("USER");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> memberService.createRecruitment(user, request()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Employer account required");
    }

    @Test
    void deleteRecruitmentRemovesApplicationsAndSavedJobsFirst() {
        User employer = createUser("EMPLOYER");
        Company company = createCompany(employer);
        Recruitment recruitment = createRecruitment(company);
        SaveJob saveJob = new SaveJob();
        saveJob.setId(1L);
        saveJob.setRecruitment(recruitment);
        ApplyPost applyPost = new ApplyPost();
        applyPost.setId(2L);
        applyPost.setRecruitment(recruitment);

        when(userRepository.findById(employer.getId())).thenReturn(Optional.of(employer));
        when(companyRepository.findByUserId(employer.getId())).thenReturn(Optional.of(company));
        when(recruitmentRepository.findById(5L)).thenReturn(Optional.of(recruitment));
        when(applyPostRepository.findByRecruitmentIdOrderByIdDesc(5L)).thenReturn(List.of(applyPost));
        when(saveJobRepository.findByRecruitmentId(5L)).thenReturn(List.of(saveJob));

        memberService.deleteRecruitment(employer, 5L);

        verify(applyPostRepository).delete(applyPost);
        verify(saveJobRepository).delete(saveJob);
        verify(recruitmentRepository).delete(recruitment);
    }

    @Test
    void deleteApplicationRejectsForeignApplication() {
        User candidate = createUser("USER");
        candidate.setId(1L);

        User otherCandidate = createUser("USER");
        otherCandidate.setId(99L);

        ApplyPost applyPost = new ApplyPost();
        applyPost.setId(7L);
        applyPost.setUser(otherCandidate);

        when(userRepository.findById(candidate.getId())).thenReturn(Optional.of(candidate));
        when(applyPostRepository.findById(7L)).thenReturn(Optional.of(applyPost));

        assertThatThrownBy(() -> memberService.deleteApplication(candidate, 7L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("You cannot delete this application");
    }

    private User createUser(String roleName) {
        Role role = new Role();
        role.setRoleName(roleName);
        User user = new User();
        user.setId(1L);
        user.setFullName("Test User");
        user.setEmail("user@example.com");
        user.setRole(role);
        return user;
    }

    private Company createCompany(User user) {
        Company company = new Company();
        company.setId(2L);
        company.setCompanyName("Company");
        company.setUser(user);
        return company;
    }

    private Recruitment createRecruitment(Company company) {
        Category category = new Category();
        category.setId(3L);
        category.setName("JAVA");
        Recruitment recruitment = new Recruitment();
        recruitment.setId(5L);
        recruitment.setCompany(company);
        recruitment.setCategory(category);
        recruitment.setTitle("Java Developer");
        return recruitment;
    }

    private RecruitmentUpsertRequest request() {
        return new RecruitmentUpsertRequest(
            "Java Developer",
            "Da Nang",
            "Build APIs",
            "1 year",
            2,
            "Middle",
            "2000",
            "Fulltime",
            "2026-12-31",
            3L
        );
    }
}
