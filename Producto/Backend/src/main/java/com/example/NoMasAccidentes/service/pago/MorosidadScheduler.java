package com.example.NoMasAccidentes.service.pago;

import com.example.NoMasAccidentes.model.cliente.EstadoCliente;
import com.example.NoMasAccidentes.model.pago.EstadoPago;
import com.example.NoMasAccidentes.model.pago.Pago;
import com.example.NoMasAccidentes.model.usuario.Usuario;
import com.example.NoMasAccidentes.repository.pago.PagoRepository;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;



@Component
@RequiredArgsConstructor
@Slf4j
public class MorosidadScheduler {
    
    private final PagoRepository pagoRepository;

    @Scheduled(cron = "${app.scheduler.morosidad.cron:0 0 2 * * *}", zone = "America/Santiago")
    @Transactional
    public void revisarMorosidad() {
        LocalDate hoy = LocalDate.now();

        var pagosVencidos = pagoRepository.findByEstadoAndFechaVencimientoBefore(
            EstadoPago.PENDIENTE,
            hoy
        );

        Set<Long> clientesProcesados = new HashSet<>();

        for (Pago pago : pagosVencidos){
            pago.setEstado(EstadoPago.VENCIDO);
            
            var cliente = pago.getCliente();

            if (cliente == null || cliente.getId() == null){
                continue;
            }

            clientesProcesados.add(cliente.getId());

            if (cliente.getEstado() != EstadoCliente.SUSPENDIDO){
                cliente.setEstado(EstadoCliente.MOROSO);
            }

            Usuario usuario = cliente.getUsuario();
            if (usuario != null && usuario.isActivo()){
                usuario.setActivo(false);
            }
        }


        log.info(
                "Revision diaria de morosidad finalizada. Pagos vencidos={}, clientes afectados={}",
                pagosVencidos.size(),
                clientesProcesados.size()   
        );
    }
}
