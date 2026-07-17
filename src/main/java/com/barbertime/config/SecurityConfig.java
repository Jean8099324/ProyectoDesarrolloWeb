package com.barbertime.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.barbertime.repository.UsuarioRepository;

@Configuration
public class SecurityConfig {

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {
                return username -> {
                        String correoNormalizado = username.trim().toLowerCase();

                        return usuarioRepository.findByUsername(correoNormalizado).map(usuario -> User.builder()
                                        .username(usuario.getUsername()).password(usuario.getPassword())
                                        .authorities(usuario.getRoles().stream()
                                                        .map(rol -> "ROLE_" + rol.getNombre().toUpperCase())
                                                        .toArray(String[]::new))
                                        .build())
                                        .orElseThrow(() -> new UsernameNotFoundException(
                                                        "Usuario no encontrado: " + correoNormalizado));
                };
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                http.authorizeHttpRequests(auth -> auth
                                .requestMatchers("/", "/login", "/registro", "/error", "/css/**", "/js/**",
                                                "/images/**")
                                .permitAll()

                                .requestMatchers("/admin/**").hasRole("ADMIN")

                                .requestMatchers("/barbero/**").hasAnyRole("BARBERO", "ADMIN")

                                .requestMatchers("/reservas/**", "/perfil/**").authenticated()

                                .anyRequest().authenticated())

                                .formLogin(login -> login.loginPage("/login").loginProcessingUrl("/login")
                                                .usernameParameter("username").passwordParameter("password")
                                                .defaultSuccessUrl("/", true).failureUrl("/login?error=true")
                                                .permitAll())

                                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout=true")
                                                .invalidateHttpSession(true).deleteCookies("JSESSIONID").permitAll());

                return http.build();
        }
}