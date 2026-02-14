console.log("freeboardWrite.js loaded");



document.addEventListener("DOMContentLoaded", () => {
    
    /** 글자 수 카운트 **/
    const textarea = document.getElementById("contentInput");
    const charCount = document.getElementById("charCount");

    textarea.addEventListener("input", () => {
        const length = textarea.value.length;
        charCount.textContent = length;
        if (length > 4000) {
        textarea.value = textarea.value.substring(0, 4000);
        charCount.textContent = 4000;
        }
    });

    /** 이미지 미리보기 **/
    const photoInput = document.getElementById("photoInput");
    const photoPreview = document.getElementById("photoPreview");

    let selectedFiles = []; // 🔹 선택된 파일 누적 관리(JS 에서 파일 상태 직접 관리)

    photoInput.addEventListener("change", (e) => {
        const files = Array.from(e.target.files);
        //photoPreview.innerHTML = ""; // 기존 미리보기 초기화(누적 불가)

        // 최대 5장 제한
        if (selectedFiles.length + files.length > 5) {
        alert("사진은 최대 5장까지만 등록 가능합니다.");
        photoInput.value = "";
        return;
        }

        files.forEach((file) => {
        if (!file.type.startsWith("image/")) return;

        selectedFiles.push(file);

        const reader = new FileReader();
        reader.onload = (event) => {
            // 이미지 컨테이너
            const container = document.createElement("div");
            container.className = "preview-img-container";

            // 이미지
            const img = document.createElement("img");
            img.src = event.target.result;
            img.alt = "사진 미리보기";
            img.className = "preview-img";

            // 삭제 버튼
            const removeBtn = document.createElement("button");
            removeBtn.type = "button";
            removeBtn.className = "preview-remove";
            removeBtn.textContent = "×";

            removeBtn.addEventListener("click", () => {
                const index = Array.from(photoPreview.children).indexOf(container);
                selectedFiles.splice(index, 1);
                container.remove();

                /////
                refreshThumbnail();
            });

            container.appendChild(img);
            container.appendChild(removeBtn);
            photoPreview.appendChild(container);

            /////
            refreshThumbnail();
        };

        reader.readAsDataURL(file);
        });

    // 같은 파일 다시 선택 가능하게 초기화
    photoInput.value = "";
    });

    // 썸네일(대표) 이미지 갱신 (항상 첫 번째)
    function refreshThumbnail() {
        Array.from(photoPreview.children).forEach((container, index) => {
            container.classList.remove("thumbnail");

            const badge = container.querySelector(".thumbnail-badge");
            if (badge) badge.remove();

            if (index === 0) {
            container.classList.add("thumbnail");

            const badgeEl = document.createElement("div");
            badgeEl.className = "thumbnail-badge";
            badgeEl.textContent = "대표";

            container.appendChild(badgeEl);
            }
        });
    }


    const form = document.querySelector("form");
    //form.addEventListener("submit", (e) => {
    const submitBtn = document.getElementById('submitBtn'); //###LKSIURI
    submitBtn.addEventListener("click", (e) => {  //###LKSIURI
        e.preventDefault(); // 기본 submit 막기

        //  등록 확인 알림
        const ok = confirm("작성글을 등록하시겠습니까?");
        if (!ok) {
            return; // 취소 → submit 중단
        }
        /** 유효성 검사 **/
        const title = document.getElementById("titleInput").value.trim();
        const content = textarea.value.trim();

        if (title.length === 0) {
        alert("제목을 입력해주세요.");
        // e.preventDefault();
        return;
        }

        if (content.length < 10) {
        alert("내용은 최소 10자 이상 입력해주세요.");
        // e.preventDefault();
        return;
        }


        /**  FormData 생성 **/
        const formData = new FormData(form);

         formData.append("boardTitle", title); // ###LKSIURI
         formData.append("boardContent", content); // ###LKSIURI

        // 기존 images 제거 (중복 방지)
        formData.delete("images");

        // JS에서 관리하던 파일을 다시 넣는다
        selectedFiles.forEach((file) => {
            formData.append("images", file);
        });

        /** 서버 전송 **/
        //fetch(form.action, {
        fetch('/board2/freeboard/insert', { //###LKSIURI
            method: "POST",
            body: formData
        })
        .then(res => res.json()) // JSON을 JS 객체로
        .then(data => {
            alert(data.message); // 알림창 메세지

            if (data.success && data.redirectUrl) {
                //window.location.href = data.redirectUrl;
                location.href = data.redirectUrl; // JSON을 JS 객체로 // ###LKSIURI
            }
        })
        .catch(err => {
            console.error(err);
            alert("서버 통신 중 오류가 발생했습니다.")
        });
    });


});

// chatbot 팝업창 열기
function openHelper() {
    const select = document.getElementById("helperType");
    const selectedValue = select.value;

    let url = "";
    let pWinName = "";

    if (selectedValue === "ai") {
        url = "/api/ai/freeboard/page";
        pWinName = "ai";
    } else if (selectedValue === "chatbot") {
        url = "/api/chatbot/freeboard/popupBasicChatbot";
        pWinName ="chatbot";
    }

    if (!url) return;

    // // 부모(수정화면창) → 자식 팝업(챗봇 basic 팝업창)으로 전역 변수 전달하기 위함
    // window.globalData = {
    //     boardNo: window.boardNo,
    //     loginMemberNo: window.loginMemberNo
    //     // more variables
    // };  

    window.open(
        url,
        //"helper", // 창이름 (같은이름의 창존재-> 기존 창 재사용, 없으면 새 창 생성)
        pWinName,
        "width=520,height=760"
    );
}
