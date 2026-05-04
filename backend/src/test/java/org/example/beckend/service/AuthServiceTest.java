package org.example.beckend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.example.beckend.entity.LoginAccount;
import org.example.beckend.entity.UserInformation;
import org.example.beckend.repository.LoginAccountRepository;
import org.example.beckend.repository.UserInformationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private LoginAccountRepository loginAccountRepository;

    @Mock
    private UserInformationRepository userInformationRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void verifyLogin_acceptsMatchingBcrypt() {
        String hash = passwordEncoder.encode("correct-horse");
        LoginAccount account = new LoginAccount();
        account.setId(100L);
        account.setPassword(hash);
        when(loginAccountRepository.findById(100L)).thenReturn(Optional.of(account));

        AuthService svc = new AuthService(loginAccountRepository, userInformationRepository, passwordEncoder, false);
        assertTrue(svc.verifyLogin(100L, "correct-horse"));
        assertFalse(svc.verifyLogin(100L, "wrong"));
    }

    @Test
    void verifyLogin_rejectsPlaintextWhenLegacyDisabled() {
        LoginAccount account = new LoginAccount();
        account.setId(1L);
        account.setPassword("plain-secret");
        when(loginAccountRepository.findById(1L)).thenReturn(Optional.of(account));

        AuthService svc = new AuthService(loginAccountRepository, userInformationRepository, passwordEncoder, false);
        assertFalse(svc.verifyLogin(1L, "plain-secret"));
    }

    @Test
    void authenticate_byEmail_resolvesUserIdWhenPasswordMatches() {
        String hash = passwordEncoder.encode("secret");
        LoginAccount account = new LoginAccount();
        account.setId(555_666_777L);
        account.setPassword(hash);
        UserInformation profile = new UserInformation();
        profile.setId(555_666_777L);
        profile.setUserEmail("u@example.com");
        when(userInformationRepository.findByUserEmailIgnoreCase("u@example.com")).thenReturn(Optional.of(profile));
        when(loginAccountRepository.findById(555_666_777L)).thenReturn(Optional.of(account));

        AuthService svc = new AuthService(loginAccountRepository, userInformationRepository, passwordEncoder, false);
        assertEquals(Optional.of(555_666_777L), svc.authenticate(null, "u@example.com", "secret"));
        assertTrue(svc.authenticate(null, "u@example.com", "wrong").isEmpty());
    }

    @Test
    void verifyLogin_acceptsPlaintextWhenLegacyEnabled() {
        LoginAccount account = new LoginAccount();
        account.setId(1L);
        account.setPassword("plain-secret");
        when(loginAccountRepository.findById(1L)).thenReturn(Optional.of(account));

        AuthService svc = new AuthService(loginAccountRepository, userInformationRepository, passwordEncoder, true);
        assertTrue(svc.verifyLogin(1L, "plain-secret"));
    }
}
