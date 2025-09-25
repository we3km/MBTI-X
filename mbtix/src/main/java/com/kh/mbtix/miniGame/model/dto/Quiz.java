package com.kh.mbtix.miniGame.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quiz {
	private int questionId;
	private String question;
	private String answer;	
}
