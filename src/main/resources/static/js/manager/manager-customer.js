console.log("manager-customer.js loaded");

const searchInput = document.querySelector(".search-box input");
const searchBtn = document.querySelector(".search-box button");
const tableBody = document.getElementById("memberTableBody");

function renderMemberTable(members) {
  tableBody.innerHTML = "";

  if (!members || members.length === 0) {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td colspan="7" style="text-align:center; color:#999;">
        일치하는 회원이 없습니다.
      </td>
    `;
    tableBody.appendChild(tr);
    return;
  }

  members.forEach((m) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${m.memberNo}</td>
      <td>${m.memberName}</td>
      <td>${m.memberNickname}</td>
      <td>${m.memberEmail}</td>
      <td>${m.memberTel ?? "-"}</td>
      <td>${m.mCreateDate?.substring(0, 10) ?? "-"}</td>
      <td>${m.memberAdmin}</td>
    `;
    tableBody.appendChild(tr);
  });
}

searchBtn.addEventListener("click", () => {
  const email = searchInput.value.trim();

  // 검색어 없으면 전체 목록 보여주기
  if (!email) {
    location.reload();
    return;
  }

  fetch("/api/manager/selectMemberbyMemberEmail", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email }),
  })
    .then((resp) => {
      if (!resp.ok) throw new Error();
      return resp.json();
    })
    .then((member) => {
      renderMemberTable(member ? [member] : []);
    })
    .catch(() => {
      renderMemberTable([]);
    });
});

searchInput.addEventListener("keydown", (e) => {
  if (e.key === "Enter") {
    searchBtn.click();
  }
});
