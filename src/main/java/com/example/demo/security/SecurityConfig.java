package com.example.demo.security;

import com.example.demo.security.authorization.SelfOrAdminAuthorizationManager;
import com.example.demo.security.authorization.SelfOrStaffAuthorizationManager;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {

  private final AuthenticationProvider authenticationProvider;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
  private final RestAccessDeniedHandler restAccessDeniedHandler;
  private final SelfOrStaffAuthorizationManager selfOrStaffAuthorizationManager;
  private final SelfOrAdminAuthorizationManager selfOrAdminAuthorizationManager;
  private final GradeReadAuthorizationManager gradeReadAuthorizationManager;
  private final GradeAuthorizationManager gradeAuthorizationManager;

  private static final String[] ADMIN_ONLY_RESOURCES = {
    "/courses/**",
    "/groups/**",
    "/academic-years/**",
    "/course-offerings/**",
    "/teacher-assignments/**",
    "/exams/**"
  };

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    var loginEntryPoint = new LoginUrlAuthenticationEntryPoint("/login");
    var promotionsMatcher = new AntPathRequestMatcher("/promotions/**");

    http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/auth/**", "/ping", "/health/**", "/login", "/css/**")
                    .permitAll()
                    .requestMatchers("/promotions/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/graduates/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, ADMIN_ONLY_RESOURCES)
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, ADMIN_ONLY_RESOURCES)
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, ADMIN_ONLY_RESOURCES)
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, ADMIN_ONLY_RESOURCES)
                    .hasRole("ADMIN")
                    .requestMatchers("/users/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/enrollments")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/enrollments/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/enrollments")
                    .access(selfOrStaffAuthorizationManager)
                    .requestMatchers(HttpMethod.GET, "/enrollments/track")
                    .access(selfOrStaffAuthorizationManager)
                    .requestMatchers(HttpMethod.POST, "/grades/{examSessionId}")
                    .access(gradeAuthorizationManager)
                    .requestMatchers(HttpMethod.GET, "/grades/**")
                    .access(gradeReadAuthorizationManager)
                    .requestMatchers(HttpMethod.GET, "/averages/**")
                    .access(selfOrStaffAuthorizationManager)
                    .requestMatchers(HttpMethod.GET, "/transcripts/**")
                    .access(selfOrAdminAuthorizationManager)
                    .requestMatchers(HttpMethod.POST, "/transcripts/{studentId}/send-email")
                    .access(selfOrAdminAuthorizationManager)
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(authenticationProvider)
        .exceptionHandling(
            handling ->
                handling
                    .defaultAuthenticationEntryPointFor(loginEntryPoint, promotionsMatcher)
                    .defaultAccessDeniedHandlerFor(
                        (request, response, ex) -> response.sendRedirect("/login"),
                        promotionsMatcher)
                    .authenticationEntryPoint(restAuthenticationEntryPoint)
                    .accessDeniedHandler(restAccessDeniedHandler))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
