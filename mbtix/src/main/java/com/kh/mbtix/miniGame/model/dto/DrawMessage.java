package com.kh.mbtix.miniGame.model.dto;

import lombok.Data;

@Data
public class DrawMessage {
    // Excalidraw의 elements 배열 전체(복잡한 JSON 구조)를
    // 유연하게 받기 위해 Object 타입으로 선언합니다.
    private Object data;
}