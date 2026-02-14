console.log("freeboardDetail.js loaded");
// 글쓰기 버튼 클릭 시 

console.log("loginMemberNo =", loginMemberNo);
// 또는
console.log("loginMemberNo =", window.loginMemberNo);

const likeIcon = document.querySelector(".like-icon");
const img = likeIcon.querySelector("img");

const HEART_EMPTY = "../../../images/board/freeboard/iconfy_red-heart_empty.png";
const HEART_FILLED = "../../../images/board/freeboard/iconfy_twemoji_red-heart_filled.png";

const pTagLikeCount = document.getElementById("pTagLikeCount"); // 좋아요 수 표시 tag업데이트

likeIcon.addEventListener("click", async () => {
    const boardNo = likeIcon.dataset.boardId;
    const liked = likeIcon.dataset.liked === "true"; // DB에서 현 loginMember가 이 boardNo에 좋아요 한적 있는지 체크해서 업데이트 해줘야 함

    // 로그인 X
    //if(loginMemberNo == null) { // 
    if(loginMemberNo == "") { // loginMemberNo를 여기서 쓰려고 boardDetail.jsp에서 전역변수로 선언해놓았다 
        alert("로그인 후 이용해 주세요");
        return;   // 아래는 확인 필요 없음 
    }

    try {

        // ajax로 서버에 제출할 파라미터를 모아둔 JS 객체
        const data = {  memberNo : loginMemberNo,
                        'boardNo': Number(boardNo),
                        "check"  : Number(liked) //  DB에서 현 loginMember가 이 boardNo에 좋아요 한적 있는지 체크해서 업데이트 해줘야 함
                    };                    
        
        console.log(data);

        if (!liked) {
        // 좋아요 추가
        await fetch("/board/freeboard/like", {
            method: "POST",
            headers: {
            "Content-Type": "application/json"
            },
            
            body: JSON.stringify(data)
        })
        .then(resp => resp.text())
        .then(count => {
            // 파싱된 데이터를 받아서 처리하는 코드 작성
            console.log("count : " + count); // -1이면 SQL실패

            // INSERT, DELETE실패 시 (좋아요 조회 실패시 FreeboardServiceImpl에서 -1 반환)
            if (count == -1) { // 
                alert("좋아요 추가 처리 실패.");
                return;
            }

            pTagLikeCount.innerText = count;

        });


        img.src = HEART_FILLED;
        likeIcon.dataset.liked = "true";
        likeIcon.classList.add("active");

        } else {
        // 좋아요 삭제
            await fetch("/board/freeboard/like", {
                // method: "DELETE",
                method: "POST",
                headers: {
                "Content-Type": "application/json"
                },
                //body: JSON.stringify({ boardNo })
                body: JSON.stringify(data)
            })
            .then(resp => resp.text())
            .then(count => {
                // 파싱된 데이터를 받아서 처리하는 코드 작성
                console.log("count : " + count); // -1이면 SQL실패

                // INSERT, DELETE실패 시 (좋아요 조회 실패시 FreeboardServiceImpl에서 -1 반환)
                if (count == -1) { // 
                    alert("좋아요 삭제 처리 실패.");
                    return;
                }

                pTagLikeCount.innerText = count;

            });

            img.src = HEART_EMPTY;
            likeIcon.dataset.liked = "false";
            likeIcon.classList.remove("active");
        }

    } catch (e) {
        alert("좋아요 처리 중 오류가 발생했습니다.");
        console.error(e);
    }
});


// ----------------------------------
// 게시글 수정
//
// 게시글 버튼 수정 클릭시
if (document.getElementById("updateBtn") != null){ // 로그인 안했으면 수정버튼 안보임 => null처리 필요
    document.getElementById("updateBtn").addEventListener("click", ()=>{
    
        location.href = location.pathname.replace('board/', 'board2/') + '/update' + location.search; // location.search = '?cp=1'
        // eg: '/board2/freeboard/5/update?cp=1'
    
    })
}


// ---------------------------------
// 게시글 삭제 버튼이 클릭 되었을 때
//
 // 로그인 안했으면 삭제버튼 안보임 => null처리 필요 => 옵셔날 체이닝으로 처리
document.getElementById("deleteBtn")?.addEventListener("click", ()=>{

    console.log(location.pathname.replace('board/', 'board2/') + "/delete");
    if(confirm("정말 삭제 하시겠습니까?")) {

        location.href = location.pathname.replace('board/', 'board2/') + "/delete"; // -> 게시글 삭제 처리하는 controller 만들어야 한다.
        // http://localhost/board2/freeboard/5/delete

    }
})


// -----------------------------------------------------------
// 목록으로
const goToListBtn = document.getElementById("goToListBtn");

goToListBtn.addEventListener("click", ()=>{
    // location.href = location.pathname.split("/").slice(0, -1).join('/') + location.search; 
    // URL 내장 객체 : 주소 관련 정보를 나타내는 객체
    // URL.searchParams : 쿼리스트링만 별도 객체로 반환
    console.log("goToListBtn clicked... ")
    
    const params = new URL(location.href).searchParams;
    console.log("params : " + params);

    let url;
    if (params.get("key") == 'all') { // header의 통합 검색 일때 (Not used here)
        url = "/board/search";
    } else {
        url = '/board/freeboard'; // 목록으로; boardCode=3는 전역변수
    }

    location.href = url + location.search;

})

function openReportModal(targetMemberNo, targetNo) {  //<--------------------------- 함수 호출 시 타겟 회원 번호 넣어서 호출
    console.log("openReportModal함수 실행...")
    
    console.log("before Number() conversion: ")
    console.log(targetMemberNo, typeof targetMemberNo)
    console.log(targetNo, typeof targetNo)  
    
    targetMemberNo = Number(targetMemberNo);
    targetNo = Number(targetNo);
    console.log("after Number() conversion: ")
    console.log(targetMemberNo, typeof targetMemberNo)
    console.log(targetNo, typeof targetNo)

    fetch(`/report/modal?memberNo=${targetMemberNo}`) //<------------------------------------------- 타켓 대상 회원 번호 넣어주셔야 합니다.
        .then((res) => res.text())
        .then((html) => {
            console.log("받아온 html: ")
            console.log(html); // 뭘 받아오나 보자

            const root = document.getElementById("modal-root"); // freeboardDetail.html에 <div id="modal-root"></div> 태그 필요
            root.innerHTML = html;
            const modal = root.querySelector("#reportModal");
            modal.classList.remove("display-none");

            modal.dataset.targetType = "BOARD"; //<-------------------------- 이 부분은 게시판이신 분들은 BOARD로 바꿔주세요
            modal.dataset.targetNo = targetNo; //<-------------------------- 게시글 번호도 이런 식으로 넘겨주세요
            bindReportModalEvents();
        });
}


function scrollToHashIfExists() {
  const hash = location.hash; // 예: "#comment-6"
  if (!hash) return;

  const target = document.querySelector(hash);
  if (target) {
    target.scrollIntoView({ behavior: "smooth", block: "center" });
  }
}

const reportBtn = document.getElementById("reportBtn");
// 좋아요 버튼이 클릭 되었을 때
reportBtn.addEventListener("click", (e) => {
  // 로그인 X
  if (!loginMemberNo || loginMemberNo === "") {
    alert("로그인 후 이용해주세요.");
    location.href  = "/board/freeboard";
    return;
  }



})