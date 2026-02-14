package com.devlog.project.main.controller.websocket.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.devlog.project.member.model.dto.FollowDTO;

@Mapper
public interface OnlineMapper {

	List<FollowDTO> selectFollow(Long memberNo);

	List<FollowDTO> selectFollowers(Long targetMemberNo);
}
