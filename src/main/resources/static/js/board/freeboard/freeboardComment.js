console.log("freeboardComment.js loaded");


console.log("loginMemberNo:", loginMemberNo); // 로그인 회원번호 (비회원은 "")
console.log("boardNo:", boardNo); //  freeboard boardNo

// 댓글 목록 조회
function selectCommentList() {
    fetch("/board/freeboard/comment?boardNo=" + boardNo)
        .then((resp) => resp.json())
        .then((cList) => {
        console.log(cList);

        const commentList = document.getElementById("commentList"); // comment 담든 ul
        commentList.innerHTML = "";

        for (let comment of cList) {
            const commentRow = document.createElement("li"); // ul밑에 각 comment담는 li
            commentRow.classList.add("comment-row");

            // 작성자
            const commentItem = document.createElement("div");
            commentItem.classList.add("comment-item");

            // 프로필 이미지
            const profileImage = document.createElement("img");
            profileImage.classList.add("profile-img");
            if (comment.profileImg != null) {
                profileImage.setAttribute("src", comment.profileImg);
            } else {
                profileImage.setAttribute("src", "/images/user.png");
            }

            // 작성자 닉네임 + 작성 시간 + 답글 내용 container
            const commentInfo = document.createElement("div");
            commentInfo.classList.add("comment-info");

            const wrapperNicknameDate = document.createElement("div")
            // 작성자 닉네임
            const memberNickname = document.createElement("div");
            memberNickname.classList.add("comment-nickname")
            memberNickname.innerText = comment.memberNickname;
            // 작성일
            const commentDate = document.createElement("div");
            commentDate.classList.add("comment-date");
            commentDate.innerText = "(" + comment.cCreateDate + ")";
            wrapperNicknameDate.append(memberNickname, commentDate)


            // 댓글 내용
            const commentContent = document.createElement("div");
            commentContent.classList.add("comment-content");
            commentContent.innerText = comment.commentContent;

            commentInfo.append(wrapperNicknameDate, commentContent)

            // 여기까지 만든거 우선 commentItem에 담자.
            commentItem.append(profileImage, commentInfo)


            // 로그인한 회원번호와 댓글 작성자의 회원번호가 같을 때만 버튼 추가
            if (loginMemberNo && loginMemberNo == comment.memberNo) {
                const commentBtnArea = document.createElement("div");
                commentBtnArea.classList.add("comment-actions");

                // 수정 버튼
                const updateBtn = document.createElement("button");
                updateBtn.innerText = "수정";
                updateBtn.classList.add("action-btn")
                updateBtn.setAttribute(
                    "onclick",
                    "showUpdateComment(" + comment.commentNo + ", this)"


                );
                // 삭제 버튼
                const deleteBtn = document.createElement("button");
                deleteBtn.innerText = "삭제";
                deleteBtn.classList.add("action-btn")
                deleteBtn.setAttribute(
                    "onclick",
                    "deleteComment(" + comment.commentNo + ")"
                );


                commentBtnArea.append(updateBtn, deleteBtn);
                
                // 로그인한 회원번호와 댓글 작성자의 회원번호가 같을 때만 버튼 추가
                commentItem.append(commentBtnArea);
                
            }

            /////
            commentRow.append(commentItem);

            commentList.append(commentRow);
        }
        })
        .catch((err) => console.log(err));
}

// 댓글 등록
const addComment = document.getElementById("addComment");
const commentContent = document.getElementById("commentContent");

addComment.addEventListener("click", (e) => {
    console.log("댓글 등록 버튼 클릭");
    console.log("현재 loginMemberNo:", loginMemberNo);
    console.log("loginMemberNo 타입:", typeof loginMemberNo);

    // 로그인 체크
    if (!loginMemberNo || loginMemberNo == 0) {
        alert("로그인 후 이용해주세요.");
        return;
    }

    // 댓글 내용 체크
    if (commentContent.value.trim().length == 0) {
        alert("댓글을 작성한 후 버튼을 클릭해주세요.");
        commentContent.value = "";
        commentContent.focus();
        return;
    }

    // 비동기 요청을 통한 댓글 등록
    const data = {
        commentContent: commentContent.value,
        memberNo: loginMemberNo,
        boardNo: boardNo,
    };

    //fetch("/comment", {
    fetch("/board/freeboard/comment", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
    })
        .then((resp) => resp.text())
        .then((commentNo) => {
            if (commentNo > 0) {
                alert("댓글이 등록되었습니다.");
                console.log(commentNo);
                
                commentContent.value = "";

                // 댓글 목록 다시 불러와서 새로 등록된 댓글 보여주기 (화면새로고침)
                selectCommentList();
                
                // selectCommentList(); // 알림 요청
                // sendNotification(
                //     "insertComment",
                //     location.pathname + "?cn=" + commentNo, 
                //     boardNo,
                //      `<strong>${memberNickname}</strong>님이 <strong>${boardTitle}</strong> 게시글에 댓글을 작성했습니다.`

                // );
            } else {
                alert("댓글 등록에 실패했습니다...");
            }
        })
        .catch((err) => console.log(err));
});

// 댓글 삭제
function deleteComment(commentNo) {
    if (confirm("정말로 삭제 하시겠습니까?")) {
        const data = { commentNo: commentNo };

        fetch("/board/freeboard/comment", {
        method: "DELETE",
        headers: { "Content-type": "application/json" },
        body: JSON.stringify(data),
        })
        .then((resp) => resp.text())
        .then((result) => {
            if (result > 0) {
            alert("삭제되었습니다");
            selectCommentList();
            } else {
            alert("삭제 실패");
            }
        })
        .catch((err) => console.log(err));
    }
}

// 댓글 수정
let beforeCommentRow; // 수정할 댓글 (수정취소하면 이걸로 원복)

function showUpdateComment(commentNo, btn) {
    const temp = document.getElementsByClassName("update-textarea"); // 댓글 수정을 위해 생성되는 textarea입력 박스

    if (temp.length > 0) {
        if (confirm("다른 댓글이 수정 중입니다. 현재 댓글을 수정 하시겠습니까?")) {
            temp[0].parentElement.innerHTML = beforeCommentRow;
            // textarea의 parentElement가  commentRow임
        } else {
        return;
        }
    }

    const commentRow = btn.parentElement.parentElement.parentElement; // 클릭한 수정버튼을 포함하고 있는 댓글
    beforeCommentRow = commentRow.innerHTML; 

    let beforeContent = commentRow.children[0].children[1].children[1].innerHTML; // 수정할 댓글 내용

    commentRow.innerHTML = "";

    const textarea = document.createElement("textarea"); // 수정할 내용 입력할 textarea입력박스 생성
    textarea.classList.add("update-textarea");

    // XSS 방지 처리 해제
    beforeContent = beforeContent.replaceAll("&amp;", "&");
    beforeContent = beforeContent.replaceAll("&lt;", "<");
    beforeContent = beforeContent.replaceAll("&gt;", ">");
    beforeContent = beforeContent.replaceAll("&quot;", '"');

    textarea.value = beforeContent;
    commentRow.append(textarea); // 즉 현재 댓글 자리에 textarea 입력박스 생성

    ////////////////
    const commentBtnArea = document.createElement("div");
    commentBtnArea.classList.add("comment-actions");

    const updateBtn = document.createElement("button");
    updateBtn.innerText = "수정";
    updateBtn.classList.add("action-btn")
    updateBtn.setAttribute("onclick", "updateComment(" + commentNo + ", this)");

    const cancelBtn = document.createElement("button");
    cancelBtn.innerText = "취소";
    cancelBtn.classList.add("action-btn")
    cancelBtn.setAttribute("onclick", "updateCancel(this)");

    commentBtnArea.append(updateBtn, cancelBtn);
    commentRow.append(commentBtnArea);
}

// 댓글 수정 취소
function updateCancel(btn) {
    if (confirm("댓글 수정을 취소하시겠습니까?")) {
        btn.parentElement.parentElement.parentElement.innerHTML = beforeCommentRow; // 원래 댓글로 원복
        // 
        selectCommentList();
    }
}

// 댓글 수정
function updateComment(commentNo, btn) {
    const commentContent = btn.parentElement.previousElementSibling.value; //showUpdateComment()에서 더해준대로

    console.log("수정하는 댓글 내용:")
    console.log(commentContent);

    fetch("/board/freeboard/comment", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
        commentNo: commentNo,
        commentContent: commentContent,
        }),
    })
        .then((resp) => resp.text())
        .then((result) => {
        if (result > 0) {
            alert("댓글이 수정되었습니다.");
            selectCommentList();
        } else {
            alert("댓글 수정 실패");
        }
        })
        .catch((err) => console.log(err));
}

document.addEventListener("DOMContentLoaded", () => {
    selectCommentList();
});