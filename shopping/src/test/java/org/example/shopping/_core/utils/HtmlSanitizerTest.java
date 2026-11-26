package org.example.shopping._core.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HtmlSanitizerTest {

    @Test
    void 스크립트와_이벤트_속성과_javascript_링크를_제거한다() {
        String dirty = "<p>안녕<script>alert(1)</script></p>"
                + "<img src=\"/images/a.png\" onerror=\"alert(2)\">"
                + "<a href=\"javascript:alert(3)\">링크</a>"
                + "<iframe src=\"https://evil.example\"></iframe>";

        String clean = HtmlSanitizer.clean(dirty);

        assertFalse(clean.contains("<script"), clean);
        assertFalse(clean.contains("onerror"), clean);
        assertFalse(clean.contains("javascript:"), clean);
        assertFalse(clean.contains("<iframe"), clean);
    }

    @Test
    void 에디터_서식과_업로드_이미지는_남긴다() {
        String html = "<h2>제목</h2><p><strong>굵게</strong></p><ul><li>항목</li></ul>"
                + "<img src=\"/images/a.png\" alt=\"상품\">";

        String clean = HtmlSanitizer.clean(html);

        assertTrue(clean.contains("<h2>제목</h2>"), clean);
        assertTrue(clean.contains("<strong>굵게</strong>"), clean);
        assertTrue(clean.contains("<li>항목</li>"), clean);
        assertTrue(clean.contains("src=\"/images/a.png\""), clean);
    }
}
