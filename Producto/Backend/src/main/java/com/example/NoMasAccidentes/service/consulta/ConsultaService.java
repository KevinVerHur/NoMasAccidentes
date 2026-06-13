package com.example.NoMasAccidentes.service.consulta;

import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.consulta.ConsultaResponse;
import com.example.NoMasAccidentes.dto.consulta.CrearConsultaRequest;
import com.example.NoMasAccidentes.model.cliente.Cliente;
import com.example.NoMasAccidentes.model.consulta.Consulta;
import com.example.NoMasAccidentes.repository.cliente.ClienteRepository;
import com.example.NoMasAccidentes.repository.consulta.ConsultaRepository;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultaService {
    
    private static final LocalTime INICIO_ATENCION = LocalTime.of(9,0);
    private static final LocalTime FIN_ATENCION = LocalTime.of(18, 0);

    private final ConsultaRepository consultaRepository;
    private final ClienteRepository clienteRepository;

    public Page<ConsultaResponse> listar(Pageable pageable) {
        return consultaRepository.findAll(pageable).map(this::toResponse);
    }
    public List<ConsultaResponse> listarPorCliente(Long idCliente){
        return consultaRepository.findByClienteIdOrderByFechaHoraDesc(idCliente)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional 
    public ConsultaResponse crear(CrearConsultaRequest request){
        Cliente cliente = clienteRepository.findById(request.idCliente())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", request.idCliente()));
        
        LocalDateTime fechaHora = LocalDateTime.now();
        boolean fueraHorario = !esHorarioAtencion(fechaHora);

        Consulta consulta = Consulta.builder()
                .cliente(cliente)
                .fechaHora(fechaHora)
                .motivo(request.motivo())
                .detalle(request.detalle())
                .fueraHorario(fueraHorario)
                .costoAdicional(fueraHorario)
                .build();

        return toResponse(consultaRepository.save(consulta));
    }

    private boolean esHorarioAtencion(LocalDateTime fechaHora){
        DayOfWeek dia = fechaHora.getDayOfWeek();
        LocalTime hora = fechaHora.toLocalTime();

        boolean diaHabil = dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY;
        boolean horaHabil = !hora.isBefore(INICIO_ATENCION) && !hora.isAfter(FIN_ATENCION);

        return diaHabil && horaHabil;
    }


    private ConsultaResponse toResponse(Consulta consulta) {
        return new ConsultaResponse(
                consulta.getId(),
                consulta.getCliente().getId(),
                consulta.getCliente().getRazonSocial(),
                consulta.getFechaHora(),
                consulta.getMotivo(),
                consulta.getDetalle(),
                consulta.isFueraHorario(),
                consulta.isCostoAdicional()
        );
    }
}
