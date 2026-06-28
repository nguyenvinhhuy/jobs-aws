package huynv.jobservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "recruitment")
public class Recruitment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address;

    @Column(columnDefinition = "text")
    private String description;

    private String experience;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "ranks")
    private String rank;

    private String salary;

    @Column(nullable = false)
    private String title;

    private String type;

    private String deadline;

    @Column(nullable = false)
    private Integer view;

    @Column(nullable = false)
    private Integer status;

    @Column(name = "created_at")
    private String createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
}
