package org.example.shopping._core.utils;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

// 에디터로 작성한 HTML 을 화면에 그대로({{{ }}}) 출력하므로, 저장 전에 서식 태그만 남기고 스크립트·이벤트 속성 등은 제거한다
public class HtmlSanitizer {

    // relaxed: 제목·목록·표·이미지·링크 등 서식 태그 허용. /images/.. 같은 상대 경로 이미지도 유지
    private static final Safelist SAFELIST = Safelist.relaxed().preserveRelativeLinks(true);

    // 상대 경로를 판별하는 데만 쓰는 가상 주소 (결과에 붙지 않음)
    private static final String BASE_URI = "http://localhost";

    private HtmlSanitizer() {
    }

    public static String clean(String html) {
        if (html == null) {
            return null;
        }
        return Jsoup.clean(html, BASE_URI, SAFELIST);
    }
}
