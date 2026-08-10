package com.timcritt.tfg.infrastructure.security.authorization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "authorization.classroom", name = "transport", havingValue = "http", matchIfMissing = true)
@RequiredArgsConstructor
public class ClassroomAuthorizationClient implements ClassroomAuthorizationPort {

    private final RestClient.Builder restClientBuilder;
    private final ClassroomAuthorizationProperties properties;

    public MaterialAccessCheckResponse checkReadAccess(String userId, Long materialId) {
        MaterialAccessCheckRequest request = new MaterialAccessCheckRequest(userId, materialId, "READ");
        RestClient client = restClientBuilder.baseUrl(properties.getBaseUrl()).build();
        String target = properties.getBaseUrl() + properties.getCheckPath();

        try {
            RestClient.RequestBodySpec spec = client.post()
                    .uri(properties.getCheckPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON);

            if (properties.getInternalApiKey() != null && !properties.getInternalApiKey().isBlank()) {
                spec = spec.header(properties.getInternalApiKeyHeader(), properties.getInternalApiKey());
            }

            MaterialAccessCheckResponse response = spec.body(request)
                    .retrieve()
                    .body(MaterialAccessCheckResponse.class);

            if (response == null) {
                throw new ClassroomAuthorizationUnavailableException("Authorization service returned empty response", null);
            }
            return response;
        } catch (RestClientResponseException ex) {
            String responseBody = ex.getResponseBodyAsString();
            String message = "Classroom authorization call failed: "
                    + ex.getStatusCode() + " from " + target
                    + (responseBody == null || responseBody.isBlank()
                    ? ""
                    : " - " + responseBody);
            throw new ClassroomAuthorizationUnavailableException(message, ex);
        } catch (RestClientException ex) {
            throw new ClassroomAuthorizationUnavailableException(
                    "Failed to call classroom authorization endpoint: " + target, ex);
        }
    }

}

