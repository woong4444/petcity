package com.jjang051.petcity.admin.service;

import com.jjang051.petcity.admin.dao.AdminInactiveMemberDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminInactiveMemberService {
    private final AdminInactiveMemberDao adminInactiveMemberDao;

    @Transactional
    public int blockInactiveMembers() {
        return adminInactiveMemberDao.blockInactiveMembers();
    }

    @Transactional
    public int markBlockedMembersAsDeleted(){
        return adminInactiveMemberDao.markBlockedMembersAsDeleted();
    }

    @Transactional
    public int hardDeleteExpiredInactiveMembers() {
        return adminInactiveMemberDao.hardDeleteExpiredInactiveMembers();
    }
}
