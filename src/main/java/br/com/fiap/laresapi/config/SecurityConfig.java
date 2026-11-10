package br.com.fiap.laresapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
public class SecurityConfig {

	private final SecurityErrorHandler securityErrorHandler;

	public SecurityConfig(SecurityErrorHandler securityErrorHandler) {
		this.securityErrorHandler = securityErrorHandler;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.formLogin(form -> form.disable())
				.httpBasic(basic -> basic.disable())
				.logout(logout -> logout.disable())
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/auth/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/ocorrencias").hasRole("CIDADAO")
						.requestMatchers(HttpMethod.GET, "/ocorrencias/minhas").hasRole("CIDADAO")
						.requestMatchers(HttpMethod.PUT, "/ocorrencias/**").hasRole("CIDADAO")
						.requestMatchers(HttpMethod.DELETE, "/ocorrencias/**").hasRole("CIDADAO")
						.requestMatchers(HttpMethod.PATCH, "/ocorrencias/*/status").hasRole("GESTOR")
						.requestMatchers(HttpMethod.GET, "/ocorrencias/**").authenticated()
						.requestMatchers(HttpMethod.GET, "/categorias").authenticated()
						.requestMatchers("/usuarios/me").authenticated()
						.anyRequest().authenticated())
				.exceptionHandling(exceptionHandling -> exceptionHandling
						.authenticationEntryPoint(securityErrorHandler)
						.accessDeniedHandler(securityErrorHandler))
				.oauth2ResourceServer(resourceServer -> resourceServer
						.authenticationEntryPoint(securityErrorHandler)
						.accessDeniedHandler(securityErrorHandler)
						.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
				.build();
	}

	@Bean
	JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
		authoritiesConverter.setAuthoritiesClaimName("papel");
		authoritiesConverter.setAuthorityPrefix("ROLE_");

		JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
		authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
		return authenticationConverter;
	}
}