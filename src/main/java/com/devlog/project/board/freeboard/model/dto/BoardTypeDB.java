package com.devlog.project.board.freeboard.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardTypeDB {
    // 게시판코드 
    private Integer boardCode;

    // 게시판이름 
    private String boardName;

    // 부모 게시판 코드 
    private Integer parentsBoardCode;
}
