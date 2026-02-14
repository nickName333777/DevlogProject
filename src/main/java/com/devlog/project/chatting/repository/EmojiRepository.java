package com.devlog.project.chatting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.devlog.project.chatting.entity.Emoji;

@Repository
public interface EmojiRepository extends JpaRepository<Emoji , Long > {
	
	
	

}
