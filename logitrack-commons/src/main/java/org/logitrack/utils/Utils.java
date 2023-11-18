package org.logitrack.utils;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.logitrack.exceptions.CommonApplicationException;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.Authentication;

import java.security.Key;
import java.util.Map;

@Slf4j
public class Utils {
//    @Value("${jwt.secretToken}")
//    private String secretToken;
    private static String secretToken = "sdfghjklsdfghjksdfghjdfghjertyucvbertyxcvertyxcvtyvertyertyuertyuxcvxcertertdfgxcvsdfgdf";


    private static Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretToken);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public static Claims getUserNameFromToken(String token) {
        log.info("JwtService is called to extract the userEmail from the JWT");
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static boolean validateToken(String token) throws CommonApplicationException {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Map validateTokenAndReturnDetail(String secretToken) throws CommonApplicationException {
        if (Boolean.FALSE.equals(validateToken(secretToken))) {
            throw new CommonApplicationException("Invalid Token");
        }
        var claim = getUserNameFromToken(secretToken);
        return Map.of("name", claim.get("name", String.class),
                "email", claim.get("email", String.class),
                "role", claim.get("role", String.class));
    }
}
