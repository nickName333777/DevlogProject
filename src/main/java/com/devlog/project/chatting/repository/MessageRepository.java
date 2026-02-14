package com.devlog.project.chatting.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.devlog.project.chatting.dto.MessageDTO;
import com.devlog.project.chatting.dto.QueryMessageResponseDTO;
import com.devlog.project.chatting.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
	
	
	// 메세지 목록 조회함수
	@Query("""
			SELECT new com.devlog.project.chatting.dto.MessageDTO(
			    m.messageNo,
			    m.member.memberNo,
			    mem.memberNickname,
			    mem.profileImg,
			    m.type,
			    m.messageContent,
			    mi.imgPath,
			    m.sendTime,
			    m.status,
			    (
			        SELECT COUNT(cu)
			        FROM ChattingUser cu
			        WHERE cu.chatUserId.roomNo = m.chattingRoom.roomNo
			          AND cu.chatUserId.memberNo <> m.member.memberNo
			          AND cu.lastReadNo < m.messageNo
			    ),
			    CASE WHEN m.member.memberNo = :memberNo THEN 1 ELSE 0 END
			)
			FROM Message m
			JOIN m.member mem
			LEFT JOIN m.messageImg mi
			WHERE m.chattingRoom.roomNo = :roomNo
			AND m.sendTime >= (
			    SELECT cu2.joinDate
			    FROM ChattingUser cu2
			    WHERE cu2.chatUserId.roomNo = :roomNo
			      AND cu2.chatUserId.memberNo = :memberNo
			)
			ORDER BY m.messageNo
			""")
	List<MessageDTO> findByMessageList(Long roomNo, Long memberNo);
	
	
	
	// 안 읽은 메세지 수 계산
	@Query("""
			select count(m)
			from Message m
			where m.chattingRoom.roomNo = :roomNo
			and m.messageNo >
			    (select cu.lastReadNo
			     from ChattingUser cu
			     where cu.member.memberNo = :memberNo
			     and cu.chattingRoom.roomNo = :roomNo
			    )
			""")
	Long countUnreadMsg(Long memberNo, Long roomNo);


	
	// 마지막 읽은 메세지로 
	@Query("""
			select MAX(m.messageNo)
			from Message m
			where m.chattingRoom.roomNo = :roomNo
			""")
	Integer selectLastMessage(@Param("roomNo") Long roomNo);


//	private Long messageNo;
//	private String messageContent;
//	private LocalDateTime sendTime;
//	private String memberNickname;
//	private String profilePath;
	
	@Query("""
			select new com.devlog.project.chatting.dto.QueryMessageResponseDTO(
			m.messageNo,
			m.messageContent,
			m.sendTime,
			mem.memberNickname,
			mem.profileImg
			
			)
			from Message m
			JOIN m.member mem
			where m.chattingRoom.roomNo = :roomNo
				and m.messageContent LIKE concat('%', :query, '%')
				and m.type = 'TEXT'
				and (m.status is null or m.status != 'DEL')
				
			order by m.sendTime desc
			""")
	List<QueryMessageResponseDTO> searchMessage(Long roomNo, String query);


	
	@Query("""
			select m.chattingRoom.roomNo
			from Message m
			where m.messageNo = :targetId
			
			""")
	Long findRoomNoByMessageNo(Long targetId);
	
	
}
