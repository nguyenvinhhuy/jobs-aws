package huynv.jobservice.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "company")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address;

    @Column(columnDefinition = "text")
    private String description;

    private String email;

    private String logo;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(nullable = false)
    private Integer status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    private List<Recruitment> recruitments = new ArrayList<>();
}
