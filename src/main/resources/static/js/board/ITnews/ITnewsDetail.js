const boardLike = document.getElementById("boardLike");
// 좋아요 버튼이 클릭 되었을 때
boardLike.addEventListener("click", (e) => {
  // 로그인 X
  if (!isAdmin && (!loginMemberNo || loginMemberNo === 0)) {
      alert("로그인 후 이용해주세요.");
      return;
    }

  let check; // 기존에 좋아요 X(빈하트) : 0, 기존에 좋아요 O(꽉찬하트) : 1

  // contains("클래스명") : 클래스가 있으면 true, 없으면 false
  if (boardLike.classList.contains("fa-regular")) {
    // 좋아요 X(빈하트)
    check = 0;
  } else {
    // 좋아요 O(꽉찬하트)
    check = 1;
  }

  // ajax로 서버에 제출할 파라미터를 모아둔 JS 객체
  const data = { memberNo: loginMemberNo, boardNo: boardNo, check: check };

  // ajax 비동기 통신
  fetch("/ITnews/like", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  })
    .then((resp) => resp.text())
    .then((count) => {
      console.log("서버로부터 받은 최신 좋아요 수 : " + count);

      if (count == -1) {
        alert("좋아요 처리 실패ㅠㅠ");
        return;
      }

      // 1. 하트 아이콘 토글 (빈 하트 <-> 꽉 찬 하트)
      const boardLike = document.getElementById("boardLike");
      boardLike.classList.toggle("fa-regular");
      boardLike.classList.toggle("fa-solid");

      // 2. 숫자 업데이트 (id="likeCount"인 요소를 직접 찾아서 텍스트 변경)
      const likeCountSpan = document.getElementById("likeCount");
      if (likeCountSpan) {
        likeCountSpan.innerText = count;
        console.log("화면 업데이트 완료!");
      } else {
        console.error("오류: id가 'likeCount'인 span 태그를 찾을 수 없습니다.");
      }

      // if(check == 0){ //기존에 좋아요를 X

      //   //게시글 작성자에게 알림 보내기
      //   sendNotification(
      //     "boardLike",
      //     location.pathname,
      //     boardNo, // 전역 변수 boardNo
      //     `<strong>${memberNickname}</strong>님이 <strong>${boardTitle}</strong> 게시글을 좋아합니다.`
      //   );
      // }
    })
    .catch((err) => {
      console.log(err);
    }); // 예외 발생 시 처리할 코드
});

// 게시글 삭제 !!!!
const deleteBtn = document.getElementById("delete-btn");

if (deleteBtn != null) {
  // null 체크 추가
  deleteBtn.addEventListener("click", () => {
    if (!confirm("정말로 삭제 하시겠습니까?")) return;

    fetch(`/ITnews/${boardNo}/delete`, {
      method: "PUT",
    })
      .then((resp) => resp.text())
      .then((result) => {
        if (result > 0) {
          alert("게시글이 삭제되었습니다.");
          location.href = "/ITnews";
        } else {
          alert("삭제 실패");
        }
      })
      .catch((err) => console.log(err));
  });
}

// 게시글 수정
const updateBtn = document.getElementById("update-btn");

if (updateBtn != null) {
  // 버튼이 화면에 있을 때만 (관리자일 때만)
  updateBtn.addEventListener("click", () => {
    // GET 방식으로 수정 페이지 요청 (쿼리스트링으로 게시글 번호 전달)
    location.href = `/ITnews/${boardNo}/update`;
  });
}

// 스크랩
  const scrapIcon = document.getElementById("scrapIcon");
  scrapIcon.addEventListener("click", (e) => {
  // 로그인 X
if (!isAdmin && (!loginMemberNo || loginMemberNo === 0)) {
    alert("로그인 후 이용해주세요.");
    return;
  }

  const data = {
    targetNo: boardNo,
    type: "1", // 1: 게시글
  };

  fetch("/ITnews/scrap", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  })
    .then((resp) => resp.text())
    .then((result) => {
      const scrapIcon = document.getElementById("scrapIcon");

      if (result > 0) {
        // 스크랩 성공(삽입)
        scrapIcon.src = "/images/common/scrap-filled.png";
        alert("스크랩 목록에 추가되었습니다.");
      } else if (result == 0) {
        // 스크랩 취소(삭제)
        scrapIcon.src = "/images/common/scrap-empty.png";
        alert("스크랩이 취소되었습니다.");
      } else {
        alert("처리 중 오류가 발생했습니다.");
      }
    })
    .catch((err) => console.log(err));
})

function openReportModal(targetMemberNo, targetNo) {

if (!isAdmin && (!loginMemberNo || loginMemberNo === 0)) {
    alert("로그인 후 이용해주세요.");
    return;
  }
  //<--------------------------- 함수 호출 시 타겟 회원 번호 넣어서 호출

  fetch(`/report/modal?memberNo=${targetMemberNo}`) //<------------------------------------------- 타켓 대상 회원 번호 넣어주셔야 합니다.
    .then((res) => res.text())
    .then((html) => {
      const root = document.getElementById("modal-root");
      root.innerHTML = html;
      const modal = root.querySelector("#reportModal");
      modal.classList.remove("display-none");

      modal.dataset.targetType = "BOARD"; //<-------------------------- 이 부분은 게시판이신 분들은 BOARD로 바꿔주세요
      modal.dataset.targetNo = targetNo; //<-------------------------- 게시글 번호도 이런 식으로 넘겨주세요
      bindReportModalEvents();
    });
}

const reOverlay = document.getElementById("reportModal");

reOverlay?.addEventListener("click", (e) => {
  //<------------------------------모달 닫기용 입니다 .
  if (e.target.id === "reportModal") {
    closeReportModal();
  }
});
