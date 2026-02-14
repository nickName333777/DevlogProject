package com.devlog.project.common.exception;

//사용자 정의 예외 만들기
//-> Exception관련 클래스를 상속 받으면 된다.

//checked exception   : 예외 처리 필수
//unchecked exception : 예외 처리 선택 (개발자/사용자 실수)

//tip. unchecked exception을 만들고 싶은 경우 : RuntimeException 상속 받아서 구현
public class ImageDeleteException extends RuntimeException{
	
	public ImageDeleteException() { // ctrl + space-bar + enter: 기본 생성자
		//
		super("이미지 삭제 중 예외 발생");
	}
	
	// 매개변수 생성자
	public ImageDeleteException(String message) { // new ImageDeleteException("merong2~")
		super(message);
	}

}
