package huynv.jobservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import huynv.jobservice.domain.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleName(String roleName);
}
