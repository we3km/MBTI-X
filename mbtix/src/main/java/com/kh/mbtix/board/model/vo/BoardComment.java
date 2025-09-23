package com.kh.mbtix.board.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardComment {
		private int commentId;
		private long userId;
		private String content;
		private String createAt;
		private int parentId;
		private String status;
		private int boardId;
		private String nickname;
	}

