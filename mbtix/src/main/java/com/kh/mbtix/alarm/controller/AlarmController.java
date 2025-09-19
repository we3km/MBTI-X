package com.kh.mbtix.alarm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.mbtix.alarm.model.service.AlarmService;
import com.kh.mbtix.alarm.model.vo.Alarm;

@RestController
@RequestMapping("/alarms")
public class AlarmController {

    @Autowired
    private AlarmService alarmService;

    @GetMapping
    public ResponseEntity<List<Alarm>> getMyAlarmList(@AuthenticationPrincipal Long userId) {
        if (userId == null) {
            // 사용자가 인증되지 않은 경우 401 Unauthorized 응답
            return ResponseEntity.status(401).build();
        }
        List<Alarm> list = alarmService.findAlarmsByUserId(userId);
        return ResponseEntity.ok(list);
    }
    
    @PatchMapping("/{alarmId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable("alarmId") int alarmId, @AuthenticationPrincipal Long userId) {
        int result = alarmService.markAsRead(userId, alarmId);
        return result > 0 ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
    
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal Long userId) {
        alarmService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    } 
    
    // 모두 읽음 클릭시 알림 삭제
    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteReadAlarms(@AuthenticationPrincipal Long userId) {
        alarmService.deleteAllAlarms(userId);
        return ResponseEntity.ok().build();
    }
    
}