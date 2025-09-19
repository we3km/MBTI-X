package com.kh.mbtix.alarm.model.service;

import com.kh.mbtix.alarm.model.vo.Alarm;
import java.util.List;

public interface AlarmService {
	
    int createAlarm(Alarm alarm);
    List<Alarm> findAlarmsByUserId(Long userId);
    int markAsRead(Long userId, int alarmId);
    
    void markAllAsRead(Long userId);
    void deleteAllAlarms(Long userId);
    
}