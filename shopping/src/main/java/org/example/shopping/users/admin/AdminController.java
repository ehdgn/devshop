package org.example.shopping.users.admin;


import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.shopping.chat.ChatService;
import org.example.shopping.payment.log.HistoryResponse;
import org.example.shopping.product.ProductService;
import org.example.shopping.payment.log.HistoryService;
import org.example.shopping.users.User;
import org.example.shopping.users.dto.UserResponse;
import org.example.shopping.users.owner.OwnerService;
import org.example.shopping.users.owner.dto.OwnerResponse;
import org.example.shopping.users.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final OwnerService ownerService;
    private final HistoryService historyService;
    private final ProductService productService;
    private final ChatService chatService;

    @GetMapping("/admin/main")
    public String dashboard(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        // 최근 10명 최신순 조회
        List<UserResponse.UserList> recentList = userService.topTenList();
        List<OwnerResponse.OwnerList> owners = ownerService.ownerList();
        model.addAttribute("recentUsers", recentList);
        model.addAttribute("user", sessionUser);
        // shortcut: 목록을 통째로 불러와 센다, 데이터가 많아지면 count 쿼리로 바꾸기
        model.addAttribute("userCount", userService.userList().size());
        model.addAttribute("waitOwnerCount", owners.stream().filter(OwnerResponse.OwnerList::isWAIT).count());
        model.addAttribute("waitOwners", owners.stream().filter(OwnerResponse.OwnerList::isWAIT).toList());
        model.addAttribute("productCount", productService.findAllForAdmin().size());
        model.addAttribute("chatCount", chatService.findAll().size());
        return "user/admin/main";
    }

    @GetMapping("/admin/list")
    public String userList(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        List<UserResponse.UserList> userList = userService.userList();
        model.addAttribute("userList", userList);

        return "user/admin/user-list";
    }



    // 회원삭제
    @PostMapping("/admin/user/{userId}/delete")
    public String deleteUser(HttpSession session,@PathVariable Long userId){
        User sessionUser= (User) session.getAttribute("sessionUser");
        userService.deleteUser(userId);
        return "redirect:/admin/list";
    }


    // owner 전체 조회
    @GetMapping("/admin/owner/list")
    public String ownerList(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        List<OwnerResponse.OwnerList> ownerList = ownerService.ownerList();
        model.addAttribute("ownerList", ownerList);

        return "/user/admin/owner-list";
    }

    // 입점사 승인상태변경
    @PostMapping("/admin/owner/{ownerId}/approve")
    public String approveStatus(HttpSession session, @PathVariable Long ownerId) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        ownerService.approveStatus(ownerId);

        return "redirect:/admin/owner/list";
    }
    // 입점사 정지상태변경
    @PostMapping("/admin/owner/{ownerId}/suspension")
    public String suspensionStatus(HttpSession session, @PathVariable Long ownerId) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        ownerService.suspensionStatus(ownerId);

        return "redirect:/admin/owner/list";
    }

    // 입점사 상세 조회
    @GetMapping("/admin/owner/{ownerId}/detail")
    @ResponseBody
    public OwnerResponse.OwnerDetail ownerDetail(@PathVariable Long ownerId) {
        return ownerService.ownerDetail(ownerId);
    }

    // statistics 페이지 불러오기
    @GetMapping("/admin/statistics")
    public String statistics(HttpSession session, Model model){
        User sessionUser = (User) session.getAttribute("sessionUser");
        List<HistoryResponse.ListDTO> list = historyService.findAll();
        model.addAttribute("historyList", list);

        return "user/admin/statistics";
    }

}
