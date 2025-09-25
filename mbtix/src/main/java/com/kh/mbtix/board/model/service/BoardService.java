package com.kh.mbtix.board.model.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kh.mbtix.alarm.model.service.AlarmService;
import com.kh.mbtix.alarm.model.vo.Alarm;
import com.kh.mbtix.board.model.dao.BoardDao;
import com.kh.mbtix.board.model.vo.Board;
import com.kh.mbtix.board.model.vo.Board.BoardLike;
import com.kh.mbtix.board.model.vo.BoardComment;
import com.kh.mbtix.common.util.FileStorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardService {

    private final BoardDao dao;
    private final FileStorage fileStorage;
    private final AlarmService alarmService;

    public List<Board> selectBoardList(Map<String, Object> param) {
        return dao.selectBoardList(param);
    }

    public Board selectBoard(int boardId) {
        return dao.selectBoard(boardId);
    }

    public void incrementView(int boardId) {
        dao.incrementView(boardId);
    }

    @Transactional
    public int insertBoard(Board b, List<MultipartFile> images) {
        int result = dao.insertBoard(b);

        if(b.getCategoryId() == 3 && result > 0) {
            dao.inserMbtiBoard(b);
        }

        if(b.getCategoryId() == 1 && result > 0) {
            b.setMbtiName(b.getBoardMbti());
            dao.inserMbtiBoard(b);
        }

        if (result > 0 && images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                String savedPath = fileStorage.save(file);
                HashMap<String, Object> map = new HashMap<>();
                map.put("fileName", savedPath);
                map.put("refId", b.getBoardId());
                map.put("categoryId", b.getCategoryId());
                dao.insertBoardImage(map);
            }
        }

        return result;
    }

    /*public int insertReport(Report r) {
        return dao.insertReport(r);
    }
    */

    @Transactional
    public void insertComment(BoardComment c) {
        int result = dao.insertComment(c);
        
        if (result > 0) {
            
            String previewContent = c.getContent().length() > 15 
                                    ? c.getContent().substring(0, 15) + "..." 
                                    : c.getContent();
            String alarmContent = c.getNickname() + ": " + previewContent;
            
            // 대댓글
            if (c.getParentId() > 0) {
                Long parentCommentAuthorId = dao.selectParentCommentAuthorId(c.getParentId());
                if (parentCommentAuthorId != null && parentCommentAuthorId != c.getUserId()) {
                    Alarm alarm = Alarm.builder()
                            .receiverId(parentCommentAuthorId.intValue())
                            .content(alarmContent)
                            .refId(c.getBoardId())
                            .type("NEW_REPLY")
                            .build();
                    alarmService.createAlarm(alarm);
                }
            } 
            // 댓글
            else {
                Long boardAuthorId = dao.selectBoardAuthorId(c.getBoardId());
                if (boardAuthorId != null && boardAuthorId != c.getUserId()) {
                    Alarm alarm = Alarm.builder()
                            .receiverId(boardAuthorId.intValue())
                            .content(alarmContent)
                            .refId(c.getBoardId())
                            .type("NEW_COMMENT")
                            .build();
                    alarmService.createAlarm(alarm);
                }
            }
        }
    }

    public List<BoardComment> getComments(int boardId) {
        return dao.getComments(boardId);
    }

    public boolean deleteBoard(int boardId, long userId) {
        return dao.deleteBoard(boardId, userId) > 0;
    }

    public int insertLike(BoardLike map) {
        String savedStatus = dao.selectBoardLike(map);
        if(savedStatus != null) {
            String status = map.getStatus();
            System.out.println(status+"----------------"+savedStatus);
            if(status.equals(savedStatus)) {

                return dao.deleteLike(map);
            }else {
                return dao.updateLike(map);
            }
        }

        return dao.insertLike(map);
    }

    public int deleteComment(BoardComment comment) {
        return dao.deleteComment(comment);
    }

    public boolean deleteComment(int commentId, Long userId) {
        return false;
    }
}