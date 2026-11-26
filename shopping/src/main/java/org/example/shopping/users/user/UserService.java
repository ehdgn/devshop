package org.example.shopping.users.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.shopping._core.errors.exception.Exception400;
import org.example.shopping._core.errors.exception.Exception401;
import org.example.shopping._core.errors.exception.Exception404;
import org.example.shopping._core.utils.SocialUtils;
import org.example.shopping._core.utils.ValidationUtils;
import org.example.shopping.cart.CartRepository;
import org.example.shopping.users.User;
import org.example.shopping.users.dto.UserRequest;
import org.example.shopping.users.dto.UserResponse;
import org.example.shopping.users.enums.OAuthProvider;
import org.example.shopping.users.enums.RoleType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;

    private static final int TOP_USERS_COUNT = 3;



    @Transactional
    public User signUp(@Valid UserRequest.SignUpDTO signUpDTO) {

        User user = signUpDTO.toEntity();
        user.setPassword(passwordEncoder.encode(signUpDTO.getPassword()));
        UserRole userRole = new UserRole(user, RoleType.USER);
        user.setRole(userRole);
        return userRepository.save(user);
    }

    @Transactional
    public boolean usernameIsExist(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Transactional
    public User login(@Valid UserRequest.LoginDTO loginDTO) {
        User userEntity = userRepository
                .findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new Exception401("아이디 또는 비밀번호가 일치하지 않아요"));

        if (userEntity == null) {
            throw new Exception400("회원이 아닙니다.");
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(loginDTO.getPassword(), userEntity.getPassword())) {
            throw new Exception401("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        return userEntity;
    }

    public User userUpdateView(Long userSessionId) {
        return userRepository.findById(userSessionId)
                .orElseThrow(() -> new Exception404("사용자를 못찾아요"));
    }

    @Transactional
    public User userUpdate(@Valid UserRequest.UpdateDTO updateDTO, Long userId) {
        User userEntity = userRepository
                .findById(userId).orElseThrow(() -> new Exception404("사용자를 찾지 못하겠어요"));

        updateDTO.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
        userEntity.update(updateDTO);

        return userEntity;
    }

    @Transactional
    public User findOrCreateKakaoUser(UserResponse.KakaoProfile profile) {
        // 카카오 회원번호로 찾는다: username 으로 찾으면 같은 이름으로 먼저 가입한 일반 계정에 로그인될 수 있음
        String kakaoId = String.valueOf(profile.getId());
        User userOrigin = userRepository.findByProviderAndProviderId(OAuthProvider.KAKAO, kakaoId).orElse(null);

        if (userOrigin != null) {
            return userOrigin;
        }

        String email = profile.getKakaoAccount().getEmail();
        if (email != null && userRepository.findByEmail(email).isPresent()) {
            throw new Exception400("같은 이메일로 가입된 계정이 있습니다. 아이디로 로그인해주세요.");
        }

        User newUser = User.builder()
                .username(uniqueUsername("kakao_" + kakaoId))
                .password(passwordEncoder.encode(UUID.randomUUID().toString())) // 비밀번호 로그인 불가: 카카오로만 로그인
                .provider(OAuthProvider.KAKAO)
                .providerId(kakaoId)
                .nickname(profile.getKakaoAccount().getName())
                .email(profile.getKakaoAccount().getEmail())
                .phoneNumber(profile.getKakaoAccount().getPhoneNumber().replace("+82 ", "0"))
                .gender(SocialUtils.convertGender(profile.getKakaoAccount().getGender()))
                .birthday(SocialUtils.convertBirthday(profile.getKakaoAccount().getBirthyear(), profile.getKakaoAccount().getBirthday()))
                .build();

        UserRole userRole = new UserRole(newUser, RoleType.USER);
        newUser.setRole(userRole);
        return userRepository.save(newUser);
    }

    // 일반 회원이 같은 아이디를 이미 쓰고 있으면 뒤에 무작위 문자를 붙인다
    private String uniqueUsername(String base) {
        String username = base;
        while (userRepository.findByUsername(username).isPresent()) {
            username = base + "_" + UUID.randomUUID().toString().substring(0, 4);
        }
        return username;
    }


    // 회원정보 조회

    // 로그인용 사용자 조회

    // 유저 전체 조회(권한 포함)
    public List<UserResponse.UserList> userList () {
        List<User> userList = userRepository.findAll();

        return userList.stream()
                .map(UserResponse.UserList::new).toList();
    }

    public List<UserResponse.UserList> topTenList() {
        List<User> userList = userRepository.findTopUsers(TOP_USERS_COUNT);

        return userList.stream()
                .map(UserResponse.UserList::new).toList();
    }


    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception404("사용자 없음"));
        cartRepository.deleteByUserId(user.getId());
        userRepository.deleteUserById(user.getId());
    }



    // 유저 권한 회수
}
