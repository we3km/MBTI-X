package com.kh.mbtix.mypage.model.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProfileFileService {

    private final String uploadDir = System.getProperty("user.dir") + "/upload/profile";

    public String saveProfile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("빈 파일은 저장할 수 없습니다.");
        }
        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String savedFileName = java.util.UUID.randomUUID() + ext;
            Path path = Paths.get(uploadDir, savedFileName);
            Files.copy(file.getInputStream(), path);

            return savedFileName; // DB에는 파일명만 저장
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패: " + file.getOriginalFilename(), e);
        }
    }
}
