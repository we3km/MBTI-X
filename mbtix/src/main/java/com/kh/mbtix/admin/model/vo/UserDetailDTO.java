package com.kh.mbtix.admin.model.vo;

import lombok.Getter;
import lombok.Setter;
import com.kh.mbtix.user.model.vo.UserEntity;
import java.util.List;

@Getter
@Setter
public class UserDetailDTO {
	
	private UserInfo userInfo;
	private List<BanHistory> banHistory;
	private List<ReportHistory> reportsMade;
	private List<ReportHistory> reportsReceived;
	
	@Getter
    @Setter
    public static class UserInfo {
        private int userId;
        private String loginId;
        private String nickname;
        private String email;
        private String createdAt;
        private String statusName;
        private int point;
        private List<String> roles;
    }

    @Getter
    @Setter
    public static class BanHistory {
        private int bannedId;
        private String reson;
        private String penaltyDate;
        private String relesaeDate;
    }

    @Getter
    @Setter
    public static class ReportHistory {
        private int reportId;
        private String targetNickname; // 신고한, 신고받은 대상의 닉네임
        private String reportCategoryName;
        private String createdAt;
        private String status;
    }

}
