package com.kh.mbtix.board.model.vo;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Board {
	private int boardId; // boardId 
	private String title;
	private String content;
	private String nickname; // 회원 이름
	private String createdAt;
	private int view;
	private int categoryId;
	private int userId;	
	private String mbtiName;	
	private List<String> images;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BoardRequest {
        private String title;
        private String content;
        private int categoryId;
        private String mbtiName;

        // 프런트에서 formData.append("images", file) 로 넘어오는 파일들
        private List<MultipartFile> images;
    }
}



