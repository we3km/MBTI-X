package com.kh.mbtix.balgame.model.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.mbtix.balgame.mapper.BalGameMapper;
import com.kh.mbtix.balgame.model.dto.BalGameDtos;
import com.kh.mbtix.balgame.model.dto.BalGameDtos.CreateGameReq;
import com.kh.mbtix.balgame.model.dto.BalGameDtos.CreateGameRes;
import com.kh.mbtix.balgame.model.dto.BalGameDtos.OptionDto;
import com.kh.mbtix.balgame.model.dto.BalGameDtos.PastListRes;
import com.kh.mbtix.balgame.model.dto.BalGameDtos.StatsRes;
import com.kh.mbtix.balgame.model.dto.BalGameDtos.TodayGameRes;

@Service

public class BalGameService {
	
	private final BalGameMapper mapper;
	public BalGameService(BalGameMapper mapper){ this.mapper = mapper; }
	
	// 오늘의 게임 조회
	public TodayGameRes getToday(Long userId) {
	    var g = mapper.selectTodayGame();
	    if (g == null) return null;

	    long gameId = g.gameId();
	    String title = g.title();

	    // 옵션 + 투표수
	    var optionDtos = mapper.selectOptionsByGameId(gameId);
	    var voteCounts = mapper.selectOptionVoteCounts(gameId).stream()
	        .collect(Collectors.toMap(
	            row -> (String) row.get("LABEL"),
	            row -> ((Number) row.get("CNT")).longValue()
	        ));

	    var options = optionDtos.stream()
	        .map(o -> new TodayGameRes.OptionBrief(
	            o.label(),
	            o.textContent(),
	            voteCounts.getOrDefault(o.label(), 0L)
	        ))
	        .toList();

	    // 내가 투표한 옵션 라벨 (null이면 아직 안 함)
	    String myVote = mapper.selectUserVoteLabel(gameId, userId);

	    return new TodayGameRes(gameId, title, options, myVote);
	}
	
	
	// 과거 게임 목록
	 public PastListRes getPast(int page, int size){
	    int offset = (page-1)*size;
	    var rows = mapper.selectPastGamesPaged(offset,size);
	    var cards = rows.stream().map(r ->
	      new PastListRes.PastCard(
	        ((Number)r.get("GAME_ID")).longValue(),
	        (String)r.get("TITLE"),
	        (String)r.get("START_AT"))
	    ).toList();
	    int total = mapper.countPastGames();
	    int totalPages = (int)Math.ceil(total/(double)size);
	    return new PastListRes(cards,page,size,totalPages);
	  }
	 
	 
	 // 투표
	  @Transactional
	  public void vote(Long gameId,  String optionLabel, Long userId) {
		  
		// 이미 투표했는지 확인
		    if (mapper.hasUserVoted(gameId, userId) > 0) {
		        throw new IllegalStateException("ALREADY_VOTED");
		    }

		    // 해당 게임의 옵션 가져오기
		    var options = mapper.selectOptionsByGameId(gameId);
		    var chosen = options.stream()
		            .filter(o -> o.label().equals(optionLabel))
		            .findFirst()
		            .orElseThrow(() -> new IllegalArgumentException("INVALID_OPTION"));

		    // 유저 MBTI 가져오기
		    String snapMbti = mapper.selectUserMbtiCode(userId);

		    // 투표 저장
		    mapper.insertVote(gameId, chosen.optionId(), userId, snapMbti);
		}
	
	  // 게임 통계
	  public StatsRes stats(Long gameId) {
	        var counts = mapper.selectOptionVoteCounts(gameId);

	        long total = counts.stream()
	            .mapToLong(m -> ((Number) m.get("CNT")).longValue())
	            .sum();

	        Map<String, StatsRes.OptStat> options = new HashMap<>();
	        for (var row : counts) {
	            String label = (String) row.get("LABEL");
	            long cnt = ((Number) row.get("CNT")).longValue();
	            double ratio = total == 0 ? 0 : (cnt * 100.0 / total);
	            options.put(label, new StatsRes.OptStat(cnt, ratio));
	        }
	        
	        
	     // 2) 옵션별 MBTI 분포
	        var rows = mapper.selectOptionMbti(gameId); // LABEL, MBTI, CNT, PCT(0~1)
	        Map<String, Map<String, BalGameDtos.StatsRes.MbtiStat>> mbti = new HashMap<>();
	        for (var r : rows) {
	          String label = (String) r.get("LABEL");     // 'A' | 'B'
	          String code  = (String) r.get("MBTI");      // 'ENFP'
	          long   cnt   = ((Number) r.get("CNT")).longValue();
	          double pct   = ((Number) r.get("PCT")).doubleValue() * 100.0; // 0~100%

	          mbti.computeIfAbsent(label, k->new LinkedHashMap<>())
	              .put(code, new BalGameDtos.StatsRes.MbtiStat(cnt, pct));
	        }

	        return new StatsRes(gameId, total, options, mbti);
	    }
	  
	  
	  // 자정 스케줄러
	    @Scheduled(cron = "0 0 0 * * *")
	    @Transactional
	    public void resetDailyGame() {
	        mapper.deactivateTodayGame();
	        mapper.activateNewGame();
	    }
	    
	    // 겡미생성
	    @Transactional
	    public CreateGameRes createGame(CreateGameReq req) {
	        // 1) BAL_GAME insert
	        mapper.insertGame(req.title());

	        // 2) 시퀀스 CURRVAL로 방금 만든 BAL_ID 가져오기
	        Long gameId = mapper.selectLastGameId();

	        // 3) BAL_OPTION insert (A, B)
	        mapper.insertOption(gameId, "A", req.optionAText());
	        mapper.insertOption(gameId, "B", req.optionBText());

	        return new CreateGameRes(gameId, req.title());
	    }

}


























