package dummy_api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity) : SecurityFilterChain {
        http.csrf { customizer -> customizer.disable() }
        http
            .httpBasic(Customizer.withDefaults())
            .authorizeHttpRequests { customizer -> customizer
                .requestMatchers("dummy/secret").authenticated()
                .anyRequest().permitAll() }

        return http.build()
    }

    @Bean
    fun userCreds() : UserDetailsService {
        return InMemoryUserDetailsManager(
            User.withUsername("bob")
                .password("{noop}bob")
                .build()
        )
    }
}