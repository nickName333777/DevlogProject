package com.devlog.project.chatting.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChattingUserId implements Serializable {
	
	
	// JPA 복합키 (@EmbeddedId)
	// 채팅방(CHATTING_ROOM_NO)과 회원(MEMBER_NO)을 조합한 식별자
	// 채팅 참여( ChattingUser )를 유니크하게 식별하기 위한 키
	
	@Column(name = "CHATTING_ROOM_NO")
	private Long roomNo;
	
	
	@Column(name = "MEMBER_NO")
	private Long memberNo;
	
	
	
	
	
	@Override
	public boolean equals(Object o) {
	    if (this == o) return true;
	    // 현재 객체와 o가 같은 참조이면 true
	    if (o == null || getClass() != o.getClass()) return false;
	    // 비교 대상이 null이거나 클래스 타입이 다르면 같은 객체 x 
	    ChattingUserId that = (ChattingUserId) o;
	    
	    return Objects.equals(roomNo, that.roomNo)
	        && Objects.equals(memberNo, that.memberNo);
	}
	
    
    @Override
    public int hashCode() {
    	
        return Objects.hash(roomNo, memberNo);
    }
    
    
    
    /**
     * =========================
     * 복합 키 (@EmbeddedId) 설명
     * =========================
     *
     * 1. @EmbeddedId란?
     * - 객체지향적인 방식의 "식별자 클래스 매핑"
     * - 복합 키를 하나의 값 객체(Value Object)로 묶어서 사용
     * - 엔티티가 아닌 "식별자 클래스"를 직접 생성해서 사용한다
     *
     *   예)
     *   new ChattingUserId(roomNo, memberNo)
     *
     *
     * 2. 동일성 비교 vs 동등성 비교
     *
     * [동일성(Identity)]
     * - 두 객체의 참조 주소가 같은지 비교
     * - Object 클래스의 기본 equals() 동작 방식
     *
     *   예)
     *   a == b
     *
     * [동등성(Equality)]
     * - 두 객체가 가진 "값"이 같은지 비교
     * - equals()를 오버라이딩해서 구현
     *
     *
     * 3. 왜 복합 키는 equals() / hashCode()가 필수인가?
     *
     * - JPA의 영속성 컨텍스트는 엔티티를 관리할 때
     *   "식별자(PK)"를 키로 사용한다
     *
     * - 이때 사용하는 기준은
     *   참조 주소가 아니라 "식별자 값"
     *
     * - 하지만 Object의 기본 equals()는
     *   참조 주소(동일성)만 비교하기 때문에
     *   복합 키를 그대로 사용하면 문제가 발생한다
     *
     *
     * 4. 발생 가능한 문제
     *
     * - 같은 값의 복합 키인데 다른 객체로 인식됨
     * - 1차 캐시에서 엔티티를 찾지 못함
     * - Set, Map 등 컬렉션에서 중복 데이터 발생
     * - merge / remove 시 예상과 다른 동작
     *
     *
     * 5. 해결 방법 (필수 규칙)
     *
     * - 복합 키 클래스는 반드시 equals()와 hashCode()를
     *   "값 비교(동등성 비교)" 기준으로 오버라이딩해야 한다
     *
     * - 비교 대상은 반드시 "PK 필드만" 사용한다
     *
     * - 이것은 선택이 아니라 JPA 명세상 필수 규칙이다
     *
     */
	
	
}
