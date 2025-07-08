package dummy_api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity) : SecurityFilterChain {
        http.csrf { customizer -> customizer.disable() }
        http
            .authorizeHttpRequests { customizer -> customizer.anyRequest().permitAll() }

        return http.build()
    }
}