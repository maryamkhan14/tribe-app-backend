package com.savvato.tribeapp.services;

import com.savvato.tribeapp.constants.AbstractTestConstants;
import com.savvato.tribeapp.dto.CosignDTO;
import com.savvato.tribeapp.entities.Cosign;
import com.savvato.tribeapp.repositories.CosignRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class})
public class CosignServiceImplTest extends AbstractTestConstants {

    @TestConfiguration
    static class CosignServiceImplTestContextConfiguration {

        @Bean
        public  CosignService cosignService() {
            return new CosignServiceImpl();
        }

    }

    @Autowired
    CosignService cosignService;

    @MockBean
    CosignRepository cosignRepository;

    @Test
    public void saveCosign() {
        Long userIdIssuing = 1L;
        Long userIdReceiving = 1L;
        Long phraseId = 1L;

        Cosign cosign = new Cosign();
        cosign.setUserIdIssuing(userIdIssuing);
        cosign.setUserIdReceiving(userIdReceiving);
        cosign.setPhraseId(phraseId);

        CosignDTO cosignDTO = CosignDTO.builder().build();
        cosignDTO.userIdIssuing = userIdIssuing;
        cosignDTO.userIdReceiving = userIdReceiving;
        cosignDTO.phraseId = phraseId;

        when(cosignRepository.save(Mockito.any())).thenReturn(cosign);

        CosignDTO expectedCosignDTO = cosignService.saveCosign(userIdIssuing, userIdReceiving, phraseId);

        verify(cosignRepository, times(1)).save(Mockito.any());
        assertEquals(cosignDTO.userIdIssuing, expectedCosignDTO.userIdIssuing);
        assertEquals(cosignDTO.userIdReceiving, expectedCosignDTO.userIdReceiving);
        assertEquals(cosignDTO.phraseId, expectedCosignDTO.phraseId);
    }

    @Test
    public void saveCosignAlreadyExisting() {
        Long userIdIssuing = 1L;
        Long userIdReceiving = 1L;
        Long phraseId = 1L;

        Cosign cosign = new Cosign();
        cosign.setUserIdIssuing(userIdIssuing);
        cosign.setUserIdReceiving(userIdReceiving);
        cosign.setPhraseId(phraseId);

        CosignDTO cosignDTO = CosignDTO.builder().build();
        cosignDTO.userIdIssuing = userIdIssuing;
        cosignDTO.userIdReceiving = userIdReceiving;
        cosignDTO.phraseId = phraseId;

        when(cosignRepository.save(Mockito.any())).thenReturn(cosign).thenReturn(cosign);

        CosignDTO expectedCosignDTO = cosignService.saveCosign(userIdIssuing, userIdReceiving, phraseId);

        assertEquals(cosignDTO.userIdIssuing, expectedCosignDTO.userIdIssuing);
        assertEquals(cosignDTO.userIdReceiving, expectedCosignDTO.userIdReceiving);
        assertEquals(cosignDTO.phraseId, expectedCosignDTO.phraseId);

        CosignDTO expectedCosignDTORepeat = cosignService.saveCosign(userIdIssuing, userIdReceiving, phraseId);

        assertEquals(cosignDTO.userIdIssuing, expectedCosignDTORepeat.userIdIssuing);
        assertEquals(cosignDTO.userIdReceiving, expectedCosignDTORepeat.userIdReceiving);
        assertEquals(cosignDTO.phraseId, expectedCosignDTORepeat.phraseId);
    }
}
