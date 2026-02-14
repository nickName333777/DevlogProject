package com.devlog.project.Scheduler.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.devlog.project.Scheduler.dto.Hot3DTO;

@Mapper
public interface MailMapper {

	List<Hot3DTO> selectHotList();

}
