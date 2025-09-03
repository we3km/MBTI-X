package com.kh.mbtix.board.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Board {
	private int boardId;
	private String title;
	private String content;
	private String nickname; // 회원 이름
	private String createdAt;
	private int view;
	private int categoryId;
	private int userId;	
	private String mbtiName;
}



