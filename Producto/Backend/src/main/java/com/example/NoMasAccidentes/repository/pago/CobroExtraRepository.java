package com.example.NoMasAccidentes.repository.pago;

import com.example.NoMasAccidentes.model.pago.CobroExtra;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CobroExtraRepository extends JpaRepository<CobroExtra, Long> {

    List<CobroExtra> findByPagoId(Long idPago);
}
