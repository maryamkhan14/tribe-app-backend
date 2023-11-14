package com.savvato.tribeapp.services;

import com.savvato.tribeapp.constants.Constants;
import com.savvato.tribeapp.dto.PhraseDTO;
import com.savvato.tribeapp.entities.*;
import com.savvato.tribeapp.repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith({SpringExtension.class})
public class PhraseServiceImplTest extends AbstractServiceImplTest {

    @TestConfiguration
    static class PhraseServiceTestContextConfiguration {

        @Bean
        public PhraseService phraseService() {
            return new PhraseServiceImpl();
        }

    }

    @Autowired
    PhraseService phraseService;

    @MockBean
    PhraseRepository phraseRepository;

    @MockBean
    AdverbRepository adverbRepository;

    @MockBean
    VerbRepository verbRepository;

    @MockBean
    PrepositionRepository prepositionRepository;

    @MockBean
    NounRepository nounRepository;

    @MockBean
    RejectedPhraseRepository rejectedPhraseRepository;

    @MockBean
    RejectedNonEnglishWordRepository rejectedNonEnglishWordRepository;

    @MockBean
    UserPhraseService userPhraseService;

    @MockBean
    UserPhraseRepository userPhraseRepository;

    @MockBean
    ToBeReviewedRepository toBeReviewedRepository;

    @MockBean
    ReviewSubmittingUserRepository reviewSubmittingUserRepository;


    @Test
    public void isPhraseValidHappyPath() {
        String adverb = "competitively";
        String verb = "plays";
        String noun = "chess";
        String preposition = "";
        when(rejectedPhraseRepository.findByRejectedPhrase(anyString())).thenReturn(Optional.empty());
        when(rejectedNonEnglishWordRepository.findByWord(anyString())).thenReturn(Optional.empty());
        boolean rtn = phraseService.isPhraseValid(adverb, verb, preposition, noun);
        ArgumentCaptor<String> wordCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> phraseCaptor = ArgumentCaptor.forClass(String.class);

        verify(rejectedNonEnglishWordRepository, times(4)).findByWord(wordCaptor.capture());
        verify(rejectedPhraseRepository, times(1)).findByRejectedPhrase(phraseCaptor.capture());
        assertThat(wordCaptor.getAllValues()).contains(adverb, verb, noun, preposition);
        assertThat(phraseCaptor.getValue()).contains(adverb, verb, noun, preposition);
        assertTrue(rtn);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"          "})
    public void isPhraseValidWhenAdverbOrPrepositionMissing(String value) {
        String adverb = value;
        String verb = "plays";
        String noun = "chess";
        String preposition = value;
        when(rejectedPhraseRepository.findByRejectedPhrase(anyString())).thenReturn(Optional.empty());
        when(rejectedNonEnglishWordRepository.findByWord(anyString())).thenReturn(Optional.empty());
        boolean rtn = phraseService.isPhraseValid(adverb, verb, preposition, noun);
        ArgumentCaptor<String> wordCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> phraseCaptor = ArgumentCaptor.forClass(String.class);

        verify(rejectedNonEnglishWordRepository, times(4)).findByWord(wordCaptor.capture());
        verify(rejectedPhraseRepository, times(1)).findByRejectedPhrase(phraseCaptor.capture());
        assertThat(wordCaptor.getAllValues()).contains(adverb, verb, noun, preposition);
        assertThat(phraseCaptor.getValue()).contains(noun, verb);
        assertTrue(rtn);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"          "})
    public void testIsMissingVerbOrNounWhenVerbMissing(String verb) {
        String adverb = "competitively";
        String noun = "chess";
        String preposition = "";

        assertThrows(IllegalArgumentException.class, () -> {
            phraseService.isPhraseValid(adverb, verb, preposition, noun);
        });
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"          "})
    public void testIsMissingVerbOrNounWhenNounMissing(String noun) {
        String adverb = "competitively";
        String verb = "plays";
        String preposition = "";

        assertThrows(IllegalArgumentException.class, () -> {
            phraseService.isPhraseValid(adverb, verb, preposition, noun);
        });
    }

    // test that false is returned when method is called with a rejected word
    @Test
    public void testIsWordRejectedHappyPath() {

        String rejectedWord = "test";
        RejectedNonEnglishWord rejectedNonEnglishWord = new RejectedNonEnglishWord();
        rejectedNonEnglishWord.setId(1L);
        rejectedNonEnglishWord.setWord(rejectedWord);

        Mockito.when(rejectedNonEnglishWordRepository.findByWord(anyString())).thenReturn(Optional.of(rejectedNonEnglishWord));

        assertFalse(phraseService.isPhraseValid(rejectedWord, rejectedWord, rejectedWord, rejectedWord));
    }

    // test that false is returned when method is called with a rejected phrase
    @Test
    public void testIsPhrasePreviouslyRejectedHappyPath() {

        String rejectedString = "test test test test";
        RejectedPhrase rejectedPhrase = new RejectedPhrase();
        rejectedPhrase.setId(1L);
        rejectedPhrase.setRejectedPhrase(rejectedString);

        Mockito.when(rejectedPhraseRepository.findByRejectedPhrase(anyString())).thenReturn(Optional.of(rejectedPhrase));

        assertFalse(phraseService.isPhraseValid("test", "test", "test", "test"));
    }

    // Test that UserPhraseRepository is called once when calling ApplyPhraseToUser and phrase has been approved
    @Test
    public void testApplyPhraseToUserWhenPhraseHasBeenPreviouslyApproved() {

        User user1 = getUser1();

        Adverb testAdverb = getTestAdverb1();
        Verb testVerb = getTestVerb1();
        Preposition testPreposition = getTestPreposition1();
        Noun testNoun = getTestNoun1();

        Phrase testPhrase = getTestPhrase1();

        UserPhrase userPhrase = new UserPhrase();
        userPhrase.setUserId(user1.getId());
        userPhrase.setPhraseId(testPhrase.getId());

        Mockito.when(adverbRepository.findByWord(anyString())).thenReturn(Optional.of(testAdverb));
        Mockito.when(verbRepository.findByWord(anyString())).thenReturn(Optional.of(testVerb));
        Mockito.when(prepositionRepository.findByWord(anyString())).thenReturn(Optional.of(testPreposition));
        Mockito.when(nounRepository.findByWord(anyString())).thenReturn(Optional.of(testNoun));

        Mockito.when(phraseRepository.findByAdverbIdAndVerbIdAndPrepositionIdAndNounId(any(Long.class), any(Long.class), any(Long.class), any(Long.class))).thenReturn(Optional.of(testPhrase));

        Mockito.when(userPhraseRepository.save(Mockito.any())).thenReturn(userPhrase);

        boolean rtn = phraseService.applyPhraseToUser(user1.getId(), "testAdverb", "testVerb", "testPreposition", "testNoun");

        verify(userPhraseRepository, times(1)).save(Mockito.any());
        assertTrue(rtn);

    }

    // Test that reviewSubmittingUserRepository is called once when calling ApplyPhraseToUser and conditions:
    // phrase has not been approved
    // phrase exists in to_be_reviewed
    @Test
    public void testApplyPhraseToUserWhenPhraseExistsInToBeReviewed() {
        User user1 = getUser1();

        String testWord = "test";
        ToBeReviewed toBeReviewed = new ToBeReviewed();
        toBeReviewed.setId(1L);
        toBeReviewed.setHasBeenGroomed(false);
        toBeReviewed.setAdverb(testWord);
        toBeReviewed.setVerb(testWord);
        toBeReviewed.setPreposition(testWord);
        toBeReviewed.setNoun(testWord);

        Mockito.when(adverbRepository.findByWord(anyString())).thenReturn(Optional.empty());
        Mockito.when(verbRepository.findByWord(anyString())).thenReturn(Optional.empty());
        Mockito.when(prepositionRepository.findByWord(anyString())).thenReturn(Optional.empty());
        Mockito.when(nounRepository.findByWord(anyString())).thenReturn(Optional.empty());

        Mockito.when(phraseRepository.findByAdverbIdAndVerbIdAndPrepositionIdAndNounId(any(Long.class), any(Long.class), any(Long.class), any(Long.class))).thenReturn(Optional.empty());

        Mockito.when(toBeReviewedRepository.findByAdverbAndVerbAndNounAndPreposition(anyString(), anyString(), anyString(), anyString())).thenReturn(Optional.of(toBeReviewed));

        boolean rtn = phraseService.applyPhraseToUser(user1.getId(), testWord, testWord, testWord, testWord);

        verify(reviewSubmittingUserRepository, times(1)).save(Mockito.any());
        assertFalse(rtn);
    }

    // Test that reviewSubmittingUserRepository is called once when calling ApplyPhraseToUser and conditions:
    // phrase has not been approved
    // phrase does not exist in to_be_reviewed
    @Test
    public void testApplyPhraseToUserWhenPhraseDoesNotExistInToBeReviewed() {
        User user1 = getUser1();

        String testWord = "test";
        ToBeReviewed toBeReviewed = new ToBeReviewed();
        toBeReviewed.setId(1L);
        toBeReviewed.setHasBeenGroomed(false);
        toBeReviewed.setAdverb(testWord);
        toBeReviewed.setVerb(testWord);
        toBeReviewed.setPreposition(testWord);
        toBeReviewed.setNoun(testWord);

        Mockito.when(adverbRepository.findByWord(anyString())).thenReturn(Optional.empty());
        Mockito.when(verbRepository.findByWord(anyString())).thenReturn(Optional.empty());
        Mockito.when(prepositionRepository.findByWord(anyString())).thenReturn(Optional.empty());
        Mockito.when(nounRepository.findByWord(anyString())).thenReturn(Optional.empty());

        Mockito.when(phraseRepository.findByAdverbIdAndVerbIdAndPrepositionIdAndNounId(any(Long.class), any(Long.class), any(Long.class), any(Long.class))).thenReturn(Optional.empty());

        Mockito.when(toBeReviewedRepository.findByAdverbAndVerbAndNounAndPreposition(anyString(), anyString(), anyString(), anyString())).thenReturn(Optional.of(toBeReviewed));

        boolean rtn = phraseService.applyPhraseToUser(user1.getId(), testWord, testWord, testWord, testWord);

        verify(reviewSubmittingUserRepository, times(1)).save(Mockito.any());
        assertFalse(rtn);
    }

    @Test
    public void testGetListOfPhraseDTOByUserIdWithoutPlaceholderNullvalueForEmptyStrings() {
        User user1 = getUser1();
        String testWord = "test";
        String testEmptyString = "";
        List<Long> longList = new ArrayList<>(List.of(1L));
        Phrase testPhrase = getTestPhrase1();

        Mockito.when(userPhraseService.findPhraseIdsByUserId(anyLong())).thenReturn(Optional.of(longList));
        Mockito.when(phraseRepository.findPhraseByPhraseId(anyLong())).thenReturn(Optional.of(testPhrase));
        Mockito.when(adverbRepository.findAdverbById(anyLong())).thenReturn(Optional.of(Constants.NULL_VALUE_WORD));
        Mockito.when(verbRepository.findVerbById(anyLong())).thenReturn(Optional.of(testWord));
        Mockito.when(prepositionRepository.findPrepositionById(anyLong())).thenReturn(Optional.of(Constants.NULL_VALUE_WORD));
        Mockito.when(nounRepository.findNounById(anyLong())).thenReturn(Optional.of(testWord));

        Optional<List<PhraseDTO>> phraseDTOS = phraseService.getListOfPhraseDTOByUserIdWithoutPlaceholderNullvalue(user1.getId());
        PhraseDTO phrase = phraseDTOS.get().get(0);
        assertEquals(phrase.adverb, testEmptyString);
        assertEquals(phrase.preposition, testEmptyString);
    }

    @Test
    public void getListOfPhraseDTOByUserIdWithoutPlaceholderNullvalueWhenNoMatchingPhraseFound() {
        Long userId = 1L;
        Optional<List<Long>> phraseIds = Optional.of(List.of(1L, 2L));
        when(userPhraseService.findPhraseIdsByUserId(userId)).thenReturn(phraseIds);
        when(phraseRepository.findPhraseByPhraseId(anyLong())).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> {
            phraseService.getListOfPhraseDTOByUserIdWithoutPlaceholderNullvalue(userId);
        }, "phrase not found");
    }

    @Test
    public void getListOfPhraseDTOByUserIdWithoutPlaceholderNullvalueWhenNoPhraseIdsFound() {
        Long userId = 1L;
        when(userPhraseService.findPhraseIdsByUserId(userId)).thenReturn(Optional.empty());
        Optional<List<PhraseDTO>> expected = Optional.empty();

        Optional<List<PhraseDTO>> actual = phraseService.getListOfPhraseDTOByUserIdWithoutPlaceholderNullvalue(userId);
        verify(phraseRepository, never()).findPhraseByPhraseId(anyLong());
        verify(adverbRepository, never()).findAdverbById(anyLong());
        verify(verbRepository, never()).findVerbById(anyLong());
        verify(nounRepository, never()).findNounById(anyLong());
        verify(prepositionRepository, never()).findPrepositionById(anyLong());
        assertEquals(actual, expected);
    }

    @Test
    public void testApplyPhraseToUserWhenAdverbIsBlank() {
        User user1 = getUser1();
        String testAdverb = "";
        String testAdverbConverted = "nullvalue";
        String testVerb = getTestVerb1().getWord();
        String testPreposition = getTestPreposition1().getWord();
        String testNoun = getTestNoun1().getWord();

        ToBeReviewed tbrSaved = new ToBeReviewed();
        tbrSaved.setId(1L);
        tbrSaved.setHasBeenGroomed(false);
        tbrSaved.setAdverb(testAdverbConverted);
        tbrSaved.setVerb(testVerb);
        tbrSaved.setPreposition(testPreposition);
        tbrSaved.setNoun(testNoun);

        // Should return false if the phrase has not been seen before
        Mockito.when(toBeReviewedRepository.save(any())).thenReturn(tbrSaved);
        boolean applyPhraseToUser = phraseService.applyPhraseToUser(user1.getId(), testAdverb, testVerb, testPreposition, testNoun);
        assertFalse(applyPhraseToUser);

        // Empty Adverb should be converted to "nullvalue"
        ArgumentCaptor<String> argAdverb = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argVerb = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argPreposition = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argNoun = ArgumentCaptor.forClass(String.class);
        verify(toBeReviewedRepository, times(1)).findByAdverbAndVerbAndNounAndPreposition(argAdverb.capture(), argVerb.capture(), argNoun.capture(), argPreposition.capture());
        assertEquals(argAdverb.getValue(), testAdverbConverted);
    }

    @Test
    public void testApplyPhraseToUserWhenPrepositionIsBlank() {
        User user1 = getUser1();
        String testAdverb = getTestAdverb1().getWord();
        String testVerb = getTestVerb1().getWord();
        String testPreposition = "";
        String testPrepositionConverted = "nullvalue";
        String testNoun = getTestNoun1().getWord();

        ToBeReviewed tbrSaved = new ToBeReviewed();
        tbrSaved.setId(1L);
        tbrSaved.setHasBeenGroomed(false);
        tbrSaved.setAdverb(testAdverb);
        tbrSaved.setVerb(testVerb);
        tbrSaved.setPreposition(testPrepositionConverted);
        tbrSaved.setNoun(testNoun);

        // Should return false if the phrase has not been seen before
        Mockito.when(toBeReviewedRepository.save(any())).thenReturn(tbrSaved);
        when(nounRepository.findByWord(anyString())).thenReturn(Optional.of(new Noun(testNoun)));
        when(verbRepository.findByWord(anyString())).thenReturn(Optional.of(new Verb(testVerb)));
        boolean applyPhraseToUser = phraseService.applyPhraseToUser(user1.getId(), testAdverb, testVerb, testPreposition, testNoun);
        assertFalse(applyPhraseToUser);

        // Empty Preposition should be converted to "nullvalue"
        ArgumentCaptor<String> argAdverb = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argVerb = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argPreposition = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argNoun = ArgumentCaptor.forClass(String.class);
        verify(toBeReviewedRepository, times(1)).findByAdverbAndVerbAndNounAndPreposition(argAdverb.capture(), argVerb.capture(), argNoun.capture(), argPreposition.capture());
        assertEquals(argPreposition.getValue(), testPrepositionConverted);
    }

}
