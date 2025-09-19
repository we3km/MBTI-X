package com.kh.mbtix.alarm.model.service;

import com.kh.mbtix.alarm.model.dao.AlarmDao;
import com.kh.mbtix.alarm.model.vo.Alarm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AlarmServiceImpl implements AlarmService {

    @Autowired
    private AlarmDao alarmDao;

    @Override
    public int createAlarm(Alarm alarm) {
        return alarmDao.insertAlarm(alarm);
    }

    @Override
    public List<Alarm> findAlarmsByUserId(Long userId) {
        return alarmDao.findAlarmsByUserId(userId);
    }
    
    @Override
    public int markAsRead(Long userId, int alarmId) {
    	Alarm alarm = new Alarm();
    	alarm.setReceiverId(userId.intValue());
    	alarm.setAlarmId(alarmId);
    	
    	return alarmDao.markAsRead(alarm);
    }
    
    @Override
    public void markAllAsRead(Long userId) {
        alarmDao.markAllAsRead(userId);
    }
    
    @Override
    public void deleteAllAlarms(Long userId) {
    	alarmDao.deleteAllAlarms(userId);
    }
}