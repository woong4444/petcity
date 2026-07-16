package com.jjang051.petcity.admin.dao;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminInactiveMemberDao {
    int blockInactiveMembers();

    int markBlockedMembersAsDeleted();

    int hardDeleteExpiredInactiveMembers();

}
