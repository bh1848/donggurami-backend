package com.USWCicrcleLink.server.global.util.validator;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.TextException;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class InputValidator {

    /**
     * 내용을 검증하고, XSS 공격을 방지하기 위해 HTML 태그를 정제
     * 링크, 글씨체 태그 허용
     */
    public static String sanitizeContent(String content) {

        // XSS 방지를 위해 허용할 태그와 속성을 설정
        Safelist safelist = Safelist.none() // 기본적으로 모든 태그를 금지
                .addTags("a", "b", "strong", "i", "em", "u", "ul", "ol", "li", "p", "br")  // 허용할 태그들
                .addAttributes("a", "href");  // a 태그에서 href 속성만 허용

        // Jsoup을 통해 XSS 방지 및 허용 태그 이외의 모든 태그 제거
        String sanitizedContent = Jsoup.clean(content, safelist);

        // 정제 후 내용이 비어있지 않은지 확인
        if (sanitizedContent.trim().isEmpty()) {
            throw new TextException(ExceptionType.TEXT_IS_EMPTY);
        }

        return sanitizedContent;
    }

}
