function bindReportModalEvents() {
  const reportSelect = document.querySelector(".report-select");
  if (!reportSelect) return;

  const selected = reportSelect.querySelector(".selected");
  const options = reportSelect.querySelector(".options");

  let reportCode = 0;

  selected.addEventListener("click", () => {
    options.style.display =
      options.style.display === "block" ? "none" : "block";
  });

  options.querySelectorAll(".option").forEach((option) => {
    option.addEventListener("click", () => {
      selected.textContent = option.textContent;
      reportCode = option.dataset.reportCode;
      options.style.display = "none";
    });
  });

  document.addEventListener("click", (e) => {
    if (!reportSelect.contains(e.target)) {
      options.style.display = "none";
    }
  });

  const reportButton = document.getElementById("reportButton");
  const reportReason = document.getElementById("reportReason");
  const targetMemberNo = document.getElementById("targetMemberNo")?.value;
  const targetType = document.getElementById("reportModal").dataset.targetType; // 대상 타입
  const targetNo = document.getElementById("reportModal").dataset.targetNo; // 게시글 번호 or 메세지 번호

  reportButton?.addEventListener("click", () => {
    if (!reportCode) {
      alert("신고 유형을 선택해주세요.");
      return;
    }

    const data = {
      report_code: Number(reportCode),
      target_member_no: Number(targetMemberNo),
      report_reason: reportReason.value,
      target_type: targetType,
      target_no: Number(targetNo),
    };

    console.log("전송 데이터:", data);

    fetch("/report", {
      // 저는 JPA 써서 할 예정이라 음 컨트롤러에서 타켓 타입으로 일단 분리해 둘게요 한 번 확인해 주세요
      // 보드 쪽은 한 분이 서버쪽 코드 다 짜시면 그거 다 같이 쓰면 될 거에요 먼저 다 하시거나 본인이 맡아서 하실 분은 카톡방에 남겨주십셔
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    })
      .then((resp) => resp.text())
      .then((result) => {
        alert(result); // result 반환 결과는 처음하는 신고일 경우 신고가 성공적으로 접수되었습니다.  이미 신고한 게시글일 경우에는 이미 신고한 게시글입니다. 반환 받아 주세요
        closeReportModal();
      })
      .catch((e) => console.log(e));
  });
}

function closeReportModal() {
  const modal = document.getElementById("reportModal");
  modal.classList.add("display-none");
}

// 모달 띄우는 함수 입니다 각자 신고있는 페이지 js에 추가하셔서 창 띄우는거 호출하시면 됩니다.
// Chatting.html 상단에 해놓은 것 처럼 하시면 됩니다.
//      <div id="modal-root"></div>
//      <script th:src="@{/js/common/report.js}"></script> 이렇게 상단에 추가하시면 될 거ㅔㅇ요
//
//
//
//
//      다들 새해 복 많이 받으시고 화이팅
//

// function openReportModal(targetMemberNo, targetNo) {
//   //<--------------------------- 함수 호출 시 타겟 회원 번호 넣어서 호출

//   fetch(`/report/modal?memberNo=${targetMemberNo}`) //<------------------------------------------- 타켓 대상 회원 번호 넣어주셔야 합니다.
//     .then((res) => res.text())
//     .then((html) => {
//       const root = document.getElementById("modal-root");
//       root.innerHTML = html;
//       const modal = root.querySelector("#reportModal");
//       modal.classList.remove("display-none");

//       modal.dataset.targetType = "BOARD"; //<-------------------------- 이 부분은 게시판이신 분들은 BOARD로 바꿔주세요
//       modal.dataset.targetNo = targetNo; //<-------------------------- 게시글 번호도 이런 식으로 넘겨주세요
//       bindReportModalEvents();
//     });
// }

// const reOverlay = document.getElementById("reportModal");

// reOverlay?.addEventListener("click", (e) => {
//   //<------------------------------모달 닫기용 입니다 .
//   if (e.target.id === "reportModal") {
//     closeReportModal();
//   }
// });
