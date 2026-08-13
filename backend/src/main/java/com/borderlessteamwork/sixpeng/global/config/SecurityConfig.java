package com.borderlessteamwork.sixpeng.global.config;

import com.borderlessteamwork.sixpeng.global.exception.ErrorCode;
import com.borderlessteamwork.sixpeng.global.security.CsrfCookieFilter;
import com.borderlessteamwork.sixpeng.global.security.CustomOAuth2UserService;
import com.borderlessteamwork.sixpeng.global.security.ErrorResponseWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final ErrorResponseWriter errorResponseWriter;

    @Value("${app.oauth2.success-redirect-uri}")
    private String successRedirectUri;

    @Value("${app.oauth2.failure-redirect-uri}")
    private String failureRedirectUri;

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 세션 쿠키가 SameSite=None 이라 SameSite 방어가 없다. CSRF 토큰이 유일한 방어선이다.
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository())
                        .csrfTokenRequestHandler(csrfTokenRequestHandler()))
                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/accounts/oauth/**", "/oauth2/**", "/login/**", "/error")
                        .permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oauth2SuccessHandler())
                        .failureHandler(new SimpleUrlAuthenticationFailureHandler(failureRedirectUri)))
                .logout(logout -> logout
                        .logoutUrl("/accounts/logout")
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpStatus.NO_CONTENT.value()))
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID"))
                .exceptionHandling(exception -> exception
                        // SPA 라 로그인 페이지로 리다이렉트하지 않고 401/403 JSON 을 내려준다.
                        .authenticationEntryPoint((request, response, authException) ->
                                errorResponseWriter.write(response, ErrorCode.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                errorResponseWriter.write(response, ErrorCode.ACCESS_DENIED)));

        return http.build();
    }

    /**
     * SPA 가 읽어야 하므로 HttpOnly 를 끄고, 세션 쿠키와 같은 SameSite=None; Secure 로 맞춘다.
     * 프론트는 XSRF-TOKEN 쿠키 값을 X-XSRF-TOKEN 헤더로 되돌려 보내야 한다.
     */
    private CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieCustomizer(cookie -> cookie.sameSite("None").secure(true));
        return repository;
    }

    private CsrfTokenRequestAttributeHandler csrfTokenRequestHandler() {
        CsrfTokenRequestAttributeHandler handler = new CsrfTokenRequestAttributeHandler();
        // null 로 두면 토큰을 지연 로딩하지 않아 BREACH 대응 인코딩 없이 원문 토큰을 그대로 비교한다(SPA 권장 설정).
        handler.setCsrfRequestAttributeName(null);
        return handler;
    }

    private SimpleUrlAuthenticationSuccessHandler oauth2SuccessHandler() {
        SimpleUrlAuthenticationSuccessHandler handler =
                new SimpleUrlAuthenticationSuccessHandler(successRedirectUri);
        handler.setAlwaysUseDefaultTargetUrl(true);
        return handler;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        // 프론트가 fetch(credentials: 'include') 로 세션 쿠키를 보내야 한다.
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
