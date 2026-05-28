import io.jsonwebtoken.*;
import io.jsonwebtoken.security.*;
import java.security.*;
import java.security.spec.*;
import java.util.*;

public class JwtTest {
    public static void main(String[] args) throws Exception {
        String b64pem = System.getenv("JWT_PRIVATE_KEY");
        // Decode base64 -> PEM text
        byte[] pemBytes = Base64.getDecoder().decode(b64pem.trim());
        String pem = new String(pemBytes);
        // Strip headers and whitespace
        String inner = pem.replace("-----BEGIN PRIVATE KEY-----","").replace("-----END PRIVATE KEY-----","").replaceAll("\s","");
        byte[] der = Base64.getDecoder().decode(inner);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
        PrivateKey pk = KeyFactory.getInstance("RSA").generatePrivate(spec);
        System.out.println("Key parsed OK: " + pk.getClass().getSimpleName());
        // Try to sign with JJWT
        try {
            String jwt = Jwts.builder()
                .subject("test")
                .signWith(pk, Jwts.SIG.RS256)
                .compact();
            System.out.println("JJWT sign OK: " + jwt.substring(0,20) + "...");
        } catch (Exception e) {
            System.out.println("JJWT sign FAILED: " + e.getMessage());
            e.printStackTrace();
        }
        // Try raw JCA signing
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(pk);
            sig.update("test".getBytes());
            byte[] signed = sig.sign();
            System.out.println("JCA sign OK: " + signed.length + " bytes");
        } catch (Exception e) {
            System.out.println("JCA sign FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
