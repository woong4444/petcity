package com.jjang051.petcity.admin.service;

import com.jjang051.petcity.admin.dao.AdminDao;
import com.jjang051.petcity.admin.dto.AdminMemberDetailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDetailService {

    private final AdminDao adminDao;

    public AdminMemberDetailDto getMemberDetail(Long memberId) {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("회원 번호가 올바르지 않습니다.");
        }
        return adminDao.findMemberDetailById(memberId);
    }


}
