package org.example.shopping.users;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shopping._core.errors.exception.Exception400;
import org.example.shopping._core.errors.exception.Exception404;
import org.example.shopping._core.errors.exception.Exception500;
import org.example.shopping._core.utils.MailUtils;
import org.example.shopping.order.Order;
import org.example.shopping.order.OrderRepository;
import org.example.shopping.orderItem.OrderItem;
import org.example.shopping.payment.Payment;
import org.example.shopping.payment.PaymentRepository;
import org.example.shopping.users.user.UserRepository;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;
    private final HttpSession session;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public void sendAuthCode(String email) {

        boolean isExistEmail = userRepository.findByEmail(email).isPresent();
        if (isExistEmail) {
            throw new Exception400("이미 가입된 이메일입니다.");
        }

        String code = MailUtils.generatedRandomCode();
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("[DEV-SHOP] 회원가입 이메일 전송");
            helper.setText("<h3>인증 번호는 [" + code + "] 입니다.</h3>", true);

            javaMailSender.send(message);

            session.setAttribute("code_" + email, code);

        } catch (Exception e) {
            log.error("메일 발생 오류: {}", e.getMessage());
            throw new Exception500("서버 문제입니다.");
        }
    }

    public boolean verifyAuthCode(String email, String code) {
        String savedCode = (String) session.getAttribute("code_" + email);

        if (savedCode != null && savedCode.equals(code)) {
            session.removeAttribute("code_" + email);
            return true;
        }
        return false;
    }

    public void sendPayInfo(User user,Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new Exception404("없음"));
        List<OrderItem> items = order.getOrderItems();
        Payment payment = order.getPayment();
        MimeMessage message  = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setSubject("[DEV-SHOP 결제 내역]");

            StringBuilder emailBody = new StringBuilder();
            emailBody.append("<h2>주문 상품 정보</h2>");
            emailBody.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
            emailBody.append("<tr><th>상품명</th><th>수량</th><th>금액</th><th>주문번호</th></tr>");

            for (OrderItem item : items) {
                emailBody.append("<tr>");
                emailBody.append("<td>").append(item.getProductName()).append("</td>");
                emailBody.append("<td>").append(item.getQuantity()).append("</td>");
                emailBody.append("<td>").append(item.getAmount()).append("원</td>");
                emailBody.append("<td>").append(order.getOrderNumber()).append("</td>");
                emailBody.append("</tr>");
            }

            emailBody.append("</table>");
            emailBody.append("<h3>총 결제 금액: ").append(payment.getAmount()).append("원</h3>");
            emailBody.append("<p>결제 방법: ").append(payment.getMethod()).append("</p>");
            emailBody.append("<p>주문 번호: ").append(order.getId()).append("</p>");

            helper.setText(emailBody.toString(), true);
            javaMailSender.send(message);

        } catch (Exception e) {
            // 결제는 이미 승인됨: 메일 실패로 주문 기록까지 롤백되면 안 됨
            log.error("결제 안내 메일 발송 실패 orderId={}", orderId, e);
        }
    }
}
