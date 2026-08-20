package com.borderlessteamwork.sixpeng.global.config;

import com.borderlessteamwork.sixpeng.global.exception.ErrorCode;
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
                // 프론트/백엔드가 서로 다른 도메인이라 프론트 JS 가 XSRF-TOKEN 쿠키를 읽을 수 없어
                // double-submit-cookie 방식의 CSRF 토큰 검증이 원천적으로 불가능하다.
                // 대신 CORS 허용 origin 을 명시적 목록으로 제한하고(와일드카드 아님), 상태를 바꾸는
                // 요청은 모두 JSON 본문을 요구해(단순 요청이 아니라 preflight 대상) 이를 방어선으로 삼는다.
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 배포 확인용 엔드포인트. health/info 만 열려 있고 민감 정보는 담기지 않는다.
                        .requestMatchers("/actuator/health", "/actuator/info")
                        .permitAll()
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
