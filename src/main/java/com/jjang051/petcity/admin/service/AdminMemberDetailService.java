package com.jjang051.petcity.admin.service;

import com.jjang051.petcity.admin.dao.AdminDao;
import com.jjang051.petcity.admin.dao.AdminMemberDetailDao;
import com.jjang051.petcity.admin.dto.AdminMemberDetailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminMemberDetailService {

    private final AdminMemberDetailDao adminMemberDetailDao;

    @Transactional(readOnly = true)
    public AdminMemberDetailDto getMemberDetail(Long memberId) {
        validateMemberId(memberId);
        return adminMemberDetailDao.findMemberDetailById(memberId);
    }

    @Transactional
    public void updateMemberAccountSettings(Long memberId, Long loginAdminId, String role, String status, String memberStatus) {

        validateMemberId(memberId);

        String normalizedRole = normalizeRole(role);
        String normalizedStatus = normalizeStatus(status);
        String normalizedMemberStatus = normalizeMemberStatus(memberStatus);

        if (Objects.equals(memberId, loginAdminId)) {
            boolean isRoleChanged = !"ADMIN".equals(normalizedRole);
            boolean isAccountDisabled = !"ACTIVE".equals(normalizedStatus);
            boolean isMemberDisabled = !"ACTIVE".equals(normalizedMemberStatus);
            if (isRoleChanged || isAccountDisabled || isMemberDisabled) {
                throw new IllegalArgumentException("현재 로그인한 관리자 계정의 권한이나 상태는 변경할 수 없습니다.");
            }
        }
        int updatedRows = adminMemberDetailDao.updateMemberAccountSettings(memberId, normalizedRole, normalizedStatus, normalizedMemberStatus);

        if (updatedRows != 1) {
            throw new IllegalArgumentException("회원 정보를 수정할 수 없습니다.");
        }
    }

    private void validateMemberId(Long memberId) {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("회원 번호가 올바르지 않습니다.");
        }
    }

    private String normalizeRole(String role) {
        if (role == null) {
            throw new IllegalArgumentException("회원 권한을 선택해 주세요.");
        }
        String normalizedRole = role.trim().toUpperCase();
        return switch (normalizedRole) {
            case "USER", "OWNER", "ADMIN" -> normalizedRole;
            default -> throw new IllegalArgumentException("올바르지 않은 회원 권한입니다.");
        };
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            throw new IllegalArgumentException("계정 상태를 확인해 주세요.");
        }
        String normalizeStatus = status.trim().toUpperCase();
        return switch (normalizeStatus) {
            case "ACTIVE", "BLOCKED", "DELETED" -> normalizeStatus;
            default -> throw new IllegalArgumentException("올바르지 않은 계정 상태입니다.");
        };
    }

    private String normalizeMemberStatus(String memberStatus) {
        if (memberStatus == null) {
            throw new IllegalArgumentException("회원 상태을 선택해 주세요.");
        }
        String normalizeMemberStatus = memberStatus.trim().toUpperCase();
        return switch (normalizeMemberStatus) {
            case "ACTIVE", "DELETE_PENDING", "DELETED" -> normalizeMemberStatus;
            default -> throw new IllegalArgumentException("올바르지 않은 회원 상태입니다.");
        };
    }

}
