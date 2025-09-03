package com.kh.mbtix.alarm.model.dao;

import org.apache.ibatis.annotations.Mapper;
import com.kh.mbtix.alarm.model.vo.Alarm;

@Mapper
public class AlarmDao {
	
	int insertAlarm(Alarm alarm);

}
