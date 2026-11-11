package jro.pry.PasarelaPagos.repository;

import jro.pry.PasarelaPagos.model.Wallet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUserId(Long userId);

    @EntityGraph(attributePaths = "user")
    @Query("select w from Wallet w where w.id in :ids")
    List<Wallet> findAllWithUserByIds(@Param("ids") Collection<Long> ids);
}
