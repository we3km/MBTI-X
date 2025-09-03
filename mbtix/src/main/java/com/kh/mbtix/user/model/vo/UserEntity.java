package com.kh.mbtix.user.model.vo;

import java.sql.Date;
import lombok.Data;

@Data
public class UserEntity {

    private int userId;
    private int mbtiId;
    private String loginId;
    private String email;
    private String name;
    private String nickname;
    private Date createdAt;
    private String isQuit;
    private int point;
    private String statusName;
    private Date releasaeDate;
}