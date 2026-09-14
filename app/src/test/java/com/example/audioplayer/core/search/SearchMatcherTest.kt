package com.example.audioplayer.core.search

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SearchMatcherTest {
    @Test
    fun matches_isCaseInsensitiveAndSearchesMultipleFields() {
        assertThat(SearchMatcher.matches("jay 夜曲", "Jay Chou", "叶惠美", "夜曲.mp3")).isTrue()
        assertThat(SearchMatcher.matches("MUSIC", "music", null, null)).isTrue()
    }

    @Test
    fun matches_requiresAllTermsToAppear() {
        assertThat(SearchMatcher.matches("周杰伦 稻香", "周杰伦", "魔杰座", "稻香.mp3")).isTrue()
        assertThat(SearchMatcher.matches("周杰伦 晴天", "周杰伦", "叶惠美", "夜曲.mp3")).isFalse()
    }

    @Test
    fun matches_emptyQueryReturnsEverything() {
        assertThat(SearchMatcher.matches("   ", "anything")).isTrue()
    }
}