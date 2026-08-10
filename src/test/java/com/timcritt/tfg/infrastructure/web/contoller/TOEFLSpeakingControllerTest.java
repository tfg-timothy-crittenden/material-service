package com.timcritt.tfg.infrastructure.web.contoller;

import com.timcritt.tfg.application.dto.SpeakingSectionEditResult;
import com.timcritt.tfg.application.port.inbound.TOEFLSpeakingMaterialCommandUseCase;
import com.timcritt.tfg.application.port.inbound.TOEFLSpeakingNavigationUseCase;
import com.timcritt.tfg.domain.model.MaterialStatus;
import com.timcritt.tfg.infrastructure.web.validation.SpeakingMultipartFieldValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TOEFLSpeakingControllerTest {

    private final TOEFLSpeakingNavigationUseCase navigationUseCase = mock(TOEFLSpeakingNavigationUseCase.class);
    private final TOEFLSpeakingMaterialCommandUseCase commandUseCase = mock(TOEFLSpeakingMaterialCommandUseCase.class);
    private final SpeakingMultipartFieldValidator multipartFieldValidator = new SpeakingMultipartFieldValidator();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TOEFLSpeakingController controller = new TOEFLSpeakingController(
                navigationUseCase,
                commandUseCase,
                multipartFieldValidator
        );

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .build();
    }

    @Test
    void saveSpeakingSectionDraft_allowsIncompleteMultipartPayload() throws Exception {
        when(commandUseCase.uploadSpeakingSection(any())).thenReturn(123L);

        mockMvc.perform(multipart("/api/toefl-speaking/material/section/draft")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("materialDescription", "Draft in progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materialId").value(123));

        verify(commandUseCase, times(1)).uploadSpeakingSection(any());
    }

    @Test
    void uploadSpeakingSection_rejectsIncompleteMultipartPayload() throws Exception {
        mockMvc.perform(multipart("/api/toefl-speaking/material/section/upload")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("materialDescription", "Draft in progress"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(commandUseCase);
    }

    @Test
    void deleteSpeakingSection_returnsOkAndDelegatesToUseCase() throws Exception {
        mockMvc.perform(delete("/api/toefl-speaking/material/{materialId}", 123L))
                .andExpect(status().isOk());

        verify(commandUseCase, times(1)).deleteSpeakingSection(123L);
    }

    @Test
    void publishSpeakingSection_returnsOkAndDelegatesToUseCase() throws Exception {
        mockMvc.perform(patch("/api/toefl-speaking/material/{materialId}/publish", 123L))
                .andExpect(status().isOk());

        verify(commandUseCase, times(1)).publishSpeakingSection(123L);
    }

    @Test
    void getSpeakingSectionForEdit_returnsStatus() throws Exception {
        when(navigationUseCase.getSpeakingSectionForEdit(55L)).thenReturn(Optional.of(
                SpeakingSectionEditResult.builder()
                        .materialId(55L)
                        .sectionId(550L)
                        .status(MaterialStatus.DRAFT)
                        .materialTitle("Draft section")
                        .build()
        ));

        mockMvc.perform(get("/api/toefl-speaking/material/{materialId}/section", 55L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }
}

