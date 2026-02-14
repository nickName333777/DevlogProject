console.log("ITnews.js loaded");

const filterButtons = document.querySelectorAll(".filter-btn");

filterButtons.forEach(btn => {
    btn.addEventListener("click", () => {
        // 버튼에 설정된 data-type (21, 22 등) 가져오기
        const boardCode = btn.dataset.type;
        
        // 이동할 때 항상 1페이지(cp=1)로 가도록 설정
        if (boardCode === "0") {
            location.href = "/ITnews?cp=1";
        } else {
            location.href = "/ITnews?cp=1&boardCode=" + boardCode;
        }
    });
});

