package org.logitrack.securities;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.logitrack.enums.Role;
import org.logitrack.exceptions.CommonApplicationException;
import org.logitrack.exceptions.UserExistException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
@Slf4j
public class JWTService {
    @Value("${jwt.secretToken}")
    private String secretToken;
//   private static final String secretToken = "sdfghjklsdfghjksdfghjdfghjertyucvbertyxcvertyxcvtyvertyertyuertyuxcvxcertertdfgxcvsdfgdf";

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretToken);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication, Role role) {
        String email = authentication.getName();
        String fullName = ((AppUserDetails) authentication.getPrincipal()).getFullName(); // Get user's full name

        Date currentDate = new Date(System.currentTimeMillis());
        Date expiryDate = new Date(System.currentTimeMillis() + 60000 * 120);

        return Jwts.builder()
                .setSubject(email)
                .claim("name", fullName)
                .claim("email", email)
                .claim("role", role.name())
                .setIssuedAt(currentDate)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }


    public String getUserNameFromToken(String token) {
        log.info("JwtService is called to extract the userEmail from the JWT");
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            throw new UserExistException("Invalid Token", HttpStatus.BAD_REQUEST);
        }
    }

    public  Claims getClaimsFromToken(String token) {
        log.info("JwtService is called to extract the userEmail from the JWT");
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public Map<String, String> validateTokenAndReturnDetail(String secretToken) throws CommonApplicationException {
        if (Boolean.FALSE.equals(validateToken(secretToken))) {
            throw new CommonApplicationException("Invalid Token");
        }
        var claim = getClaimsFromToken(secretToken);
        return Map.of("name", claim.get("name", String.class),
                "email", claim.get("email", String.class),
                "role", claim.get("role", String.class));
    }
}
