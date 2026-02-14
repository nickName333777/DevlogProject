console.log("freeboardUpdate.js v2 (fixed) loaded");

const deleteSet = new Set();

let photoPreview;
let selectedFiles = []; // 전역 변수로 파일 상태 관리

document.addEventListener("DOMContentLoaded", () => {

    photoPreview = document.getElementById("photoPreview");

    if (!photoPreview) {
        console.log("photoPreview 없음 -> 이미지 없는 수정 페이지");
        return;
    }

    /** DOM에 이미 그려진 이미지 -> selectedFiles로 옮기기 **/
    initExistingImages();    
    /** 기존 이미지 이벤트 바인딩 함수 추가 **/
    bindExistingRemoveButtons();  

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

    photoInput.addEventListener("change", (e) => {
        const files = Array.from(e.target.files);

        // 최대 5장 제한
        if (selectedFiles.length + files.length > 5) {
            alert("사진은 최대 5장까지만 등록 가능합니다.");
            return;
        }

        // placeholder 컨테이너를 미리 순서대로 DOM에 추가
        files.forEach((file) => {
            if (!file.type.startsWith("image/")) return;

            // selectedFiles에 새 이미지 추가
            selectedFiles.push({
                type: "NEW",
                file: file
            });

            // placeholder 컨테이너 미리 생성 및 DOM에 추가
            const container = document.createElement("div");
            container.className = "preview-img-container";
            photoPreview.appendChild(container);

            // 비동기로 이미지 로드
            const reader = new FileReader();
            reader.onload = (event) => {
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
                    handleRemove(container);
                });

                // 이미 DOM에 추가된 container에 내용만 채움
                container.appendChild(img);
                container.appendChild(removeBtn);

                // 썸네일 갱신
                refreshThumbnail();
            };

            reader.readAsDataURL(file);
        });

        // 같은 파일 다시 선택 가능하게 초기화
        photoInput.value = "";
    });


    // submit 시 FormData 구성
    const form = document.querySelector("form");
    //form.addEventListener("submit", (e) => {
    const submitBtn = document.getElementById('submitBtn'); // ###LKSIURI
    submitBtn.addEventListener("click", (e) => {  // ###LKSIURI	
        e.preventDefault();

        const ok = confirm("작성글을 수정하시겠습니까?");
        if (!ok) {
            return;
        }

        /** 유효성 검사 **/
        const title = document.getElementById("titleInput").value.trim();
        const content = textarea.value.trim();

        if (title.length === 0) {
            alert("제목을 입력해주세요.");
            return;
        }

        if (content.length < 10) {
            alert("내용은 최소 10자 이상 입력해주세요.");
            return;
        }

        /**  FormData 생성 **/
        const formData = new FormData(form);

        formData.append("boardTitle", title); // ###LKSIURI
        formData.append("boardContent", content); // ###LKSIURI
        
        // 기존 images 제거 (중복 방지)
        formData.delete("images");

        // 새 이미지만 images로 전송
        selectedFiles
            .filter(item => item.type === "NEW")
            .forEach(item => {
                formData.append("images", item.file);
            });

        // 유지할 기존 이미지 PK + 순서
        const existingImgNos = selectedFiles
            .filter(item => item.type === "EXISTING")
            .map(item => item.imgNo);

        formData.append(
            "existingImgNos",
            JSON.stringify(existingImgNos)
        );

        console.log("=== V2 전송 데이터 확인 ===");
        console.log("selectedFiles:", selectedFiles);
        console.log("새 이미지 개수:", formData.getAll("images").length);
        console.log("기존 이미지 번호:", existingImgNos);

        /** 서버 전송 **/
        ////fetch(form.action, { //  <form th:action="@{update}" method="post" enctype="multipart/form-data"></form>
		////fetch('/board2/freeboard/update', { // ###LKSIURI - 1th (initial: redirection issue)
		//fetch('/board2/freeboard/insert', { // ###LKSIURI-monkeyPatch - 2nd  (monkeyPatch: insert + del까지만)
        // ##### 원래 게시글 수정으로 복귀 - 3rd (이제 유효한 update로 복귀)
        const origBoardNo = parseInt(window.boardNo);
        const addrUpdate3rd = '/board2/freeboard/'+ origBoardNo + '/update' ; 
        console.log("addrUpdate3rd = ", addrUpdate3rd);
        fetch(addrUpdate3rd, { // ##### 원래 게시글 수정으로 복귀 - 3rd (이제 유효한 update로 복귀)
            method: "POST",
            body: formData
        })
        .then(res => res.json())
        .then(data => {
            // if (data.success) { // ###LKSIURI-monkeyPatch - 2nd  (monkeyPatch: insert + del까지만)
            //     // alert(data.message);
            //     // // window.location.href = data.redirectUrl;
            //     // location.href = data.redirectUrl; // JSON을 JS 객체로 // ###LKSIURI
            //     console.log("수정대신에 insert 성공"); // ###LKSIURI-monkeyPatch
            //     console.log("data: ", data)
            //     //location.href = data.redirectUrl; //  ###LKSIURI-monkeyPatch
            //     console.log("insert 반환 결과 data.redirectUrl = ", data.redirectUrl); // 예1) '/board/freeboard/62', inserted된 글로
            //     const insertedBoardNo = parseInt(data.redirectUrl.split("/")[3]);
            //
            //     /////// monkey-patch by YYP, 2026-01-08
            //     // update -> insert + old delete으로 moneky-patch
            //     // 수정대신 insert성공하면, 이리로 넘어옴: 여기서 [A] 해당 BoardNo글 삭제 시행 후, [B] location.href로 redirection.
            //     // - 기존 보드넘버는 window.boardNo로 부터: oldBoardNo = window.boardNo
            //     // - 새 보드넘버는  redirectUrl = "/board/freeboard/" + boardNo는  에서 얻어와라:
            //     /////// [A] 기존 old boardNo게시글 삭제를 GET아니라 ajaxt POST로 수행해라
            //     //location.href = location.pathname.replace('board/', 'board2/') + "/delete"; // -> 게시글 삭제 처리하는 controller 만들어야 한다.
            //     // // http://localhost:8880/board2/freeboard/5/delete
            //     // ajax 주소: location.pathname = "/board/freeboard/27"  ==> location.pathname.split('/') ==> [  "", "board", "freeboard", "27" ]
            //     // -- 0) DELETE-POST 새주소(delPOSTaddr) 생성:
            //     // 예시: /board/freeboard/27 ==> "/board/freeboard/15" , 여기서 oldBoardNo = 15
            //     const oldBoardNo = parseInt(window.boardNo);
            //     const pathArr = location.pathname.split('/'); // 
            //     //pathArr[1] = "board2"; // 불필요. 원래 location.pathname = "/board2/freeboard/{oldBoardNo}/update"
            //     pathArr[3] = oldBoardNo; // 15
            //     const delPOSTaddr = pathArr.join('/') + "/deletePOST"; //  "/board2/freeboard/15" + "/deletePOST"
            //                                                             //  /board2/freeboard/20/update/deletePOST                                                   
            //     console.log("delPOSTaddr", delPOSTaddr); // 예1) /board2/freeboard/61/update/deletePOST
            //     //  -- 1) body에 전달할 데이터 구성:  
            //     // const formDataDEL = new FormData(form);
            //     // formDataDEL.append( // 기존 이미지  PK + 순서를 추후 콘트롤러에서의 업데이트 위해(나중에...) 담아준다
            //     //     "existingImgNos",
            //     //     JSON.stringify(existingImgNos)
            //     // );
            //     // formDataDEL.append( // old boardNo (경로에서 받아도 되나?)
            //     //     "oldBoardNo",
            //     //     JSON.stringify(oldBoardNo)
            //     // );                
            //     //
            //     // body에 넣을 JS 객체로 구성
            //     const requestBodyData = {
            //         oldBoardNo: oldBoardNo,
            //         insertedBoardNo: insertedBoardNo,
            //         existingImgNos: existingImgNos
            //     };
            //
            //     fetch(delPOSTaddr, { // ###LKSIURI-monkeyPatch ==> 콘트롤러 작성해라.
            //                          // 
            //         method: "POST",
            //         headers: { 'Content-Type': 'application/json' },
            //         // body: formDataDEL
            //         body: JSON.stringify(requestBodyData)
            //     })
            //     .then(res => res.json())
            //     .then(dataDel => {
            //         console.log("dataDel", dataDel); 
            //         if (dataDel.success) { // data 구성은 "update" POST 콘트롤러 참여해라
            //             console.log("dataDel.redirectUrl", dataDel.redirectUrl); // // 예1) '/board/freeboard/62'
            //
            //             /////// [B] 이제 location.href로 redirection.
            //             console.log("수정대신에 insert 성공!!"); // ###LKSIURI-monkeyPatch
            //             //alert(data.message); // 기존 "수정 성공"메시지 알림창에 출력
            //             console.log("data.redirectUrl", data.redirectUrl); // // 예1) '/board/freeboard/62', inserted된 글로
            //             location.href = data.redirectUrl;   //  ###LKSIURI-monkeyPatch; 
            //                                                 //   data.redirectUrl == dataDel.redirectUrl
            //         } else {
            //             console.log("기존 게시글 삭제처리 실패!!"); 
            //         }
            //     })
            //     .catch(err => {
            //         console.error(err);
            //         alert("오류가 발생했습니다."); // DELETE - POST실패
            //     });
            //
            // } else {
            //     alert(data.message);
            // }

            if (data.success) { // ##### 원래 게시글 수정으로 복귀 - 3rd (이제 유효한 update로 복귀)
                alert(data.message);
                //window.location.href = data.redirectUrl;
                location.href = data.redirectUrl; // JSON을 JS 객체로 // ###LKSIURI
            } else {
                alert(data.message);
            }


        })
        .catch(err => {
            console.error(err);
            alert("오류가 발생했습니다.");
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
    
    // 부모(수정화면창) → 자식 팝업(챗봇 basic 팝업창)으로 전역 변수 전달하기 위함
    window.globalData = {
        boardNoGlobal: window.boardNo,
        loginMemberNoGlobal: window.loginMemberNo
        // more variables
    };

    window.open(
        url,
        pWinName,
        "width=520,height=760"
    );
}

// 썸네일(대표) 이미지 갱신 (항상 첫 번째)
function refreshThumbnail() {
    if (!photoPreview) return;

    const containers = photoPreview.querySelectorAll(".preview-img-container");

    containers.forEach((container, index) => {
        container.classList.remove("thumbnail");

        const badge = container.querySelector(".thumbnail-badge");
        if (badge) badge.remove();

        // DOM에서 첫 번째가 대표
        if (index === 0) {
            container.classList.add("thumbnail");

            const badgeEl = document.createElement("div");
            badgeEl.className = "thumbnail-badge";
            badgeEl.textContent = "대표";
            container.appendChild(badgeEl);
        }
    });
}

// DOM에 이미 그려진 이미지 → selectedFiles로 옮기기
function initExistingImages() {
    if (!photoPreview) return;

    const containers = document.querySelectorAll(
        "#photoPreview .preview-img-container"
    );

    containers.forEach(container => {
        const imgNo = container.dataset.imgNo;

        selectedFiles.push({
            type: "EXISTING",
            imgNo: imgNo
        });
    });

    refreshThumbnail();
}

// 기존 이미지 vs 새 이미지 구분해서 삭제
function handleRemove(container) {
    const index = Array.from(photoPreview.children).indexOf(container);
    const fileInfo = selectedFiles[index];

    console.log("=== 삭제 시작 ===");
    console.log("삭제 인덱스:", index);
    console.log("삭제 대상:", fileInfo);
    console.log("삭제 전 selectedFiles:", [...selectedFiles]);

    // 마지막 이미지 삭제 방지
    if (selectedFiles.length === 1) {
        alert("최소 1장의 이미지는 반드시 필요합니다.");
        return;
    }

    // 대표 이미지 삭제 경고
    if (index === 0) {
        const ok = confirm(
            "대표 이미지입니다.\n삭제 시 다음 이미지가 대표로 지정됩니다.\n삭제하시겠습니까?"
        );
        if (!ok) return;
    }    

    // 기존 이미지인 경우
    if (fileInfo.type === "EXISTING") {
        const imgNo = fileInfo.imgNo;

        if (!confirm("이미지를 삭제하시겠습니까?")) return;

        fetch(`/board2/freeboard/deleteImage/${imgNo}`, {
            method: "DELETE"    
        })
        .then(res => {
            if (!res.ok) throw new Error("삭제 실패");
            return res.json();
        })
        .then(data => {
            // 서버 삭제 성공 시
            selectedFiles.splice(index, 1);
            container.remove();
            console.log("=== 삭제 완료 ===");
            console.log("삭제 후 selectedFiles:", [...selectedFiles]);
            console.log("삭제 후 DOM 개수:", photoPreview.children.length);
            refreshThumbnail();
        })
        .catch(err => {
            alert("이미지 삭제 중 오류가 발생했습니다.");
            console.error(err);
        });

    } else {
        // 새 이미지인 경우 (서버 요청 X)
        selectedFiles.splice(index, 1);
        container.remove();
        console.log("=== 삭제 완료 (새 이미지) ===");
        console.log("삭제 후 selectedFiles:", [...selectedFiles]);
        console.log("삭제 후 DOM 개수:", photoPreview.children.length);
        refreshThumbnail();
    }
}

// 기존 이미지 이벤트 바인딩 함수
function bindExistingRemoveButtons() {
    const containers = document.querySelectorAll(
        "#photoPreview .preview-img-container"
    );

    containers.forEach(container => {
        const removeBtn = container.querySelector(".preview-remove");

        if (!removeBtn) return;

        removeBtn.addEventListener("click", () => {
            handleRemove(container);
        });
    });
}