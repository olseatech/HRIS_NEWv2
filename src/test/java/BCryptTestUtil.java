import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptTestUtil {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

        String rawPassword = "admin";

        // Generate a new hash
        String newHash = encoder.encode(rawPassword);
        System.out.println("Generated hash: " + newHash);

        // Test with the hash you provided
        String providedHash = "$2a$10$slYQmyNdGzin7olVN3/p2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMUm";
        boolean matches = encoder.matches(rawPassword, providedHash);
        System.out.println("Does 'admin' match provided hash? " + matches);

        // Verify the new hash
        boolean newHashMatches = encoder.matches(rawPassword, newHash);
        System.out.println("Does 'admin' match new hash? " + newHashMatches);
    }
}
