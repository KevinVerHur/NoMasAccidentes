package com.example.NoMasAccidentes.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final Key claveFirma;
    private final long expiracionMs;

    public JwtService(
            @Value("${jwt.secret}") String secretBase64,
            @Value("${jwt.expiration}") long expiracionMs) {
        // La clave debe tener al menos 256 bits (32 bytes) para HS256.
        byte[] bytes = secretBase64.getBytes();
        if (bytes.length < 32) {
            // Permitir pasar el secret en base64 también
            try {
                bytes = Decoders.BASE64.decode(secretBase64);
            } catch (Exception ignorado) {
                // se queda con los bytes crudos
            }
        }
        this.claveFirma = Keys.hmacShaKeyFor(bytes);
        this.expiracionMs = expiracionMs;
    }

    /** Genera un token con el email como subject y el rol como claim. */
    public String generarToken(String email, String rol) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expiracionMs);
        return Jwts.builder()
                .subject(email)
                .claims(Map.of("rol", rol))
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(claveFirma)
                .compact();
    }

    public String extraerEmail(String token) {
        return parsear(token).getSubject();
    }

    public boolean esValido(String token, UserDetails userDetails) {
        Claims claims = parsear(token);
        return claims.getSubject().equals(userDetails.getUsername())
                && claims.getExpiration().after(new Date());
    }

    private Claims parsear(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) claveFirma)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
