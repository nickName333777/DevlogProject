// 댓글 목록 조회 부분 수정
function selectCommentList() {
  fetch("/ITnews/comment?boardNo=" + boardNo)
    .then((resp) => resp.json())
    .then((cList) => {
      console.log("조회된 댓글 목록:", cList);
      const commentList = document.getElementById("commentList");
      commentList.innerHTML = "";

      for (let comment of cList) {
        const commentRow = document.createElement("li");
        commentRow.classList.add("comment-row");

        // 대댓글일 경우 'reply' 클래스 추가
        if (comment.parentCommentNo != 0) {
          commentRow.classList.add("reply");
        }

        commentRow.setAttribute("data-comment-id", comment.commentNo);
        commentRow.id = "comment-" + comment.commentNo;
        // 삭제된 댓글 처리
        if (comment.commentDeleteFlag === "Y") {
          const deleteMsg = document.createElement("p");
          deleteMsg.classList.add("comment-content");
          deleteMsg.innerText = "삭제된 댓글입니다.";
          deleteMsg.style.color = "#999";
          deleteMsg.style.fontStyle = "italic";
          deleteMsg.style.padding = "10px 0";

          commentRow.append(deleteMsg);
          commentList.append(commentRow);

          continue;
        }
        // --- 작성자 정보---
        const commentWriter = document.createElement("p");
        commentWriter.classList.add("comment-writer");
        const profileImage = document.createElement("img");
        profileImage.classList.add("comment-profile");
        // 프로필 이미지가 없거나 빈 값일 경우 기본 이미지 설정
        let imagePath = comment.profileImg;
        if (imagePath == null || imagePath == "") {
            imagePath = "/images/user.png"; // 기본 프로필 이미지 경로
        }

        profileImage.setAttribute("src", imagePath);
        const memberNickname = document.createElement("span");
        memberNickname.classList.add("comment-nickname");
        memberNickname.innerText = comment.memberNickname;
        const commentDate = document.createElement("span");
        commentDate.classList.add("comment-date");
        commentDate.innerText = "(" + comment.commentCreateDate + ")";

        // 댓글이 수정됨일때 옆에 (수정됨)표시
        if (comment.modifyYN === "Y") {
          const modifySpan = document.createElement("span");
          modifySpan.innerText = " (수정됨)";
          modifySpan.style.fontSize = "0.85em";
          modifySpan.style.color = "#999";
          modifySpan.style.marginLeft = "5px";
          commentDate.append(modifySpan);
        }

        commentWriter.append(profileImage, memberNickname, commentDate);

        // 댓글 내용
        const commentContent = document.createElement("p");
        commentContent.classList.add("comment-content");
        commentContent.innerHTML = comment.commentContent;

        // --- 추가: 반응 영역 (좋아요/싫어요) ---
        const commentActions = document.createElement("div");
        commentActions.classList.add("comment-actions");

        commentActions.innerHTML = `
        <button class="comment-good-btn" data-type="good">
            <img src="/images/board/ITnews/comment_good.svg" alt="좋아요" class="comment-icon" 
                style="filter: ${
                  comment.likeCheck == 1 ? "grayscale(0%)" : "grayscale(100%)"
                }">
            <span class="good-count">${comment.likeCount}</span>
        </button>

        <button class="comment-bad-btn" data-type="bad">
            <img src="/images/board/ITnews/comment_bad.svg" alt="싫어요" class="comment-icon" 
                style="filter: ${
                  comment.badCheck == 1 ? "grayscale(0%)" : "grayscale(100%)"
                }">
            <span class="count">${comment.badCount}</span>
        </button>
        `;

        commentRow.append(commentWriter, commentContent, commentActions);
        // 버튼 영역(div)은 무조건 생성
        const btnArea = document.createElement("div");
        btnArea.classList.add("comment-btn-area");

        // 답글 버튼 생성 (로그인한 사람이라면 '남의 댓글'에도 보여야 함)
        // parentCommentNo가 0인 경우(원댓글)에만 답글을 달 수 있게 설정
        if (loginMemberNo && loginMemberNo != 0) {
          if (comment.parentCommentNo == 0) {
            const replyBtn = document.createElement("button");
            replyBtn.innerText = "답글";
            replyBtn.classList.add("reply-btn");
            // 로그인한 유저라면 누구나 답글 버튼을 클릭할 수 있음
            replyBtn.onclick = () =>
              showInsertComment(comment.commentNo, replyBtn);
            btnArea.append(replyBtn);
          }
        }

        // 수정/삭제 버튼 생성 (작성자 본인일 때만 추가)
        if (loginMemberNo && loginMemberNo == comment.memberNo) {
          const updateBtn = document.createElement("button");
          updateBtn.innerText = "수정";
          updateBtn.onclick = () =>
            showUpdateComment(comment.commentNo, updateBtn);

          const deleteBtn = document.createElement("button");
          deleteBtn.innerText = "삭제";
          deleteBtn.onclick = () => deleteComment(comment.commentNo);

          btnArea.append(updateBtn, deleteBtn);
        }

        // 답글 버튼 (부모 댓글일 때만)
        if (comment.parentCommentNo == 0) {
        }
        commentRow.append(btnArea);
        commentList.append(commentRow);
      }
      scrollToHashIfExists();
    });
}

// 댓글 등록
const addComment = document.getElementById("addComment");
const commentContent = document.getElementById("commentContent");

addComment.addEventListener("click", (e) => {
  // console.log("댓글 등록 버튼 클릭");
  // console.log("현재 loginMemberNo:", loginMemberNo);
  // console.log("loginMemberNo 타입:", typeof loginMemberNo);

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

  const data = {
    memberNo: loginMemberNo,
    boardNo: boardNo,
    commentContent: commentContent.value,
  };
  console.log(data);

  fetch("/ITnews/comment", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  })
    .then((resp) => resp.text())
    .then((result) => {
      const commentNo = Number(result);

      if (commentNo > 0) {
        alert("댓글이 등록되었습니다.");
        console.log(commentNo);

        commentContent.value = "";
        selectCommentList();
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
    console.log("삭제 요청 번호:", commentNo);

    fetch("/ITnews/comment", {
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

// ---------------------------------------------------------------------------
// 댓글 수정 화면 전환
let beforeCommentRow; // 원래 상태 저장 변수

function showUpdateComment(commentNo, btn) {
  const temp = document.getElementsByClassName("update-textarea"); // 댓글 수정 한개만 열릴 수 있게
  if (temp.length > 0) {
    // 수정이 한 개 이상 열려 있는 경우
    if (confirm("다른 댓글이 수정 중입니다. 현재 댓글을 수정하겠습니까?")) {
      temp[0].parentElement.innerHTML = beforeCommentRow;
    } else {
      return;
    }
  }

  // 댓글 수정이 클릭된 행을 선택
  const commentRow = btn.parentElement.parentElement; // 수정 버튼의 부모의 부모

  // 행 내용 삭제 전 현재 상태 저장
  beforeCommentRow = commentRow.innerHTML;

  // 댓글에 작성되어있던 내용만
  let beforeContent = commentRow.children[1].innerHTML;

  // 댓글 행 내부 내용 모두 삭제
  commentRow.innerHTML = "";

  // textarea 요소 생성 + 클래스 추가 + 내용 추가
  const textarea = document.createElement("textarea");
  textarea.classList.add("update-textarea");

  // ******************************************
  // XSS 방지 처리 해제
  beforeContent = beforeContent.replaceAll("&amp;", "&");
  beforeContent = beforeContent.replaceAll("&lt;", "<");
  beforeContent = beforeContent.replaceAll("&gt;", ">");
  beforeContent = beforeContent.replaceAll("&quot;", '"');

  textarea.value = beforeContent;

  // commentRow에 생성된 textarea 추가
  commentRow.append(textarea);

  // 버튼 영역 + 수정/취소 버튼 생성
  const commentBtnArea = document.createElement("div");
  commentBtnArea.classList.add("comment-btn-area");

  const updateBtn = document.createElement("button");
  updateBtn.innerText = "수정";
  updateBtn.setAttribute("onclick", "updateComment(" + commentNo + ", this)");

  const cancelBtn = document.createElement("button");
  cancelBtn.innerText = "취소";
  cancelBtn.setAttribute("onclick", "updateCancel(this)");

  // 버튼영역에 버튼 추가 후
  //  commentRow(행)에 버튼영역 추가
  commentBtnArea.append(updateBtn, cancelBtn);
  commentRow.append(commentBtnArea);
}

// -----------------------------------------------------------------------------------
// 댓글 수정 취소
function updateCancel(btn) {
  // 매개변수 btn : 클릭된 취소 버튼
  // 전역변수 beforeCommentRow : 수정 전 원래 행(댓글)을 저장한 변수

  if (confirm("댓글 수정을 취소하시겠습니까?")) {
    btn.parentElement.parentElement.innerHTML = beforeCommentRow;
  }
}

// -----------------------------------------------------------------------------------
// 댓글 수정(AJAX)
function updateComment(commentNo, btn) {
  // 새로 작성된 댓글 내용 얻어오기
  const commentContent = btn.parentElement.previousElementSibling.value;

  fetch("/ITnews/comment", {
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

// 답글 작성 화면 추가
// -> 답글 작성 화면은 전체 화면에 1개만 존재 해야한다!

function showInsertComment(parentCommentNo, btn) {
  // 부모 댓글 번호, 클릭한 답글 버튼

  // ** 답글 작성 textarea가 한 개만 열릴 수 있도록 만들기 **
  const temp = document.getElementsByClassName("commentInsertContent");

  if (temp.length > 0) {
    // 답글 작성 textara가 이미 화면에 존재하는 경우

    if (
      confirm(
        "다른 답글을 작성 중입니다. 현재 댓글에 답글을 작성 하시겠습니까?"
      )
    ) {
      temp[0].nextElementSibling.remove(); // 버튼 영역부터 삭제
      temp[0].remove(); // textara 삭제 (기준점은 마지막에 삭제해야 된다!)
    } else {
      return; // 함수를 종료시켜 답글이 생성되지 않게함.
    }
  }

  // 답글을 작성할 textarea 요소 생성
  const textarea = document.createElement("textarea");
  textarea.classList.add("commentInsertContent");

  // 답글 버튼의 부모의 뒤쪽에 textarea 추가
  // after(요소) : 뒤쪽에 추가
  btn.parentElement.after(textarea);

  // 답글 버튼 영역 + 등록/취소 버튼 생성 및 추가
  const commentBtnArea = document.createElement("div");
  commentBtnArea.classList.add("comment-btn-area");

  const insertBtn = document.createElement("button");
  insertBtn.innerText = "등록";
  insertBtn.setAttribute(
    "onclick",
    "insertChildComment(" + parentCommentNo + ", this)"
  );

  const cancelBtn = document.createElement("button");
  cancelBtn.innerText = "취소";
  cancelBtn.setAttribute("onclick", "insertCancel(this)");

  // 답글 버튼 영역의 자식으로 등록/취소 버튼 추가
  commentBtnArea.append(insertBtn, cancelBtn);

  // 답글 버튼 영역을 화면에 추가된 textarea 뒤쪽에 추가
  textarea.after(commentBtnArea);
}

// 답글 취소
function insertCancel(btn) {
  // 취소
  btn.parentElement.previousElementSibling.remove(); // 취소의 부모의 이전 요소(textarea) 제거
  btn.parentElement.remove(); // 취소의 부모 요소(comment-btn-area) 제거
}

// 답글 등록
function insertChildComment(parentCommentNo, btn) {
  // 답글 내용
  const commentContent = btn.parentElement.previousElementSibling.value;

  // 답글 내용이 작성되지 않은 경우
  if (commentContent.trim().length == 0) {
    alert("답글 작성 후 등록 버튼을 클릭해주세요.");
    btn.parentElement.previousElementSibling.value = "";
    btn.parentElement.previousElementSibling.focus();
    return;
  }

  const data = {
    commentContent: commentContent,
    memberNo: loginMemberNo,
    boardNo: boardNo,
    parentCommentNo: parentCommentNo,
  };

  fetch("/ITnews/comment", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data), // JS 객체 -> JSON 파싱
  })
    .then((resp) => resp.text())
    .then((commentNo) => {
      if (commentNo > 0) {
        // 등록 성공
        alert("답글이 등록되었습니다.");

        commentContent.value = ""; // 작성했던 댓글 삭제
        selectCommentList(); // 비동기 댓글 목록 조회 함수 호출
        // -> 새로운 댓글이 추가되어짐

        // 알림 클릭 시 이동하는 url에 ?cn = 댓글 번호 추가
        // -> 알림 클릭 시 작성된 댓글 또는 답글 위치로 바로 이동

        // 댓글을 작성한 경우
        // -> {닉네임}님이 답글을 작성했습니다.
        // sendNotification(
        //     "insertChildComment",
        //     location.pathname + "?cn=" + commentNo,
        //     parentNo, /* 부모 댓글을 불러와야 답글인지 알기 때문에 */
        //      `<strong>${memberNickname}</strong>님이 답글을 작성했습니다.`

        // )

        // 답글(대댓글)을 작성한 경우
        // -> {닉네임}님이 답글을 작성했습니다.
      } else {
        // 실패
        alert("답글 등록에 실패했습니다...");
      }
    })
    .catch((err) => console.log(err));
}

function scrollToHashIfExists() {
  const hash = location.hash; // 예: "#comment-6"
  if (!hash) return;

  const target = document.querySelector(hash);
  if (target) {
    target.scrollIntoView({ behavior: "smooth", block: "center" });
  }
}

// --- 좋아요 / 싫어요 클릭 이벤트 (수정된 안전한 버전) ---
// 페이지가 완전히 로드된 후 이벤트를 등록합니다.
document.addEventListener("DOMContentLoaded", () => {
  // 요소가 있는지 확인 후 등록 (혹은 document에 위임)
  const commentList = document.getElementById("commentList");

  if (commentList) {
    commentList.addEventListener("click", (e) => {
      // 클릭된 요소가 버튼인지, 혹은 버튼 안의 이미지/스팬인지 확인
      const btn = e.target.closest(".comment-good-btn, .comment-bad-btn");
      if (!btn) return;

      // 로그인 체크 (loginMemberNo 변수가 전역에 선언되어 있어야 함)
      if (
        typeof loginMemberNo === "undefined" ||
        !loginMemberNo ||
        loginMemberNo == 0
      ) {
        alert("로그인 후 이용해주세요.");
        return;
      }

      const commentRow = btn.closest(".comment-row");
      const commentNo = commentRow.dataset.commentId;
      const type = btn.dataset.type; // 'good' 또는 'bad'
      const status = type === "good" ? 1 : 2; // 1:좋아요, 2:싫어요

      const data = {
        commentNo: commentNo,
        memberNo: loginMemberNo,
        status: status,
      };

      fetch("/ITnews/comment/like", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      })
        .then((resp) => resp.json())
        .then((result) => {
          // 숫자 업데이트
          commentRow.querySelector(".good-count").innerText = result.likeCount;
          commentRow.querySelector(".comment-bad-btn .count").innerText =
            result.dislikeCount;

          // 시각적 피드백 (이미지 필터 변경)
          const goodImg = commentRow.querySelector(".comment-good-btn img");
          const badImg = commentRow.querySelector(".comment-bad-btn img");

          goodImg.style.filter = "grayscale(100%)";
          badImg.style.filter = "grayscale(100%)";

          if (result.currentStatus == 1) {
            goodImg.style.filter = "grayscale(0%)";
          } else if (result.currentStatus == 2) {
            badImg.style.filter = "grayscale(0%)";
          }
        })
        .catch((err) => console.log("좋아요 처리 중 오류:", err));
    });
  }
});

// 서비스 호출
selectCommentList();
