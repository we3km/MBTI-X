package com.kh.mbtix.board.controller;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannedUser {
    private Long userId;
    private String banReason;
}