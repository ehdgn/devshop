package org.example.shopping.chat;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shopping.chatRoom.ChatRoomGuard;
import org.example.shopping.users.User;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final ChatRoomGuard chatRoomGuard;


    // 내 채팅방 조회
    @GetMapping("/owner/chatRoom/me")
    public String findMyChatRoom(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("sessionUser");

        ChatResponse.ChatRoomDTO myChatRoom = chatService.findMyChatRoom(sessionUser.getId());

        String myRole = sessionUser.getRole().getRole().toString();

        List<ChatResponse.ChatListDTO> chatList = myChatRoom.getChatList().stream()
                        .map(chat -> new ChatResponse.ChatListDTO(
                                chat.getSender().name(),
                                chat.getMessage(),
                                chat.getSender().toString().equals(myRole)
                        ))
                        .toList();

        model.addAttribute("chatList", chatList);
        model.addAttribute("chatRoomId", myChatRoom.getChatRoomId());
        model.addAttribute("myRole", myRole);
        return "chat/index";
    }

    @PostMapping("/chatRoom/delete")
    public String deleteChatRoom(HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");

        chatService.deleteChatRoom(sessionUser.getId());
        return "redirect:/";
    }

    // 메세지 수신
    @MessageMapping("/chat/message")
    public void receiveMessage(Map<String, Object> payload, SimpMessageHeaderAccessor accessor) {

        Map<String, Object> attrs = accessor.getSessionAttributes();
        User sessionUser = attrs == null ? null : (User) attrs.get("sessionUser");

        Object message = payload.get("message");
        Long chatRoomId = payload.get("chatRoomId") instanceof Number n ? n.longValue() : null;

        if (!(message instanceof String text) || text.isBlank() || !chatRoomGuard.canAccess(sessionUser, chatRoomId)) {
            log.warn("채팅 메시지 거부 chatRoomId={} userId={}", chatRoomId, sessionUser == null ? null : sessionUser.getId());
            return;
        }

        SenderRole sender = sessionUser.isAdmin() ? SenderRole.ADMIN : SenderRole.OWNER;
        chatService.saveAndBroadcast(text, chatRoomId, sender);
    }

}
