// 상각_07-19: 탈퇴·복구 감사 이력 기록 및 1년 경과 자료 자동 삭제
package com.jjang051.petcity.member.service;

import com.jjang051.petcity.member.dao.MemberSecurityAuditMapper;
import com.jjang051.petcity.member.dto.MemberDto;
import com.jjang051.petcity.member.dto.MemberSecurityAuditDto;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberSecurityAuditService {
    private final MemberSecurityAuditMapper mapper;

    @Transactional
    public void record(MemberDto member, String eventType, String eventResult,
                       String eventReason, String ipAddress) {
        if (member == null) return;
        MemberSecurityAuditDto audit = new MemberSecurityAuditDto();
        audit.setMemberId(member.getMemberId());
        audit.setLoginId(member.getLoginId());
        audit.setNickname(member.getNickname());
        audit.setLoginType(member.getLoginType());
        audit.setEventType(eventType);
        audit.setEventResult(eventResult);
        audit.setEventReason(eventReason);
        audit.setIpAddress(normalizeIp(ipAddress));
        mapper.insert(audit);
    }

    public List<MemberSecurityAuditDto> findAll() {
        return mapper.findAll();
    }

    public List<MemberSecurityAuditDto> findByMemberId(Long memberId) {
        return mapper.findByMemberId(memberId);
    }

    @Scheduled(cron = "0 20 4 * * *", zone = "Asia/Seoul")
    @Transactional
    public void deleteExpiredAuditHistory() {
        mapper.deleteExpired();
    }

    private String normalizeIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) return null;
        String value = ipAddress.split(",")[0].trim();
        return value.length() > 64 ? value.substring(0, 64) : value;
    }
}
