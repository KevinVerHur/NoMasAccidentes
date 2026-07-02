package com.example.NoMasAccidentes.repository.cliente;

import com.example.NoMasAccidentes.model.cliente.Cliente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /** Resuelve el representante a partir de su cuenta de usuario (portal cliente). */
    Optional<Cliente> findByUsuarioEmail(String email);

    /** Representantes de una empresa. */
    List<Cliente> findByEmpresaId(Long idEmpresa);

    /** Representante con login asociado a una empresa (para reenvío de invitación, etc.). */
    Optional<Cliente> findFirstByEmpresaIdAndUsuarioIsNotNull(Long idEmpresa);
}
