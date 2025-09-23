package com.kh.mbtix.common.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileStorage {

    // 파일 저장할 기본 경로 (예: 프로젝트 내부 /uploads)
    private final String uploadDir = System.getProperty("user.dir") + "/uploads";

    public String save(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("빈 파일은 저장할 수 없습니다.");
        }

        try {
            // 저장 폴더 없으면 생성
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 고유한 파일명 생성 (UUID + 원본 확장자)
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String savedFileName = java.util.UUID.randomUUID() + ext;

            // 저장 경로
            Path path = Paths.get(uploadDir, savedFileName);

            // 실제 파일 저장
            Files.copy(file.getInputStream(), path);

            // DB 저장용으로는 상대경로나 파일명만 반환하는 게 일반적
            return savedFileName;

        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패: " + file.getOriginalFilename(), e);
        }
    }
}
