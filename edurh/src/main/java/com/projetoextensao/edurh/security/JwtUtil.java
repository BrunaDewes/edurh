package com.projetoextensao.edurh.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
    // Gera uma chave forte automaticamente (você pode gerar uma fixa depois)
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
            "UmaChaveSuperSecretaDeNoMinimo32Caracteres!".getBytes()
    );

    private static final long EXPIRATION_TIME = 7200000; //2 horas
    // private static final long EXPIRATION_TIME = 86400000; // 1 dia

    // Gera o token com o e-mail
    public static String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // Valida o token
    public static boolean validarToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // Extrai o e-mail do token
    public static String getEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException e) {
            return null;
        }
    }
}
