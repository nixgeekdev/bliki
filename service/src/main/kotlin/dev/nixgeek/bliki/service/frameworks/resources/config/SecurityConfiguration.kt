package dev.nixgeek.bliki.service.frameworks.resources.config

import com.nimbusds.jose.jwk.source.ImmutableSecret
import dev.nixgeek.bliki.service.domain.model.IdentityRole
import dev.nixgeek.bliki.service.frameworks.resources.config.properties.JwtProperties
import dev.nixgeek.bliki.service.shared.resources.Routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfiguration {
    companion object {
        private const val ALLOWED_HEADERS = "*"
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val SECRET_KEY_ALGORITHM = "HmacSHA256"
        private const val URL_PATH_ALL = "/**"

        private val corsAllowedOrigins = listOf("http://localhost:3000", "http://localhost:5173")
        private val corsAllowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
    }

    @Bean
    fun springSecurityFilterChain(
        http: ServerHttpSecurity,
        jwtAuthenticationConverter: JwtAuthenticationConverter,
    ): SecurityWebFilterChain =
        http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .cors(Customizer.withDefaults())
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers("${Routes.DOCS_BASE}$URL_PATH_ALL")
                    .permitAll()
                    .pathMatchers(HttpMethod.POST, Routes.LOGIN_BASE)
                    .permitAll()
                    .pathMatchers("${Routes.MANAGE_HEALTH_BASE}$URL_PATH_ALL")
                    .permitAll()
                    .pathMatchers("${Routes.PUBLIC_BASE}$URL_PATH_ALL")
                    .permitAll()
                    .pathMatchers("${Routes.ADMIN_BASE}$URL_PATH_ALL")
                    .hasRole(IdentityRole.ADMIN.name)
                    .anyExchange()
                    .denyAll()
            }.oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)
                }
            }.build()

    @Bean
    fun passwordEncoder(): PasswordEncoder =
        Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()

    @Bean
    fun reactiveJwtDecoder(jwtProperties: JwtProperties): ReactiveJwtDecoder =
        NimbusReactiveJwtDecoder
            .withSecretKey(
                SecretKeySpec(
                    jwtProperties.secret.toByteArray(),
                    SECRET_KEY_ALGORITHM,
                ),
            ).build()

    @Bean
    fun jwtEncoder(jwtProperties: JwtProperties): JwtEncoder =
        NimbusJwtEncoder(
            ImmutableSecret(
                SecretKeySpec(
                    jwtProperties.secret.toByteArray(),
                    SECRET_KEY_ALGORITHM,
                ),
            ),
        )

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource =
        CorsConfiguration()
            .apply {
                allowedOrigins = corsAllowedOrigins
                allowedMethods = corsAllowedMethods
                allowedHeaders = listOf(ALLOWED_HEADERS)
                exposedHeaders = listOf(AUTHORIZATION_HEADER)
                allowCredentials = false
            }.let { configuration ->
                UrlBasedCorsConfigurationSource().apply {
                    registerCorsConfiguration(URL_PATH_ALL, configuration)
                }
            }
}
