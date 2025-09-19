package com.kh.mbtix.admin.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DashboardStatsDTO {
	
	private int newUsersToday;
	private int pendingInquiries;
	private int pendingReports;
	private int totalUsers;

}
