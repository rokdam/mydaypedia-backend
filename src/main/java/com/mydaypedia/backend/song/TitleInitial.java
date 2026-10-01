package com.mydaypedia.backend.song;

/**
 * 곡 제목의 색인 글자 (가나다 색인 필터용). 사전 색인처럼:
 * 한글은 첫 음절의 초성(쌍자음은 기본 자음으로 묶음: ㄲ→ㄱ, ㄸ→ㄷ, ㅃ→ㅂ, ㅆ→ㅅ, ㅉ→ㅈ),
 * 영문은 대문자 한 글자, 그 밖(숫자·일본어·기호 등)은 '#'.
 * 제목 앞의 괄호·따옴표 같은 기호는 건너뛰고 첫 글자를 본다.
 */
final class TitleInitial {

    static final String OTHER = "#";

    /** 초성 19개 → 색인 14개 (쌍자음은 앞의 기본 자음으로) */
    private static final String[] CHOSEONG = {
            "ㄱ", "ㄱ", "ㄴ", "ㄷ", "ㄷ", "ㄹ", "ㅁ", "ㅂ", "ㅂ", "ㅅ", "ㅅ", "ㅇ", "ㅈ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
    };
    private static final int HANGUL_FIRST = 0xAC00;
    private static final int HANGUL_LAST = 0xD7A3;
    private static final int SYLLABLES_PER_CHOSEONG = 21 * 28;

    private TitleInitial() {
    }

    static String of(String title) {
        if (title == null) {
            return OTHER;
        }
        for (int i = 0; i < title.length(); ) {
            int cp = title.codePointAt(i);
            i += Character.charCount(cp);
            if (cp >= HANGUL_FIRST && cp <= HANGUL_LAST) {
                return CHOSEONG[(cp - HANGUL_FIRST) / SYLLABLES_PER_CHOSEONG];
            }
            if ((cp >= 'A' && cp <= 'Z') || (cp >= 'a' && cp <= 'z')) {
                return String.valueOf((char) Character.toUpperCase(cp));
            }
            if (Character.isLetterOrDigit(cp)) {
                return OTHER; // 숫자, 일본어·태국어 등
            }
            // 공백·괄호·따옴표 등 기호는 건너뛴다
        }
        return OTHER;
    }
}
