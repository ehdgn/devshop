package org.example.shopping.payment;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.shopping.payment.dto.PaymentRequest;
import org.example.shopping.payment.dto.PaymentResponse;
import org.example.shopping.payment.service.PaymentService;
import org.example.shopping.users.User;
import org.springframework.core.env.Environment;
import org.example.shopping.payment.paymentEnum.RefundStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Controller
public class PaymentController {


    private final PaymentService paymentService;
    private final Environment env;

    // 결제 생성
    @GetMapping("/payment")
    public String createPaymentForm(
            HttpSession session,
            Long cartId,
            Model model
    ) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (cartId != null) {
            PaymentResponse.CartPaymentDTO cartPayment = paymentService.getCartInfo(cartId, sessionUser.getId());
            model.addAttribute("cartPayment", cartPayment);
            model.addAttribute("cartId", cartId);
        }
        String clientKey = env.getProperty("payment.toss.client-key");
        String baseUrl = "http://localhost:8080";
        model.addAttribute("tossClientKey", clientKey);
        model.addAttribute("baseUrl", baseUrl);
        return "payment/payment-form";
    }
    // ============================================================================================================

    // MOCK 결제
    @PostMapping("/payment/cart/{cartId}")
    public String createPayment(PaymentRequest.CreateDTO createDTO, HttpSession session, @PathVariable Long cartId) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        paymentService.createPayment(createDTO, sessionUser, cartId);
        return "redirect:/order/list"; // 모의 결제도 결과를 바로 볼 수 있게 주문 내역으로

    }

    // ============================================================================================================

    // TOSS 결제
    @GetMapping("/payment/cart/{cartId}/approve")
    public String approvePaymentProc(HttpSession session, @PathVariable Long cartId, PaymentRequest.CreateDTO createDTO, Model model) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        try {
            PaymentResponse.PaymentResultDTO result = paymentService.approvePayment(sessionUser, cartId, createDTO);
            model.addAttribute("orderId", createDTO.getOrderId());
            model.addAttribute("amount", createDTO.getAmount());
            model.addAttribute("method", createDTO.getMethod());
            model.addAttribute("paymentKey", createDTO.getPaymentKey());
            model.addAttribute("items", result.getItems());
            return "payment/payment-success";

        } catch (Exception e) {
            model.addAttribute("message", e.getMessage());
            return "payment/payment-fail";
        }
    }

    @GetMapping("/payment/cart/{cartId}/fail")
    public String failPaymentForm(@PathVariable Long cartId, String code, String message, Model model) {
        model.addAttribute("message", message);
        model.addAttribute("code", code);
        model.addAttribute("cartId", cartId);
        return "payment/payment-fail";

    }

    //==============================================================================================================================

    // 주문 상품 단위 환불 (결제는 부분취소)
    @PostMapping("/order/items/{id}/refund")
    @ResponseBody
    public ResponseEntity<PaymentResponse.SingleRefundDTO> refund(@PathVariable(name = "id") Long orderItemId,
                                                                  HttpSession session,
                                                                  @RequestBody PaymentRequest.RefundDTO req) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        PaymentResponse.SingleRefundDTO response = paymentService.refund(orderItemId, sessionUser.getId(), req);
        // PG 환불 실패는 기록은 남기되 화면에는 실패로 알려야 한다
        if (response.getStatus() == RefundStatus.FAILED) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
        }
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/order/items/{id}/confirm")
    @ResponseBody
    public ResponseEntity<Void> confirmPurchase(@PathVariable(name = "id") Long orderItemId,
                                             HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        paymentService.confirmPurchase(orderItemId, sessionUser.getId());
        return ResponseEntity.ok().build();
    }

}
