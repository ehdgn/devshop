package org.example.shopping._core.errors;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.shopping._core.errors.exception.Exception400;
import org.example.shopping._core.errors.exception.Exception401;
import org.example.shopping._core.errors.exception.Exception403;
import org.example.shopping._core.errors.exception.Exception404;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;


// 오류는 항상 실제 HTTP 상태코드로 응답한다: fetch 는 response.ok 로 성공 여부를 판단하기 때문
// Accept: application/json 요청에는 {"message": ...} JSON, 그 외에는 alert 페이지
@ControllerAdvice
@Slf4j
public class MyExceptionHandler {

    @ExceptionHandler(Exception400.class)
    public Object ex400(Exception400 e, HttpServletRequest request) {
        return respond(request, HttpStatus.BAD_REQUEST, e.getMessage(), null);
    }

    @ExceptionHandler(Exception401.class)
    public Object ex401(Exception401 e, HttpServletRequest request) {
        return respond(request, HttpStatus.UNAUTHORIZED, e.getMessage(), "/user/login"); // 로그인 폼으로 유도
    }

    @ExceptionHandler(Exception403.class)
    public Object ex403(Exception403 e, HttpServletRequest request) {
        return respond(request, HttpStatus.FORBIDDEN, e.getMessage(), null);
    }

    @ExceptionHandler(Exception404.class)
    public Object ex404(Exception404 e, HttpServletRequest request) {
        return respond(request, HttpStatus.NOT_FOUND, e.getMessage(), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Object handleDataViolationException(DataIntegrityViolationException e, HttpServletRequest request) {
        String errorMessage = e.getMessage();
        String displayMsg = "데이터베이스 오류가 발생했습니다.";

        if(errorMessage != null && errorMessage.contains("FOREIGN KEY")) {
            displayMsg = "관련된 데이터가 존재하여 삭제하거나 수정할 수 없습니다.";
        }

        return respond(request, HttpStatus.CONFLICT, displayMsg, null);
    }

    @ExceptionHandler(RuntimeException.class)
    public Object handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("예상치 못한 에러: {}", e.getMessage(), e);
        return respond(request, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다: " + e.getMessage(), null);
    }

    private Object respond(HttpServletRequest request, HttpStatus status, String message, String url) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            return ResponseEntity.status(status)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("message", message == null ? "" : message));
        }

        ModelAndView mav = new ModelAndView("alert");
        mav.addObject("alertMessage", message);
        if (url != null) {
            mav.addObject("url", url);
        }
        mav.setStatus(status);
        return mav;
    }
}
