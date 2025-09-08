package com.kh.mbtix.balgame.model.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.mbtix.balgame.mapper.BalGameMapper;
import com.kh.mbtix.balgame.model.dto.BalGameDtos.OptionDto;
import com.kh.mbtix.balgame.model.dto.BalGameDtos.PastListRes;
import com.kh.mbtix.balgame.model.dto.BalGameDtos.StatsRes;
import com.kh.mbtix.balgame.model.dto.BalGameDtos.TodayGameRes;

@Service

public class BalGameService {
	private final BalGameMapper mapper;
	  public BalGameService(BalGameMapper mapper){ this.mapper = mapper; }

	  public TodayGameRes getToday(Long userId) {
		    var g = mapper.selectTodayGame();
		    if (g == null) return null;

		    long gameId = g.gameId();
		    String title = g.title();

		    // ✅ OptionDto 그대로 활용
		    var options = mapper.selectOptionsByGameId(gameId).stream()
		        .map(o -> new TodayGameRes.OptionBrief(
		                o.label(),
		                o.textContent()
		        ))
		        .toList();

		    // TODO: 나중에 실제 투표 여부 조회 로직 추가
		    String myVote = null;

		    return new TodayGameRes(gameId, title, options, myVote);
		}

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
	

	  public StatsRes stats(Long gameId) {
		  var counts = mapper.selectOptionVoteCounts(gameId);

		    long total = counts.stream()
		            .mapToLong(m -> {
		                Number n = (Number) m.get("CNT");   // ★ 대문자
		                return n == null ? 0L : n.longValue();
		            })
		            .sum();

		    Map<String, StatsRes.OptStat> options = new HashMap<>();
		    for (var row : counts) {
		        String label = (String) (row.get("LABEL") != null ? row.get("LABEL") : row.get("label"));
		        Number n = (Number) (row.get("CNT") != null ? row.get("CNT") : row.get("cnt"));
		        long cnt = n == null ? 0L : n.longValue();

		        double ratio = total == 0 ? 0 : (cnt * 100.0 / total);
		        options.put(label, new StatsRes.OptStat(cnt, ratio));
		    }

		    return new StatsRes(gameId, total, options);
		}

}
