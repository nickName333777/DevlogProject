package com.devlog.project.chatting.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.devlog.project.chatting.entity.ChattingUser;
import com.devlog.project.chatting.entity.ChattingUserId;

@Repository
public interface ChattingRepository extends JpaRepository<ChattingUser, ChattingUserId> {


	@Query(value = """
			
			SELECT 
				CR.CHATTING_ROOM_NO AS "chattingRoomNo",
				CR.ROOM_TYPE AS "roomType",
				CU.PINNED_YN AS "pinnedYn",
				CASE
					WHEN CR.ROOM_TYPE = 'PRIVATE' THEN (SELECT MEMBER_NICKNAME FROM MEMBER M2
														JOIN CHATTING_USER U2 ON M2.MEMBER_NO = U2.MEMBER_NO
														WHERE U2.CHATTING_ROOM_NO = CR.CHATTING_ROOM_NO
														AND U2.MEMBER_NO != :memberNo)
					ELSE CR.CHATTING_ROOM_NAME
				END AS "displayName",
				
				CASE
					WHEN CR.ROOM_TYPE = 'PRIVATE' THEN (SELECT PROFILE_IMG FROM MEMBER M2
														JOIN CHATTING_USER U2 ON M2.MEMBER_NO = U2.MEMBER_NO
														WHERE U2.CHATTING_ROOM_NO = CR.CHATTING_ROOM_NO
														AND U2.MEMBER_NO != :memberNo)
					ELSE CR.ROOM_IMG
				END AS "roomImg",

				M.MESSAGE_CONTENT AS "lastMessage",
				
				M.SEND_TIME AS "lastMessageAt",
				
				(SELECT COUNT(*) FROM MESSAGE M2
					WHERE M2.CHATTING_ROOM_NO = CR.CHATTING_ROOM_NO
					AND M2.MESSAGE_NO > CU.LAST_READ_NO) AS "unreadCount"
			
			FROM CHATTING_ROOM CR
			JOIN CHATTING_USER CU
			ON CU.CHATTING_ROOM_NO = CR.CHATTING_ROOM_NO
			LEFT JOIN MESSAGE M ON M.MESSAGE_NO = 
			(SELECT MAX(m.MESSAGE_NO)
			FROM MESSAGE m
			WHERE m.CHATTING_ROOM_NO = CR.CHATTING_ROOM_NO)
			WHERE CU.MEMBER_NO = :memberNo
			""", nativeQuery = true)
	List<Object[]> selectChatList(@Param("memberNo") int memberNo);

}
