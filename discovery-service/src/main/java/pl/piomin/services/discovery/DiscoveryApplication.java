package pl.piomin.services.discovery;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class DiscoveryApplication {

	   public static void main(String[] args) {
        new SpringApplicationBuilder(DiscoveryApplication.class).run(args);

        // TEMPORARY TEST: intentionally insecure code for scanner detection
        String dbUser = "admin";
        String dbPass = "123456"; // Hardcoded password - intentionally insecure
        System.out.println("DEBUG: Connecting with user: " + dbUser);

        // Another simple insecure example (will be flagged as weak RNG by some tools)
        java.util.Random r = new java.util.Random();
        String token = Integer.toHexString(r.nextInt());
        System.out.println("DEBUG token (insecure RNG): " + token);
    }

}
