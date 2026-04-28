package com.timcritt.tfg.application.dto.toefl;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadedFileCommand {
    private String originalFilename;
    private String contentType;
    private long size;
    private byte[] bytes;
}

