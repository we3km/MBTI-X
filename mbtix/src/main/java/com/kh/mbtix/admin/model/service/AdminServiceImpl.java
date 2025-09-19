package com.kh.mbtix.admin.model.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kh.mbtix.admin.model.dao.AdminDao;

@Service
public class AdminServiceImpl implements AdminService {
	@Autowired
	private AdminDao adminDao;

	@Override
	public void insertGameData(Map<String, Object> data) {
		String type = ((String) data.get("type"));

		switch (type) {
		case "speedQuiz":
			adminDao.insertSpeedQuiz(data);
			break;
		case "catchMind":
			adminDao.insertCathMindWords(data);
			break;
		default:
			return;
		}
	}
}