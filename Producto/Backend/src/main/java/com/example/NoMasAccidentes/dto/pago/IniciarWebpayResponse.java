package com.example.NoMasAccidentes.dto.pago;

/**
 * Datos para redirigir el navegador del cliente a la pasarela Webpay (RF09):
 * el frontend arma un form POST con {@code token_ws=token} hacia {@code url}.
 */
public record IniciarWebpayResponse(String token, String url) {}
