package example.cashcard;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

// Erzeugt unter anderem einen Filter in der Security Chain
@EnableWebSecurity
// Spring nutzt diese Klasse nun für die Konfiguration. Jede Bean-Methode wird aufgerufen und das Resultat in den Spring IoC Container gelegt
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(request -> request
                        // Alle Anfragen an /cashcards/** müssen authentifiziert sein bei Basic Auth
                        // TODO Rollen einführen
                        .requestMatchers("/cashcards/**").authenticated())
                .httpBasic(Customizer.withDefaults())
                // TODO Cross Site Request Forgery (CSRF) lernen
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    UserDetailsService onlyTestUser(PasswordEncoder passwordEncoder) {
        UserDetails owner1 = User.builder()
                .username("owner1")
                .password(passwordEncoder.encode("12345"))
                .roles(UserRole.CARD_OWNER.toString())
                .build();
        UserDetails owner2 = User.builder()
                .username("owner2")
                .password(passwordEncoder.encode("22345"))
                .roles(UserRole.CARD_OWNER.toString())
                .build();
        UserDetails hank = User.builder()
                .username("hank-owns-no-cards")
                .password(passwordEncoder.encode("54321"))
                .roles(UserRole.NON_CARD_OWNER.name())
                .build();
        return new InMemoryUserDetailsManager(owner1, hank);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public InMemoryUserDetailsManager userDetailsService() {
//        UserDetails user = User.withDefaultPasswordEncoder()
//                .username("user")
//                .password("password")
//                .roles("USER")
//                .build();
//        return new InMemoryUserDetailsManager(user);
//    }
}