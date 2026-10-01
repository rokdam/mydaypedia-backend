package com.mydaypedia.backend.song;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TitleInitialTest {

    @Test
    void 한글은_첫_음절의_초성() {
        assertThat(TitleInitial.of("겨울이 간다")).isEqualTo("ㄱ");
        assertThat(TitleInitial.of("예뻤어")).isEqualTo("ㅇ");
        assertThat(TitleInitial.of("행복했던 날들이었다")).isEqualTo("ㅎ");
        assertThat(TitleInitial.of("녹아내려요")).isEqualTo("ㄴ");
    }

    @Test
    void 쌍자음은_기본_자음으로_묶는다() {
        assertThat(TitleInitial.of("꿈의 버스")).isEqualTo("ㄱ");
        assertThat(TitleInitial.of("땡스 투 (Thanks to)")).isEqualTo("ㄷ");
        assertThat(TitleInitial.of("쏟아진다")).isEqualTo("ㅅ");
        assertThat(TitleInitial.of("짜증나")).isEqualTo("ㅈ");
        assertThat(TitleInitial.of("빠빠빠")).isEqualTo("ㅂ");
    }

    @Test
    void 영문은_대문자_한_글자() {
        assertThat(TitleInitial.of("Zombie")).isEqualTo("Z");
        assertThat(TitleInitial.of("better Better")).isEqualTo("B");
    }

    @Test
    void 숫자와_외국_문자는_기타() {
        assertThat(TitleInitial.of("1 to 10")).isEqualTo(TitleInitial.OTHER);
        assertThat(TitleInitial.of("ใจอ้วน (Sugar High)")).isEqualTo(TitleInitial.OTHER);
        assertThat(TitleInitial.of("")).isEqualTo(TitleInitial.OTHER);
        assertThat(TitleInitial.of(null)).isEqualTo(TitleInitial.OTHER);
    }

    @Test
    void 앞의_기호는_건너뛴다() {
        assertThat(TitleInitial.of("「사랑」")).isEqualTo("ㅅ");
        assertThat(TitleInitial.of("  (Intro) Hi")).isEqualTo("I");
        assertThat(TitleInitial.of("'Congratulations'")).isEqualTo("C");
    }
}
