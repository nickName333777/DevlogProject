package com.devlog.project.chatting.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.devlog.project.chatting.chatenums.ChatEnums.Role;
import com.devlog.project.chatting.dto.MentionDTO;
import com.devlog.project.chatting.dto.ParticipantDTO;
import com.devlog.project.chatting.dto.ParticipantDTO.ChatListUpdateDTO;
import com.devlog.project.chatting.entity.ChattingUser;
import com.devlog.project.chatting.entity.ChattingUserId;

@Repository
public interface ChattingUserRepository extends JpaRepository<ChattingUser, ChattingUserId> {
	
	
	// 회원 있는지
	@Query("""
		SELECT cr.roomNo
		from ChattingUser cu
		join ChatRoom cr on cu.id.roomNo = cr.roomNo
		where cr.roomType = 'PRIVATE'
		and cu.id.memberNo in (:myMemberNo, :targetMemberNo)
		group by cr.roomNo
		having count(distinct  cu.id.memberNo) = 2
	
			""")
	Optional<Long> findPrivateRoomNo(@Param("myMemberNo") Long myMemberNo, @Param("targetMemberNo") Long targetMemberNo);
	
	
	
	// 채팅방 참여유저 조회
	@Query("""
			select new com.devlog.project.chatting.dto.ParticipantDTO(
			 m.memberNo, m.memberNickname, m.profileImg, 
			case when cu.role = 'OWNER' then 1 else 0 end)
			from ChattingUser cu
			join cu.member m
			where cu.chatUserId.roomNo = :roomNo
			
			""")
	List<ParticipantDTO> findByParticipants(@Param("roomNo") Long roomNo);


	
	// 개인채팅방일 경우 상대방 이름, 상대방 프로필 조회
	@Query("""
			select cu
			from ChattingUser cu
			where cu.chattingRoom.roomNo = :roomNo
			and cu.member.memberNo <> :memberNo
			""")
	ChattingUser findOpponent(@Param("roomNo") Long roomNo,@Param("memberNo") Long memberNo);


	
	
	// 해당 유저 마지막 읽은 메세지 업데이트
	@Modifying
	@Query("""
			update ChattingUser cu
			set cu.lastReadNo = (
				select MAX(m.messageNo)
				from Message m
				where m.chattingRoom.roomNo= :roomNo
			)
			where cu.member.memberNo = :memberNo
			and cu.chattingRoom.roomNo = :roomNo		
			""")
	void updateLastReadMessageNo(Long roomNo, Long memberNo);


	
	
	@Query("""
			select cu.member.memberNo
			from ChattingUser cu
			where cu.chattingRoom.roomNo = :roomNo
			""")
	List<Long> selectUsers(@Param("roomNo") Long roomNo);


	
	
	// 방장 여붖 ㅗ회
	boolean existsByChatUserIdRoomNoAndChatUserIdMemberNoAndRole(Long roomNo, Long memberNo, Role owner);


	
	// 업데이트 전 마지막 읽은 메세지 조회
	@Query("""
			select cu.lastReadNo
			from ChattingUser cu
			where cu.member.memberNo = :memberNo
			and cu.chattingRoom.roomNo = :roomNo
			""")
	Long selectLastReadMessagNo(Long roomNo, Long memberNo);

	
	
	
	
	@Query("""
		    select new com.devlog.project.chatting.dto.MentionDTO(
		        m.memberNo,
		        m.memberNickname,
		        m.profileImg
		    )
		    from ChattingUser cu
		    join cu.member m
		    where cu.chattingRoom.roomNo = :roomNo
		      and m.memberNo <> :memberNo
		      and m.memberNickname like concat('%', :keyword, '%')
		    order by m.memberNickname
		""")
	List<MentionDTO> findByUser(Long roomNo, String keyword, Long memberNo);


	
	@Query("""
			select count(cu)
			from ChattingUser cu
			where cu.chattingRoom.roomNo = :roomNo
			""")
	Long countUsers(Long roomNo);

	
	

	
	
	
	
	
	
	
	
	
}
