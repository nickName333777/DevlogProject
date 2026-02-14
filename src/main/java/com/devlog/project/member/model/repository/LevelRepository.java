package com.devlog.project.member.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.devlog.project.member.model.entity.Level;


public interface LevelRepository extends JpaRepository<Level, Integer>{
	
	
	
	// 현재 레벨 조회
	@Query("""
			select MAX(lv.levelNo)
			from Level lv
			where lv.requiredTotalExp <= :currentExp
			""")
	//Integer findByCurrentLevel(Integer currentExp); // 로그인 런타임오류 발생 d
	Integer findByCurrentLevel(@Param("currentExp") Integer currentExp);

}
