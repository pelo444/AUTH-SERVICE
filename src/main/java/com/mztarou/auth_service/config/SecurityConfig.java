package com.mztarou.auth_service.config;

import com.mztarou.auth_service.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 許可するオリジン（開発中）
        configuration.setAllowedOrigins(List.of(
            "http://localhost:5500",   // VS Code Live Server
            "http://127.0.0.1:5500",  // VS Code Live Server（別表記）
            "http://localhost:3000"    // その他ローカルサーバー
        ));

        // 許可するHTTPメソッド
        configuration.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        // 許可するヘッダー
        configuration.setAllowedHeaders(List.of("*"));

        // Cookieを含むリクエストを許可（セッション認証に必要）
        configuration.setAllowCredentials(true);

        // プリフライトリクエストのキャッシュ時間（秒）
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        org.springframework.security.authentication.AuthenticationProvider provider =
            new org.springframework.security.authentication.AuthenticationProvider() {

            @Override
            public Authentication authenticate(Authentication authentication)
                    throws AuthenticationException {

                String personId = authentication.getName();
                String rawPassword = authentication.getCredentials().toString();

                UserDetails userDetails;
                try {
                    userDetails = customUserDetailsService.loadUserByUsername(personId);
                } catch (UsernameNotFoundException e) {
                    throw e;
                }

                if (!passwordEncoder().matches(rawPassword, userDetails.getPassword())) {
                    throw new org.springframework.security.authentication
                        .BadCredentialsException("パスワードが一致しません");
                }

                return new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
                );
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return UsernamePasswordAuthenticationToken.class
                    .isAssignableFrom(authentication);
            }
        };

        return new ProviderManager(List.of(provider));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authenticationManager(authenticationManager())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/logout").permitAll()
                .requestMatchers("/api/auth/status").permitAll()
                .requestMatchers("/api/users/register").permitAll()
                .requestMatchers("/api/users/preregister").permitAll()
                .requestMatchers("/api/users/verify").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}