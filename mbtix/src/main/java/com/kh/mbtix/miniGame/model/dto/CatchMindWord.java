package com.kh.mbtix.miniGame.model.dto;

import java.util.Date;

import lombok.Data;

@Data
public class CatchMindWord {
	private int wordId;
	private String word;
	private Date createdAt;
}
