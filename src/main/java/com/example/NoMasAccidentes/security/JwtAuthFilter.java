package com.example.NoMasAccidentes.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro que se ejecuta una vez por request y, si encuentra un JWT válido en la cabecera
 * Authorization, popula el SecurityContext con el usuario autenticado.
 * Se registra en SecurityConfig antes del UsernamePasswordAuthenticationFilter.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String CABECERA = "Authorization";
    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String cabecera = request.getHeader(CABECERA);
        if (cabecera == null || !cabecera.startsWith(PREFIJO)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = cabecera.substring(PREFIJO.length());
        try {
            String email = jwtService.extraerEmail(token);
            // Solo autenticar si aún no hay autenticación en el contexto
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                if (jwtService.esValido(token, userDetails)) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (Exception ex) {
            // Token inválido / expirado: dejar el contexto vacío y que el endpoint
            // protegido devuelva 401 a través del EntryPoint configurado.
            log.debug("Token JWT inválido: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
