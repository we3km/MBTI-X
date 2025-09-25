package com.kh.mbtix.balgame.model.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.mbtix.balgame.mapper.BalGameMapper;
import com.kh.mbtix.balgame.model.dto.BalGameDtos;
import com.kh.mbtix.balgame.model.dto.BalGameDtos.CreateGameReq;
import com.kh.mbtix.balgame.model.dto.BalGameDtos.CreateGameRes;
import com.kh.mbtix.balgame.model.dto.BalGameDtos.PastListRes;
import com.kh.mbtix.balgame.model.dto.BalGameDtos.StatsRes;
import com.kh.mbtix.balgame.model.dto.BalGameDtos.TodayGameRes;
import com.kh.mbtix.balgame.model.dto.BalGameDtos.TodayListRes;

@Service
public class BalGameService {
    private final BalGameMapper mapper;
    public BalGameService(BalGameMapper mapper){ this.mapper = mapper; }
    
    

    /**
     * 오늘의 게임 (페이지네이션)
     */
    public TodayListRes getTodayPaged(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        var games = mapper.selectTodayGamesPaged(offset, size);

        var content = games.stream().map(g -> {
            long gameId = g.gameId();
            var optionDtos = mapper.selectOptionsByGameId(gameId);
            var voteCounts = mapper.selectOptionVoteCounts(gameId).stream()
                .collect(Collectors.toMap(
                    r -> (String) r.get("LABEL"),
                    r -> ((Number) r.get("CNT")).longValue()
                ));
            var options = optionDtos.stream()
                .map(o -> new TodayGameRes.OptionBrief(
                    o.label(),
                    o.textContent(),
                    voteCounts.getOrDefault(o.label(), 0L)
                )).toList();
            String myVote = mapper.selectUserVoteLabel(gameId, userId);
            return new TodayGameRes(gameId, g.title(), options, myVote);
        }).toList();

        int total = mapper.countTodayGames();
        int totalPages = (int)Math.ceil(total / (double)size);

        return new TodayListRes(content, page, size, totalPages);
    }
    
    /**
     * 지난 게임 날짜 목록
     */
    public List<String> getPastDates() {
        return mapper.selectPastDates();
    }
    

    
    /**
     * 특정 날짜의 게임 목록 조회
     */
    public List<PastListRes.PastCard> getPastGamesByDate(String date) {
        return mapper.selectPastGamesByDate(date).stream().map(r -> {
            Long gameId = ((Number) r.get("GAME_ID")).longValue();
            var opts = mapper.selectOptionsByGameId(gameId);
            return new PastListRes.PastCard(
                gameId,
                (String) r.get("TITLE"),
                (String) r.get("START_AT"),
                opts.stream().map(o -> Map.of(
                    "label", o.label(),
                    "textContent", o.textContent()
                )).toList()
            );
        }).toList();
    }
    
    /**
     * 지난 게임 (날짜별 페이징)
     */
    public PastListRes getPast(String date, int page, int size) {
        int offset = (page - 1) * size;
        var rows = mapper.selectPastGamesPaged(date, offset, size);
        int total = mapper.countPastGames(date);
        int totalPages = (int)Math.ceil(total / (double)size);

        var cards = rows.stream().map(r -> {
            Long gameId = ((Number) r.get("GAME_ID")).longValue();
            var opts = mapper.selectOptionsByGameId(gameId);
            return new PastListRes.PastCard(
                gameId,
                (String) r.get("TITLE"),
                (String) r.get("START_AT"),
                opts.stream().map(o -> Map.of(
                    "label", o.label(),
                    "textContent", o.textContent()
                )).toList()
            );
        }).toList();

        return new PastListRes(cards, page, size, totalPages);
    }
    
    /**
     * 투표하기
     */
    @Transactional
    public void vote(Long gameId, String optionLabel, Long userId) {
        if (mapper.hasUserVoted(gameId, userId) > 0)
            throw new IllegalStateException("ALREADY_VOTED");

        var options = mapper.selectOptionsByGameId(gameId);
        var chosen = options.stream()
                .filter(o -> o.label().equals(optionLabel))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("INVALID_OPTION"));

        String snapMbti = mapper.selectUserMbtiCode(userId);
        mapper.insertVote(gameId, chosen.optionId(), userId, snapMbti);
    }
    
    /**
     * 투표 통계 (전체 투표 수 + 옵션별 비율 + MBTI 분포)
     */
    public StatsRes stats(Long gameId) {
        var counts = mapper.selectOptionVoteCounts(gameId);
        long total = counts.stream().mapToLong(m -> ((Number) m.get("CNT")).longValue()).sum();

        Map<String, StatsRes.OptStat> options = new HashMap<>();
        for (var row : counts) {
            String label = (String) row.get("LABEL");
            long cnt = ((Number) row.get("CNT")).longValue();
            double ratio = total == 0 ? 0 : (cnt * 100.0 / total);
            options.put(label, new StatsRes.OptStat(cnt, ratio));
        }

        var rows = mapper.selectOptionMbti(gameId);
        Map<String, Map<String, StatsRes.MbtiStat>> mbti = new HashMap<>();
        for (var r : rows) {
            String label = (String) r.get("LABEL");
            String code  = (String) r.get("MBTI");
            long   cnt   = ((Number) r.get("CNT")).longValue();
            double pct   = ((Number) r.get("PCT")).doubleValue() * 100.0;
            mbti.computeIfAbsent(label, k->new LinkedHashMap<>())
                .put(code, new StatsRes.MbtiStat(cnt, pct));
        }

        return new StatsRes(gameId, total, options, mbti);
    }
    
    /**
     * 게임 생성 (기존 Y → N 비활성화 후 새 게임 추가)
     */
    @Transactional
    public CreateGameRes createGame(CreateGameReq req) {
        mapper.deactivateAllActiveGames();
        mapper.insertGame(req.title());
        Long gameId = mapper.selectLastGameId();
        mapper.insertOption(gameId, "A", req.optionAText());
        mapper.insertOption(gameId, "B", req.optionBText());
        return new CreateGameRes(gameId, req.title());
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void resetDailyGame() {
        mapper.deactivateTodayGame();
    }
}


























