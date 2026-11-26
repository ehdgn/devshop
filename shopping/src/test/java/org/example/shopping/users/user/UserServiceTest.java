package org.example.shopping.users.user;

import org.example.shopping._core.errors.exception.Exception400;
import org.example.shopping.users.User;
import org.example.shopping.users.dto.UserResponse;
import org.example.shopping.users.enums.OAuthProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserService userService;

    private UserResponse.KakaoProfile profile(long kakaoId, String email) {
        UserResponse.KakaoAccount account = new UserResponse.KakaoAccount();
        account.setName("홍길동");
        account.setEmail(email);
        account.setPhoneNumber("+82 10-1234-5678");
        UserResponse.KakaoProfile profile = new UserResponse.KakaoProfile();
        profile.setId(kakaoId);
        profile.setKakaoAccount(account);
        return profile;
    }

    @Test
    void 같은_아이디의_일반_계정이_있어도_그_계정으로_로그인되지_않는다() {
        User local = User.builder().id(1L).username("kakao_123").provider(OAuthProvider.LOCAL).build();
        when(userRepository.findByProviderAndProviderId(OAuthProvider.KAKAO, "123")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("kakao_123")).thenReturn(Optional.of(local));
        when(userRepository.findByUsername(argThat(name -> !name.equals("kakao_123")))).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User user = userService.findOrCreateKakaoUser(profile(123L, null));

        assertNotSame(local, user);
        assertNotEquals("kakao_123", user.getUsername());
        assertEquals("123", user.getProviderId());
        assertEquals(OAuthProvider.KAKAO, user.getProvider());
    }

    @Test
    void 이미_가입한_카카오_회원은_회원번호로_찾는다() {
        User kakao = User.builder().id(2L).provider(OAuthProvider.KAKAO).providerId("123").build();
        when(userRepository.findByProviderAndProviderId(OAuthProvider.KAKAO, "123")).thenReturn(Optional.of(kakao));

        assertSame(kakao, userService.findOrCreateKakaoUser(profile(123L, null)));
        verify(userRepository, never()).save(any());
    }

    @Test
    void 같은_이메일의_다른_계정이_있으면_자동으로_연결하지_않는다() {
        when(userRepository.findByProviderAndProviderId(OAuthProvider.KAKAO, "123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(User.builder().id(1L).build()));

        assertThrows(Exception400.class, () -> userService.findOrCreateKakaoUser(profile(123L, "a@b.com")));
        verify(userRepository, never()).save(any());
    }
}
