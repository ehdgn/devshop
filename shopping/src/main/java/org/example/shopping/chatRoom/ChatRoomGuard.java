package org.example.shopping.chatRoom;

import lombok.RequiredArgsConstructor;
import org.example.shopping.users.User;
import org.springframework.stereotype.Component;

// 채팅방 접근 권한: 관리자이거나 그 방의 주인(입점사 회원)만 구독·전송할 수 있다
@Component
@RequiredArgsConstructor
public class ChatRoomGuard {
    private final ChatRoomRepository chatRoomRepository;

    public boolean canAccess(User user, Long chatRoomId) {
        if (user == null || chatRoomId == null) {
            return false;
        }
        if (user.isAdmin()) {
            return chatRoomRepository.existsById(chatRoomId);
        }
        return chatRoomRepository.findById(chatRoomId)
                .map(room -> user.getId().equals(room.getUserId()))
                .orElse(false);
    }
}
