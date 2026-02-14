package com.devlog.project.board.freeboard.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BoardImgDB {
	
    private Long imgNo;
    private String imgPath;
    private String imgOrig;
    private String imgRename;
    private int imgOrder;
    private Long boardNo;
}