package com.devlog.project.es.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.devlog.project.es.model.document.MemberStatsDoc;

@Mapper
public interface MemberStatsMapper {

    @Select("""
        SELECT
          MEMBER_NO        AS memberNo,
          MEMBER_DEL_FL    AS delFl,
          M_CREATE_DATE    AS joinedAt
        FROM MEMBER
    """)
    List<MemberStatsDoc> selectAllForStats();
}