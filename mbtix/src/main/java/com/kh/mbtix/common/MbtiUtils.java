package com.kh.mbtix.common;

import java.util.Map;

public class MbtiUtils {

    // MBTI 번호 → 문자열
    private static final Map<String, String> MBTI_CODE_MAP = Map.ofEntries(
        Map.entry("1", "ISTJ"),
        Map.entry("2", "ISFJ"),
        Map.entry("3", "INFJ"),
        Map.entry("4", "INTJ"),
        Map.entry("5", "ISTP"),
        Map.entry("6", "ISFP"),
        Map.entry("7", "INFP"),
        Map.entry("8", "INTP"),
        Map.entry("9", "ESTP"),
        Map.entry("10", "ESFP"),
        Map.entry("11", "ENFP"),
        Map.entry("12", "ENTP"),
        Map.entry("13", "ESTJ"),
        Map.entry("14", "ESFJ"),
        Map.entry("15", "ENFJ"),
        Map.entry("16", "ENTJ")
    );

    /**
     * MBTI 번호 → 문자열 (예: "5" → "ISTP")
     */
    public static String getMbti(String mbtiId) {
        return MBTI_CODE_MAP.get(mbtiId);
    }

    /**
     * MBTI 번호 → 디폴트 프로필 파일명 (예: "5" → "istp.jpg")
     */
    public static String getProfileFileName(String mbtiId) {
        String mbti = getMbti(mbtiId);
        return mbti != null ? mbti.toLowerCase() + ".jpg" : "default.jpg";
    }
}
