const modal = document.getElementById("reportModal");
const moveButton = document.getElementById("moveButton");
const modalMessageContent = document.getElementById("modalMessageContent");

let currentTargetUrl = null;
let currentTargetType = null;
let currentReportId = null;
let currentMessageNo = null;

// 모달 내부 필요한 요소들
const modalReportNo = document.getElementById("modalReportNo");
const modalReportType = document.getElementById("modalReportType");
const modalReportDate = document.getElementById("modalReportDate");
const modalReason = document.getElementById("modalReason");
const modalTargetName = document.getElementById("modalTargetName");


// 신고 목록 행 클릭 시
function openModal(row) {
  const reportId = row.dataset.reportNo;
  currentReportId = reportId;

  const targetType = row.dataset.targetType;
  const reportType = row.dataset.reportType;
  const reportReason = row.dataset.reportReason;
  const targetName = row.dataset.target;
  const reportDate = row.dataset.reportDate;
  const messageContent = row.dataset.messageContent;
  const reportStatus = row.querySelector(".status-select")?.value;

  currentTargetUrl = row.dataset.targetUrl;
  currentTargetType = targetType;
  currentMessageNo = row.dataset.messageNo;
  
  modalReportNo.value = reportId;
  modalReportType.value = reportType;
  modalReportDate.value = reportDate;
  modalReason.value = reportReason;
  modalTargetName.textContent = targetName;

  // 버튼 기본 초기화
  moveButton.disabled = false;
  moveButton.classList.remove("disabled");
  moveButton.textContent = "해당 게시글 이동";

  if (targetType === "BOARD") {
    modalMessageContent.classList.remove("open");
    modalMessageContent.textContent = "";
    document.getElementById("deleteMessageBtn").style.display = "none";

    // 처리완료
    if (reportStatus !== "PENDING") {
      moveButton.disabled = true;
      moveButton.classList.add("disabled");
      moveButton.textContent = "이미 처리된 신고입니다";
    }

    // 이미 삭제된 게시글
    if (!currentTargetUrl) {
      moveButton.disabled = true;
      moveButton.classList.add("disabled");
      moveButton.textContent = "삭제된 게시글입니다";
    }
  } else {
    // 메시지 신고
    moveButton.textContent = "신고된 메시지 확인";
    modalMessageContent.textContent =
      messageContent || "신고된 메시지 내용이 없습니다.";

    document.getElementById("deleteMessageBtn").style.display = "inline-block";

    document.getElementById("deleteMessageBtn").style.display = "inline-block";

  }

  modal.style.display = "flex";
}





// 모달 닫기
function closeModal() {
  modal.style.display = "none";
}

modal.addEventListener("click", function (e) {
  if (e.target.classList.contains("modal-overlay")) {
    closeModal();
  }
});

// // 중요!!!!!!! 해당 게시글로 실제로 이동 채팅이면 메시지 보여주기
// moveButton.addEventListener("click", function () {
//   if (!currentTargetId) return;

//   if (currentTargetType === "BOARD") {
//     location.href = `/board/detail?boardId=${currentTargetId}`;
//   } else {
//     location.href = `/chat/detail?chatId=${currentTargetId}`;
//   }
// });

moveButton.addEventListener("click", function () {
  if (!currentTargetType) return;

  if (currentTargetType === "BOARD") {
    location.href = currentTargetUrl;
    return;
  }

  
  // 채팅 신고
  modalMessageContent.classList.toggle("open");
});

const deleteBtn = document.getElementById("deleteMessageBtn");

deleteBtn.addEventListener("click", async () => {
  if (!currentMessageNo) {
    alert("삭제할 메시지가 없습니다.");
    return;
  }

  if (
    !confirm(
      "정말 이 메시지를 삭제하시겠습니까?\n삭제 시 신고는 자동으로 처리완료됩니다."
    )
  ) {
    return;
  }

  try {

    // 채팅 메세지 삭제
    const res = await fetch(
      `/devtalk/delete-msg?messageNo=${currentMessageNo}`,
      { method: "GET" }
    );

    if (!res.ok) throw new Error("메시지 삭제 실패");

    // 성공하면 신고 완료처리 하는 내 컨트로러로 요청 보냄
    const resolveRes = await fetch("/manager/dashboard/report/resolve", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        reportIds: [currentReportId],
        status: "RESOLVED",
      }),
    });

    if (!resolveRes.ok) throw new Error("신고 처리 실패");

    alert("메시지가 삭제되었고, 신고가 처리완료되었습니다.");

    closeModal();

    location.reload();
  } catch (e) {
    console.error(e);
    alert("처리 중 오류가 발생했습니다.");
  }
});

// 신고 체크박스 및 수동처리!!!!!!!! 테스트코드 옮겨요..
document.getElementById("checkAll")?.addEventListener("change", function () {
  const checked = this.checked;

  document.querySelectorAll(".row-check").forEach((cb) => {
    if (!cb.disabled) {
      cb.checked = checked;
    }
  });
});

function confirmSingleResolve(selectEl) {
  const row = selectEl.closest("tr");
  const reportId = row.dataset.reportNo;
  const newStatus = selectEl.value;

  if (newStatus === "PENDING") return;

  if (!confirm("해당 신고를 처리 완료로 변경하시겠습니까?")) {
    selectEl.value = "PENDING";
    return;
  }

  fetch("/manager/dashboard/report/resolve", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      reportIds: [reportId],
      status: newStatus,
    }),
  })
    .then((res) => {
      if (!res.ok) throw new Error();
      alert("처리가 완료되었습니다.");
      location.reload();
    })
    .catch(() => {
      alert("처리 중 오류가 발생했습니다.");
      selectEl.value = "PENDING";
    });
}


function confirmBatchResolve() {
  const reportIds = Array.from(document.querySelectorAll(".row-check:checked"))
    .filter((cb) => !cb.disabled) // 메시지는 일괄처리 안됩니다잉 직접 보세요
    .map((cb) => cb.value);

  if (reportIds.length === 0) {
    alert("처리할 게시글 신고를 선택해주세요.");
    return;
  }

  if (!confirm("선택한 게시글 신고를 모두 처리 완료로 변경하시겠습니까?"))
    return;

  fetch("/manager/dashboard/report/resolve", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      reportIds: reportIds,
      status: "RESOLVED",
    }),
  })
    .then((res) => {
      if (!res.ok) throw new Error();
      alert("선택한 게시글 신고가 처리 완료되었습니다.");
      location.reload();
    })
    .catch(() => {
      alert("처리 중 오류가 발생했습니다.");
    });
}

let page = 1; // 0페이지는 이미 렌더됨
let loading = false;
let lastPage = false;

const tbody = document.getElementById("reportTbody");
const container = document.querySelector(".main"); // 스크롤 기준

container.addEventListener("scroll", () => {
  if (loading || lastPage) return;

  const nearBottom =
    container.scrollTop + container.clientHeight >=
    container.scrollHeight - 100;

  if (nearBottom) {
    loadMoreReports();
  }
});

function loadMoreReports() {
  loading = true;

  fetch(`/manager/report/list?page=${page}`)
    .then((res) => res.json())
    .then((data) => {
      if (data.content.length === 0) {
        lastPage = true;
        return;
      }

      data.content.forEach((report) => {
        tbody.insertAdjacentHTML("beforeend", createRow(report));
      });

      page++;
    })
    .finally(() => (loading = false));
}

// 무한 스크롤~~~~
function createRow(report) {
  return `
<tr data-report-no="${report.reportId}"
    data-target-id="${report.targetId}"
    data-message-no="${report.messageNo}"
    data-target-type="${report.targetType}"
    data-target-url="${report.targetUrl}"
    data-report-type="${report.reportType}"
    data-report-reason="${report.reportReason}"
    data-reporter="${report.reporterNickname}"
    data-target="${report.targetNickname}"
    data-report-date="${report.reportDate}"
    data-message-content="${report.messageContent}"
    onclick="openModal(this)">

  <td onclick="event.stopPropagation()">
    <input type="checkbox"
      class="row-check ${
        report.targetType !== "BOARD" || report.status !== "PENDING"
          ? "row-check--disabled"
          : ""
      }"
      value="${report.reportId}" />
  </td>

  <td>${report.reportId}</td>
  <td>${report.reportType}</td>
  <td>${report.targetType}</td>
  <td>${report.reportReason}</td>
  <td>${report.reporterNickname}</td>
  <td>${report.targetNickname}</td>
  <td>${formatDate(report.reportDate)}</td>
  <td>${report.processDate ? formatDate(report.processDate) : "-"}</td>

  <td>
    <select class="status-select" ${
      report.status !== "PENDING" ? "disabled" : ""
    }
      onchange="confirmSingleResolve(this)">
      <option value="PENDING" ${
        report.status === "PENDING" ? "selected" : ""
      }>처리전</option>
      <option value="RESOLVED" ${
        report.status === "RESOLVED" ? "selected" : ""
      }>처리완료</option>
      <option value="REJECTED" ${
        report.status === "REJECTED" ? "selected" : ""
      }>반려</option>
    </select>
  </td>
</tr>`;
}
