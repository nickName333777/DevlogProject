console.log("freeboardList.js loaded");
// 글쓰기 버튼 클릭 시 


document.getElementById("fbWriteBtn")?.addEventListener("click", ()=>{
    // JS BOM 객체 중 location
    console.log(location.pathname.split("/")); // location.pathname = "/board/freeboard"

    // location.href = '주소' : 해당 주소로 요청(GET 방식)
	location.href = `/board2/${location.pathname.split("/")[2]}/insert`; // => http://localhost:8880/board2/freeboard/insert
	
});