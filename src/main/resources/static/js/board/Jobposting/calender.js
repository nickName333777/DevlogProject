document.addEventListener("DOMContentLoaded", function () {
  const calendarEl = document.getElementById("calendar");
  const rawData = window.jobPostingData || [];

  const events = rawData.map((job) => {
    // 1. 날짜 데이터 정제 (하이픈 형식이든 점 형식이든 추출)
    const getCleanDate = (str) => {
      if (!str) return null;
      const match = str.match(/\d{4}[\.\-]\d{2}[\.\-]\d{2}/);
      return match ? match[0].replace(/\./g, "-") : null;
    };

    const startDate = getCleanDate(job.applyStart);
    const endDate = getCleanDate(job.applyEnd);

    // 2. 날짜 결정 우선순위:
    // 시작일이 있으면 시작일로, 없으면 마감일로, 둘 다 없으면 오늘로.
    let finalDate =
      startDate || endDate || new Date().toISOString().slice(0, 10);

    return {
      title: job.postingTitle,
      start: finalDate,
      extendedProps: {
        jobId: job.postingNo,
      },
      // 클래스 구분: 마감일 텍스트에 '채용시'가 들어있으면 핑크
      className:
        job.applyEnd && job.applyEnd.includes("채용시")
          ? "event-pink"
          : "event-purple",
    };
  });

  const calendar = new FullCalendar.Calendar(calendarEl, {
    initialView: "dayGridMonth",
    locale: "ko",
    // initialDate: '2025-12-01',
    headerToolbar: {
      left: "title",
      center: "",
      right: "prev,next today",
    },
    dayMaxEvents: 3,
    contentHeight: 750,
    events: events,

    eventClick: function (info) {
      const jobId = info.event.extendedProps.jobId;

      if (!jobId) {
        alert("jobId 없음");
        return;
      }

      window.location.href = `/jobposting/${jobId}`;
    },
  });

  calendar.render();

  function showEventPopup(dateStr, events) {
    const old = document.querySelector(".custom-popup");
    if (old) old.remove();
    const popup = document.createElement("div");
    popup.className = "custom-popup";
    popup.innerHTML = `
            <div class="popup-content">
                <div class="popup-header">
                    <strong>${dateStr} 공고</strong>
                    <span class="close-btn" style="cursor:pointer;">&times;</span>
                </div>
                <div class="popup-body">
                    ${events
                      .map(
                        (ev) => `
                        <div class="popup-item ${
                          ev.classNames[0] || "event-purple"
                        }" style="margin-bottom:5px; padding:5px; border-radius:4px; color:white;">
                            ${ev.title}
                        </div>
                    `
                      )
                      .join("")}
                </div>
            </div>`;
    document.body.appendChild(popup);
    popup.querySelector(".close-btn").onclick = () => popup.remove();
  }
});
