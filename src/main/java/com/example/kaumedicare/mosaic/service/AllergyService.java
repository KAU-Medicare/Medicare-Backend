package com.example.kaumedicare.mosaic.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Service
public class AllergyService {

    @Value("${flask.server.url}")
    private String flaskServerUrl;

    public String processAndSaveImage(MultipartFile image) throws IOException {
        // 임시 파일 생성
        Path tempFile = Files.createTempFile("upload_", image.getOriginalFilename());
        image.transferTo(tempFile.toFile());

        // Flask 서버로 이미지 전송
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(tempFile.toFile()));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(flaskServerUrl + "/mosaic", requestEntity, String.class);

        // 임시 파일 삭제
        Files.delete(tempFile);

        if (response.getStatusCode() == HttpStatus.OK) {
            // Base64로 인코딩된 이미지 데이터를 받아 처리
            String base64Image = response.getBody();
            // 여기서 base64Image를 저장하거나 추가 처리할 수 있습니다.
            return base64Image;
        } else {
            throw new RuntimeException("Failed to process image");
        }
    }
}