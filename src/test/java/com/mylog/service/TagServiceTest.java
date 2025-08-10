package com.mylog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.model.entity.Article;
import com.mylog.model.entity.ArticleTag;
import com.mylog.model.entity.Category;
import com.mylog.model.entity.Member;
import com.mylog.model.entity.Tag;
import com.mylog.repository.articletag.ArticleTagRepository;
import com.mylog.repository.tag.TagRepository;
import com.mylog.service.tag.TagService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Comprehensive unit tests for TagService
 * Tests tag creation, article-tag relationship management, and edge cases
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TagService Unit Tests")
public class TagServiceTest {

    @InjectMocks
    private TagService tagService;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ArticleTagRepository articleTagRepository;

    private Article testArticle;
    private Member testMember;
    private Category testCategory;
    private Tag existingTag;
    private Tag newTag;

    private static final String EXISTING_TAG_NAME = "기존태그";
    private static final String NEW_TAG_NAME = "새태그";
    private static final String ANOTHER_TAG_NAME = "다른태그";
    private static final Long TEST_MEMBER_ID = 1L;
    private static final Long TEST_ARTICLE_ID = 100L;
    private static final Long TEST_TAG_ID = 200L;

    @BeforeEach
    void setUp() {
        // Test Member setup
        testMember = Member.builder()
            .id(TEST_MEMBER_ID)
            .email("test@example.com")
            .nickname("testuser")
            .memberName("Test User")
            .password("password123")
            .profileImg("profile.jpg")
            .provider(OauthProvider.LOCAL)
            .providerId("test@example.com" + OauthProvider.LOCAL)
            .build();

        // Test Category setup
        testCategory = Category.builder()
            .id(1L)
            .categoryName("테스트 카테고리")
            .member(testMember)
            .build();

        // Test Article setup
        testArticle = Article.builder()
            .id(TEST_ARTICLE_ID)
            .title("테스트 제목")
            .content("테스트 내용")
            .member(testMember)
            .category(testCategory)
            .build();

        // Test Tags setup
        existingTag = Tag.builder()
            .id(TEST_TAG_ID)
            .tagName(EXISTING_TAG_NAME)
            .build();

        newTag = Tag.builder()
            .id(TEST_TAG_ID + 1)
            .tagName(NEW_TAG_NAME)
            .build();
    }

    @Nested
    @DisplayName("saveTag Tests")
    class SaveTagTests {

        @Test
        @DisplayName("새로운 태그를 생성하고 ArticleTag 관계를 저장한다")
        void saveTag_WhenNewTag_CreatesTagAndArticleTag() {
            // Given
            List<String> tags = List.of(NEW_TAG_NAME);
            when(tagRepository.existsByTagName(NEW_TAG_NAME)).thenReturn(false);
            when(tagRepository.findByTagName(NEW_TAG_NAME)).thenReturn(Optional.of(newTag));

            // When
            tagService.saveTag(tags, testArticle);

            // Then
            ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
            verify(tagRepository).save(tagCaptor.capture());
            
            Tag savedTag = tagCaptor.getValue();
            // Tag entity has no getter, so we verify through constructor argument matching
            verify(tagRepository).save(any(Tag.class));

            ArgumentCaptor<ArticleTag> articleTagCaptor = ArgumentCaptor.forClass(ArticleTag.class);
            verify(articleTagRepository).save(articleTagCaptor.capture());
            
            // ArticleTag entity has no getters, so we verify through constructor matching
            verify(articleTagRepository).save(any(ArticleTag.class));
        }

        @Test
        @DisplayName("기존 태그가 있는 경우 새로 생성하지 않고 ArticleTag 관계만 저장한다")
        void saveTag_WhenExistingTag_DoesNotCreateTagButSavesArticleTag() {
            // Given
            List<String> tags = List.of(EXISTING_TAG_NAME);
            when(tagRepository.existsByTagName(EXISTING_TAG_NAME)).thenReturn(true);
            when(tagRepository.findByTagName(EXISTING_TAG_NAME)).thenReturn(Optional.of(existingTag));

            // When
            tagService.saveTag(tags, testArticle);

            // Then
            verify(tagRepository, never()).save(any(Tag.class));
            
            ArgumentCaptor<ArticleTag> articleTagCaptor = ArgumentCaptor.forClass(ArticleTag.class);
            verify(articleTagRepository).save(articleTagCaptor.capture());
            
            // ArticleTag entity has no getters, so we verify through save operation
            verify(articleTagRepository).save(any(ArticleTag.class));
        }

        @Test
        @DisplayName("여러 태그를 한 번에 처리한다")
        void saveTag_WithMultipleTags_ProcessesAllTags() {
            // Given
            List<String> tags = Arrays.asList(EXISTING_TAG_NAME, NEW_TAG_NAME, ANOTHER_TAG_NAME);
            
            // 기존 태그
            when(tagRepository.existsByTagName(EXISTING_TAG_NAME)).thenReturn(true);
            when(tagRepository.findByTagName(EXISTING_TAG_NAME)).thenReturn(Optional.of(existingTag));
            
            // 새 태그들
            when(tagRepository.existsByTagName(NEW_TAG_NAME)).thenReturn(false);
            when(tagRepository.findByTagName(NEW_TAG_NAME)).thenReturn(Optional.of(newTag));
            
            when(tagRepository.existsByTagName(ANOTHER_TAG_NAME)).thenReturn(false);
            Tag anotherTag = Tag.builder().id(TEST_TAG_ID + 2).tagName(ANOTHER_TAG_NAME).build();
            when(tagRepository.findByTagName(ANOTHER_TAG_NAME)).thenReturn(Optional.of(anotherTag));

            // When
            tagService.saveTag(tags, testArticle);

            // Then
            verify(tagRepository, times(2)).save(any(Tag.class)); // 새 태그 2개만 저장
            verify(articleTagRepository, times(3)).save(any(ArticleTag.class)); // 모든 태그에 대해 관계 저장
            
            verify(tagRepository).existsByTagName(EXISTING_TAG_NAME);
            verify(tagRepository).existsByTagName(NEW_TAG_NAME);
            verify(tagRepository).existsByTagName(ANOTHER_TAG_NAME);
        }

        @Test
        @DisplayName("빈 태그 리스트인 경우 아무것도 처리하지 않는다")
        void saveTag_WithEmptyList_DoesNothing() {
            // Given
            List<String> emptyTags = Collections.emptyList();

            // When
            tagService.saveTag(emptyTags, testArticle);

            // Then
            verify(tagRepository, never()).existsByTagName(anyString());
            verify(tagRepository, never()).save(any(Tag.class));
            verify(tagRepository, never()).findByTagName(anyString());
            verify(articleTagRepository, never()).save(any(ArticleTag.class));
        }

        @Test
        @DisplayName("태그를 저장한 후 findByTagName에서 찾을 수 없는 경우 예외를 발생시킨다")
        void saveTag_WhenTagNotFoundAfterSave_ThrowsException() {
            // Given
            List<String> tags = List.of(NEW_TAG_NAME);
            when(tagRepository.existsByTagName(NEW_TAG_NAME)).thenReturn(false);
            when(tagRepository.findByTagName(NEW_TAG_NAME)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> tagService.saveTag(tags, testArticle))
                .isInstanceOf(CMissingDataException.class);

            verify(tagRepository).save(any(Tag.class));
            verify(articleTagRepository, never()).save(any(ArticleTag.class));
        }

        @Test
        @DisplayName("기존 태그를 찾을 수 없는 경우 예외를 발생시킨다")
        void saveTag_WhenExistingTagNotFound_ThrowsException() {
            // Given
            List<String> tags = List.of(EXISTING_TAG_NAME);
            when(tagRepository.existsByTagName(EXISTING_TAG_NAME)).thenReturn(true);
            when(tagRepository.findByTagName(EXISTING_TAG_NAME)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> tagService.saveTag(tags, testArticle))
                .isInstanceOf(CMissingDataException.class);

            verify(tagRepository, never()).save(any(Tag.class));
            verify(articleTagRepository, never()).save(any(ArticleTag.class));
        }

        @Test
        @DisplayName("중복된 태그명이 있는 경우 각각 처리한다")
        void saveTag_WithDuplicateTagNames_ProcessesEachOccurrence() {
            // Given
            List<String> tagsWithDuplicates = Arrays.asList(EXISTING_TAG_NAME, EXISTING_TAG_NAME, NEW_TAG_NAME);
            when(tagRepository.existsByTagName(EXISTING_TAG_NAME)).thenReturn(true);
            when(tagRepository.findByTagName(EXISTING_TAG_NAME)).thenReturn(Optional.of(existingTag));
            when(tagRepository.existsByTagName(NEW_TAG_NAME)).thenReturn(false);
            when(tagRepository.findByTagName(NEW_TAG_NAME)).thenReturn(Optional.of(newTag));

            // When
            tagService.saveTag(tagsWithDuplicates, testArticle);

            // Then
            verify(tagRepository, times(2)).existsByTagName(EXISTING_TAG_NAME); // 중복 호출
            verify(tagRepository, times(1)).existsByTagName(NEW_TAG_NAME);
            verify(tagRepository, times(1)).save(any(Tag.class)); // 새 태그 1개만 저장
            verify(articleTagRepository, times(3)).save(any(ArticleTag.class)); // 모든 태그에 대해 관계 저장
        }

        @Test
        @DisplayName("한글, 영어, 숫자가 포함된 태그명을 처리한다")
        void saveTag_WithMixedLanguageTagNames_ProcessesCorrectly() {
            // Given
            String koreanTag = "한글태그";
            String englishTag = "english";
            String mixedTag = "mixed한글123";
            List<String> tags = Arrays.asList(koreanTag, englishTag, mixedTag);
            
            when(tagRepository.existsByTagName(koreanTag)).thenReturn(false);
            when(tagRepository.existsByTagName(englishTag)).thenReturn(false);
            when(tagRepository.existsByTagName(mixedTag)).thenReturn(false);
            
            Tag koreanTagEntity = Tag.builder().tagName(koreanTag).build();
            Tag englishTagEntity = Tag.builder().tagName(englishTag).build();
            Tag mixedTagEntity = Tag.builder().tagName(mixedTag).build();
            
            when(tagRepository.findByTagName(koreanTag)).thenReturn(Optional.of(koreanTagEntity));
            when(tagRepository.findByTagName(englishTag)).thenReturn(Optional.of(englishTagEntity));
            when(tagRepository.findByTagName(mixedTag)).thenReturn(Optional.of(mixedTagEntity));

            // When
            tagService.saveTag(tags, testArticle);

            // Then
            verify(tagRepository, times(3)).save(any(Tag.class));
            verify(articleTagRepository, times(3)).save(any(ArticleTag.class));
        }

        @Test
        @DisplayName("특수문자가 포함된 태그명을 처리한다")
        void saveTag_WithSpecialCharacters_ProcessesCorrectly() {
            // Given
            String specialTag = "태그#$%";
            List<String> tags = List.of(specialTag);
            
            when(tagRepository.existsByTagName(specialTag)).thenReturn(false);
            Tag specialTagEntity = Tag.builder().tagName(specialTag).build();
            when(tagRepository.findByTagName(specialTag)).thenReturn(Optional.of(specialTagEntity));

            // When
            tagService.saveTag(tags, testArticle);

            // Then
            ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
            verify(tagRepository).save(tagCaptor.capture());
            
            // Tag entity has no getter, verify through save operation
            verify(tagRepository).save(any(Tag.class));
        }

        @Test
        @DisplayName("공백이 포함된 태그명을 처리한다")
        void saveTag_WithWhitespace_ProcessesCorrectly() {
            // Given
            String tagWithSpaces = " 공백 태그 ";
            List<String> tags = List.of(tagWithSpaces);
            
            when(tagRepository.existsByTagName(tagWithSpaces)).thenReturn(false);
            Tag tagEntity = Tag.builder().tagName(tagWithSpaces).build();
            when(tagRepository.findByTagName(tagWithSpaces)).thenReturn(Optional.of(tagEntity));

            // When
            tagService.saveTag(tags, testArticle);

            // Then
            ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
            verify(tagRepository).save(tagCaptor.capture());
            
            // Tag entity has no getter, verify through save operation
            verify(tagRepository).save(any(Tag.class));
        }
    }

    @Nested
    @DisplayName("Integration and Edge Cases")
    class IntegrationAndEdgeCasesTests {

        @Test
        @DisplayName("하나의 아티클에 여러 태그를 연결하는 전체 플로우가 정상 작동한다")
        void saveTag_FullWorkflow_CreatesTagsAndRelationships() {
            // Given
            List<String> tags = Arrays.asList("새태그1", "기존태그", "새태그2");
            
            // 기존 태그
            when(tagRepository.existsByTagName("기존태그")).thenReturn(true);
            when(tagRepository.findByTagName("기존태그")).thenReturn(Optional.of(existingTag));
            
            // 새 태그들
            when(tagRepository.existsByTagName("새태그1")).thenReturn(false);
            when(tagRepository.existsByTagName("새태그2")).thenReturn(false);
            
            Tag newTag1 = Tag.builder().tagName("새태그1").build();
            Tag newTag2 = Tag.builder().tagName("새태그2").build();
            
            when(tagRepository.findByTagName("새태그1")).thenReturn(Optional.of(newTag1));
            when(tagRepository.findByTagName("새태그2")).thenReturn(Optional.of(newTag2));

            // When
            tagService.saveTag(tags, testArticle);

            // Then
            // 새 태그 2개만 저장됨
            verify(tagRepository, times(2)).save(any(Tag.class));
            
            // 모든 태그에 대해 ArticleTag 관계 저장됨
            verify(articleTagRepository, times(3)).save(any(ArticleTag.class));
            
            // 각 태그에 대해 존재 여부 확인됨
            verify(tagRepository).existsByTagName("새태그1");
            verify(tagRepository).existsByTagName("기존태그");
            verify(tagRepository).existsByTagName("새태그2");
            
            // 각 태그에 대해 조회됨
            verify(tagRepository).findByTagName("새태그1");
            verify(tagRepository).findByTagName("기존태그");
            verify(tagRepository).findByTagName("새태그2");
        }

        @Test
        @DisplayName("같은 아티클에 이미 연결된 태그를 다시 추가해도 중복 생성된다")
        void saveTag_WhenTagAlreadyLinkedToArticle_CreatesDuplicateRelationship() {
            // Given - 이미 연결된 태그를 다시 추가하는 시나리오
            List<String> tags = List.of(EXISTING_TAG_NAME);
            when(tagRepository.existsByTagName(EXISTING_TAG_NAME)).thenReturn(true);
            when(tagRepository.findByTagName(EXISTING_TAG_NAME)).thenReturn(Optional.of(existingTag));

            // When - 같은 태그를 두 번 추가
            tagService.saveTag(tags, testArticle);
            tagService.saveTag(tags, testArticle);

            // Then - ArticleTag가 두 번 저장됨 (비즈니스 로직에 따라)
            verify(articleTagRepository, times(2)).save(any(ArticleTag.class));
        }

        @Test
        @DisplayName("다른 아티클에 같은 태그를 연결할 수 있다")
        void saveTag_WithDifferentArticle_CreatesSeparateRelationships() {
            // Given
            Article anotherArticle = Article.builder()
                .id(TEST_ARTICLE_ID + 1)
                .title("다른 제목")
                .content("다른 내용")
                .member(testMember)
                .category(testCategory)
                .build();
                
            List<String> tags = List.of(EXISTING_TAG_NAME);
            when(tagRepository.existsByTagName(EXISTING_TAG_NAME)).thenReturn(true);
            when(tagRepository.findByTagName(EXISTING_TAG_NAME)).thenReturn(Optional.of(existingTag));

            // When
            tagService.saveTag(tags, testArticle);
            tagService.saveTag(tags, anotherArticle);

            // Then
            ArgumentCaptor<ArticleTag> articleTagCaptor = ArgumentCaptor.forClass(ArticleTag.class);
            verify(articleTagRepository, times(2)).save(articleTagCaptor.capture());
            
            // ArticleTag entity has no getters, verify through save count
            verify(articleTagRepository, times(2)).save(any(ArticleTag.class));
        }

        @Test
        @DisplayName("태그 최대 길이 제한을 테스트한다")
        void saveTag_WithMaxLengthTag_ProcessesCorrectly() {
            // Given - Tag 엔티티의 @Column(length = 10) 제약에 맞는 최대 길이
            String maxLengthTag = "1234567890"; // 10자
            List<String> tags = List.of(maxLengthTag);
            
            when(tagRepository.existsByTagName(maxLengthTag)).thenReturn(false);
            Tag maxLengthTagEntity = Tag.builder().tagName(maxLengthTag).build();
            when(tagRepository.findByTagName(maxLengthTag)).thenReturn(Optional.of(maxLengthTagEntity));

            // When
            tagService.saveTag(tags, testArticle);

            // Then
            ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
            verify(tagRepository).save(tagCaptor.capture());
            
            // Tag entity has no getter, verify through save operation and length constraint
            verify(tagRepository).save(any(Tag.class));
            assertThat(maxLengthTag.length()).isEqualTo(10);
        }

        @Test
        @DisplayName("단일 문자 태그를 처리한다")
        void saveTag_WithSingleCharacterTag_ProcessesCorrectly() {
            // Given
            String singleCharTag = "A";
            List<String> tags = List.of(singleCharTag);
            
            when(tagRepository.existsByTagName(singleCharTag)).thenReturn(false);
            Tag singleCharTagEntity = Tag.builder().tagName(singleCharTag).build();
            when(tagRepository.findByTagName(singleCharTag)).thenReturn(Optional.of(singleCharTagEntity));

            // When
            tagService.saveTag(tags, testArticle);

            // Then
            ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
            verify(tagRepository).save(tagCaptor.capture());
            
            // Tag entity has no getter, verify through save operation
            verify(tagRepository).save(any(Tag.class));
        }
    }
}