package huynv.jobservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import huynv.jobservice.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("select count(u) from User u where u.role.roleName = 'USER'")
    long countCandidates();
}
