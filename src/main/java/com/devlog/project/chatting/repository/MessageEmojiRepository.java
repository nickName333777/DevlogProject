package com.devlog.project.chatting.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.devlog.project.chatting.dto.EmojiDTO;
import com.devlog.project.chatting.entity.Emoji;
import com.devlog.project.chatting.entity.Message;
import com.devlog.project.chatting.entity.MessageEmoji;
import com.devlog.project.member.model.entity.Member;

@Repository
public interface MessageEmojiRepository extends JpaRepository<MessageEmoji, Long> {
	
	
	
	// jpql * 없음 그룹바이 , 로 구분
	@Query("""
			select new com.devlog.project.chatting.dto.EmojiDTO(
			me.message.messageNo,
			me.emoji.emoji,
			count(me))
			from MessageEmoji me
			where me.message.messageNo in :messageNos
			group by me.message.messageNo, me.emoji.emojiCode, me.emoji.emoji
			order by me.emoji.emojiCode
			
			
			""")
	List<EmojiDTO> findEmojiCount(@Param("messageNos") List<Long> messageNos);
	
	
	// 해당 메세지에 이모지 달았느닞 조회
	Optional<MessageEmoji> findByMessageAndMember(Message message, Member member);
	
	

}
