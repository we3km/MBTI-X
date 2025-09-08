package com.kh.mbtix.alarm.model.dao;

import com.kh.mbtix.alarm.model.vo.Alarm;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface AlarmDao {
	
    int insertAlarm(Alarm alarm);
    List<Alarm> findAlarmsByUserId(Long userId);
    
    int markAsRead(Alarm alarm);
    
    void markAllAsRead(Long userId);
    void deleteAllAlarms(Long userId);
    
}