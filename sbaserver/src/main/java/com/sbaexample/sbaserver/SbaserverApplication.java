package com.sbaexample.sbaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import de.codecentric.boot.admin.server.config.EnableAdminServer;

@SpringBootApplication
@Configuration
@EnableAutoConfiguration
@EnableAdminServer
public class SbaserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(SbaserverApplication.class, args);
	}

	@Configuration(proxyBeanMethods = false)
	public static class SecuritySecureConfig {

		private final AdminServerProperties adminServer;

		public SecuritySecureConfig(AdminServerProperties adminServer) {
			this.adminServer = adminServer;
		}

		@Bean
		protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
			SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
			successHandler.setTargetUrlParameter("redirectTo");
			successHandler.setDefaultTargetUrl(this.adminServer.path("/"));

			http.authorizeHttpRequests((authorizeRequests) -> authorizeRequests
					.requestMatchers(new AntPathRequestMatcher(this.adminServer.path("/assets/**"))).permitAll()
					.requestMatchers(new AntPathRequestMatcher(this.adminServer.path("/login"))).permitAll()
					.anyRequest().authenticated())

					.formLogin((formLogin) -> formLogin.loginPage(this.adminServer.path("/login"))
							.successHandler(successHandler))
					.logout((logout) -> logout.logoutUrl(this.adminServer.path("/logout")))
					.httpBasic(Customizer.withDefaults())
					.csrf((csrf) -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
							.ignoringRequestMatchers(
									new AntPathRequestMatcher(this.adminServer.path("/instances"),
											HttpMethod.POST.toString()),
									new AntPathRequestMatcher(this.adminServer.path("/instances/*"),
											HttpMethod.DELETE.toString()),
									new AntPathRequestMatcher(this.adminServer.path("/actuator/**"))));

			return http.build();
		}
	}
}
