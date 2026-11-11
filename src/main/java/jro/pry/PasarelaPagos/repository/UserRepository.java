package jro.pry.PasarelaPagos.repository;

import jro.pry.PasarelaPagos.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Override
    @EntityGraph(attributePaths = "wallets")
    List<User> findAll();

    @Override
    @EntityGraph(attributePaths = "wallets")
    Optional<User> findById(Long id);
}
