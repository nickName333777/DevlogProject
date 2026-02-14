package com.devlog.project.member.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public class CommonEnums {

	public enum Status {
		Y, N;
		
		@JsonCreator
		public static Status from(String value) {
			return value == null ? null : Status.valueOf(value.toUpperCase());
		}
		
	}

}