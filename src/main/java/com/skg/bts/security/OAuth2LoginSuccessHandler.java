package com.skg.bts.security;

import com.skg.bts.domain.User;
import com.skg.bts.domain.enums.AuthProvider;
import com.skg.bts.domain.enums.Role;
import com.skg.bts.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName() != null ? oidcUser.getFullName() : email;

        // Find-or-create: a Google login for an email that already exists as a LOCAL
        // account is treated as the same person logging in a different way, not a
        // separate account. Only a brand-new email creates a new GOOGLE-provider user.
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .name(name)
                        .email(email)
                        .passwordHash(null) // no local password for a Google-only signup
                        .role(Role.PASSENGER)
                        .authProvider(AuthProvider.GOOGLE)
                        .build()));

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        String redirectUrl = UriComponentsBuilder.fromPath("/oauth2-success.html")
                .queryParam("token", token)
                .queryParam("userId", user.getId())
                .queryParam("name", user.getName())
                .queryParam("role", user.getRole().name())
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
