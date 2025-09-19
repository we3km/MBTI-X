package com.kh.mbtix.admin.model.dao;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminDao {
	void insertSpeedQuiz(Map<String, Object> data);
	void insertCathMindWords(Map<String, Object> data);
}