package com.timcritt.tfg.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb-openapi;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "authorization.classroom.transport=http"
})
@AutoConfigureMockMvc
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiDocs_arePublic_andUseOpenApi31() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.openapi").value(startsWith("3.1")))
                .andExpect(jsonPath("$.components.schemas").exists());
    }

    @Test
    void swaggerUi_isPublic() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Swagger UI")));
    }

    @Test
    void swaggerUiShortcut_isPublic() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void businessApi_staysProtected_withoutJwt() throws Exception {
        mockMvc.perform(get("/api/storage/presigned-url")
                        .param("bucket", "materials")
                        .param("objectKey", "sample.mp3"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void openApi_contract_exposesTypedSchemas() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.schemas.SpeakingSectionSummaryDto.required", hasItem("materialId")))
                .andExpect(jsonPath("$.components.schemas.SpeakingSectionEditDto.required", hasItem("materialId")))
                .andExpect(jsonPath("$.components.schemas.MaterialNodeWithAssetsDto.required", hasItem("kind")))
                .andExpect(jsonPath("$.components.schemas.MaterialAssetDto.required", hasItem("kind")))
                .andExpect(jsonPath("$.components.schemas.MaterialAssetDto.properties.kind.enum", hasItem("AUDIO")))
                .andExpect(jsonPath("$.components.schemas.MaterialNodeWithAssetsDto.properties.kind.enum", hasItem("ITEM")))
                .andExpect(jsonPath("$.components.schemas.QuestionEditDto.properties.config.additionalProperties").value(true))
                .andExpect(jsonPath("$.components.schemas.MaterialNodeWithAssetsDto.properties.config.additionalProperties").value(true))
                .andExpect(jsonPath("$.components.schemas.QuestionUpload.properties.config.type").value("string"))
                .andExpect(jsonPath("$.components.schemas.ApiErrorResponse.required", hasItem("message")))
                .andExpect(jsonPath("$.components.schemas.ApiErrorResponse.required", not(hasItem("errors"))))
                .andExpect(jsonPath("$.components.schemas.ApiErrorResponse.properties.errors.additionalProperties").value(true))
                .andExpect(jsonPath("$['paths']['/api/toefl-speaking/material/section/upload']['post']['responses']['200']['content']['application/json']['schema']['$ref']")
                        .value(containsString("DraftSaveResponseDto")))
                .andExpect(jsonPath("$['paths']['/api/toefl-speaking/material/section/upload']['post']['responses']['400']['content']['application/json']['schema']['$ref']")
                        .value(containsString("ApiErrorResponse")))
                .andExpect(jsonPath("$['paths']['/api/toefl-speaking/material/section/upload']['post']['responses']['401']['content']['application/json']['schema']['$ref']")
                        .value(containsString("ApiErrorResponse")))
                .andExpect(jsonPath("$['paths']['/api/toefl-speaking/material/section/upload']['post']['responses']['403']['content']['application/json']['schema']['$ref']")
                        .value(containsString("ApiErrorResponse")))
                .andExpect(jsonPath("$['paths']['/api/toefl-speaking/material/section/upload']['post']['responses']['503']['content']['application/json']['schema']['$ref']")
                        .value(containsString("ApiErrorResponse")))
                .andExpect(jsonPath("$['paths']['/api/storage/presigned-url']['get']['responses']['400']['content']['application/json']['schema']['$ref']")
                        .value(containsString("ApiErrorResponse")))
                .andExpect(jsonPath("$['paths']['/api/storage/presigned-url']['get']['responses']['401']['content']['application/json']['schema']['$ref']")
                        .value(containsString("ApiErrorResponse")))
                .andExpect(jsonPath("$['paths']['/api/storage/presigned-url']['get']['responses']['403']['content']['application/json']['schema']['$ref']")
                        .value(containsString("ApiErrorResponse")))
                .andExpect(jsonPath("$['paths']['/api/storage/presigned-url']['get']['responses']['503']['content']['application/json']['schema']['$ref']")
                        .value(containsString("ApiErrorResponse")))
                .andExpect(jsonPath("$.components.schemas.ApiErrorResponse").exists());
    }
}

