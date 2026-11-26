package org.example.shopping.chatRoom;

import org.example.shopping.users.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomGuardTest {

    @Mock ChatRoomRepository chatRoomRepository;
    @InjectMocks ChatRoomGuard guard;

    private User user(Long id, boolean admin) {
        User user = spy(User.builder().id(id).build());
        lenient().doReturn(admin).when(user).isAdmin();
        return user;
    }

    @Test
    void 방_주인은_접근할_수_있다() {
        when(chatRoomRepository.findById(7L)).thenReturn(Optional.of(ChatRoom.builder().userId(2L).build()));
        assertTrue(guard.canAccess(user(2L, false), 7L));
    }

    @Test
    void 다른_입점사는_접근할_수_없다() {
        when(chatRoomRepository.findById(7L)).thenReturn(Optional.of(ChatRoom.builder().userId(2L).build()));
        assertFalse(guard.canAccess(user(4L, false), 7L));
    }

    @Test
    void 관리자는_있는_방에_접근할_수_있다() {
        when(chatRoomRepository.existsById(7L)).thenReturn(true);
        assertTrue(guard.canAccess(user(1L, true), 7L));
    }

    @Test
    void 비로그인이나_방번호가_없으면_거부한다() {
        assertFalse(guard.canAccess(null, 7L));
        assertFalse(guard.canAccess(user(2L, false), null));
        verifyNoInteractions(chatRoomRepository);
    }
}
