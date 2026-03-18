package dev.nixgeek.bliki.service.frameworks.resources.config

import com.nimbusds.jose.jwk.source.ImmutableSecret
import dev.nixgeek.bliki.lib.annotations.NotATestContainerBean
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
import org.springframework.security.crypto.factory.PasswordEncoderFactories
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

/**
 * Security configuration for the Bliki service application.
 *
 * This class configures Spring Security for a reactive WebFlux application, including:
 * - JWT-based authentication and authorization
 * - CORS configuration for cross-origin requests
 * - Password encoding using delegating password encoder
 * - Role-based access control for different API endpoints
 *
 * The configuration uses OAuth2 resource server with JWT tokens and enables reactive method security.
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfiguration {
    /**
     * Companion object containing constants for security configuration.
     *
     * Defines security-related constants, including:
     * - CORS allowed origins (development servers on localhost)
     * - CORS allowed HTTP methods
     * - JWT secret key algorithm (HMAC SHA-256)
     * - Header names and path patterns
     */
    companion object {
        private const val ALLOWED_HEADERS = "*"
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val SECRET_KEY_ALGORITHM = "HmacSHA256"
        private const val URL_PATH_ALL = "/**"

        private val corsAllowedOrigins = listOf("http://localhost:3000", "http://localhost:5173")
        private val corsAllowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
    }

    /**
     * Configures the Spring Security filter chain for the application.
     *
     * This method sets up:
     * - Disables CSRF, HTTP Basic, form login, and logout
     * - Enables CORS with configured sources
     * - Defines authorization rules for different endpoint patterns:
     *   - Public access: `/docs`, `/login`, `/manage/health`, `/public`
     *   - Admin-only access: `/admin`
     *   - All other requests are denied
     * - Configures OAuth2 resource server with JWT authentication
     *
     * @param http The ServerHttpSecurity to configure
     * @param jwtAuthenticationConverter Converter for JWT authentication
     * @return Configured SecurityWebFilterChain
     */
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

    /**
     * Provides a delegating password encoder bean.
     *
     * Creates a password encoder that supports multiple encoding strategies with bcrypt as default.
     * The delegating encoder allows for password hash format evolution and supports verification
     * of passwords encoded with different algorithms.
     *
     * @return DelegatingPasswordEncoder instance
     */
    @Bean
    @NotATestContainerBean
    fun passwordEncoder(): PasswordEncoder =
        PasswordEncoderFactories.createDelegatingPasswordEncoder()

    /**
     * Provides a reactive JWT decoder bean for validating JWT tokens.
     *
     * Configures a Nimbus-based JWT decoder using a symmetric secret key with HMAC SHA-256 algorithm.
     * The secret key is obtained from the application's JWT properties configuration.
     *
     * @param jwtProperties Configuration properties containing the JWT secret
     * @return ReactiveJwtDecoder for validating incoming JWT tokens
     */
    @Bean
    fun reactiveJwtDecoder(jwtProperties: JwtProperties): ReactiveJwtDecoder =
        NimbusReactiveJwtDecoder
            .withSecretKey(
                SecretKeySpec(
                    jwtProperties.secret.toByteArray(),
                    SECRET_KEY_ALGORITHM,
                ),
            ).build()

    /**
     * Provides a JWT encoder bean for creating JWT tokens.
     *
     * Configures a Nimbus-based JWT encoder using a symmetric secret key with HMAC SHA-256 algorithm.
     * The secret key is obtained from the application's JWT properties configuration.
     *
     * @param jwtProperties Configuration properties containing the JWT secret
     * @return JwtEncoder for generating signed JWT tokens
     */
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

    /**
     * Provides CORS configuration source for cross-origin request handling.
     *
     * Configures CORS to:
     * - Allow requests from localhost development servers (ports 3000 and 5173)
     * - Allow standard HTTP methods (GET, POST, PUT, PATCH, DELETE, OPTIONS)
     * - Allow all headers in requests
     * - Expose Authorization header in responses
     * - Disable credentials for security
     *
     * The configuration is applied to all URL paths.
     *
     * @return CorsConfigurationSource with the configured CORS rules
     */
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
