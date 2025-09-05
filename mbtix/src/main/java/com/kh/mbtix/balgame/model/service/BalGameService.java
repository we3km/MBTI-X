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

import com.kh.mbtix.balgame.mapper.BalGameMapper;
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

	    long gameId = ((Number)g.get("GAME_ID")).longValue();
	    String title = (String) g.get("TITLE");
	    var options = mapper.selectOptionsByGameId(gameId).stream()
	      .map(m -> new TodayGameRes.OptionBrief((String)m.get("LABEL"),
	                                             (String)m.get("TEXT_CONTENT")))
	      .toList();

	    // 내 투표 여부는 간단히: 옵션×유저 카운트(실서비스는 별도 쿼리/캐시 추천)
	    String myVote = null; // 필요시 구현
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

	  public void vote(long gameId, String label, long userId){
	    // label -> optionId 조회
	    var opts = mapper.selectOptionsByGameId(gameId);
	    var opt = opts.stream().filter(m -> label.equals(m.get("LABEL"))).findFirst()
	                  .orElseThrow(() -> new NoSuchElementException("Invalid option"));
	    long optionId = ((Number)opt.get("OPTION_ID")).longValue();

	    String snapMbti = Optional.ofNullable(mapper.selectUserMbtiCode(userId))
	                              .orElseThrow(() -> new IllegalStateException("Set MBTI first"));
	    try {
	      mapper.insertVote(gameId, optionId, userId, snapMbti);
	    } catch (DuplicateKeyException e) {
	      // UNIQUE(GAME_ID, USER_ID) 위반
	      throw new IllegalStateException("ALREADY_VOTED");
	    }
	  }

	  public StatsRes stats(long gameId){
	    var totals = mapper.selectOptionTotals(gameId);   // label, cnt, pct
	    var mapTotals = totals.stream().collect(Collectors.toMap(
	        r -> (String)r.get("LABEL"),
	        r -> new long[]{ ((Number)r.get("CNT")).longValue(),
	                         Math.round(((Number)r.get("PCT")).doubleValue()*10) } // tmp
	    ));
	    long totalVotes = totals.stream().mapToLong(r -> ((Number)r.get("CNT")).longValue()).sum();

	    var mbtiRows = mapper.selectOptionMbti(gameId); // label, mbti, cnt, pct
	    Map<String, Map<String,Long>> mbtiMap = new HashMap<>();
	    for (var r : mbtiRows) {
	      String label = (String) r.get("LABEL");
	      String mbti  = (String) r.get("MBTI");
	      Long cnt     = ((Number) r.get("CNT")).longValue();
	      mbtiMap.computeIfAbsent(label,k->new LinkedHashMap<>()).put(mbti,cnt);
	    }

	    Map<String, StatsRes.OptStat> options = new LinkedHashMap<>();
	    for (var label : List.of("A","B")) {
	      long cnt = mapTotals.getOrDefault(label, new long[]{0,0})[0];
	      double ratio = totalVotes==0?0.0: Math.round((cnt*1000.0/totalVotes))/10.0;
	      options.put(label, new StatsRes.OptStat(cnt, ratio, mbtiMap.getOrDefault(label, Map.of())));
	    }
	    return new StatsRes(gameId, null, totalVotes, options);
	  }

}
