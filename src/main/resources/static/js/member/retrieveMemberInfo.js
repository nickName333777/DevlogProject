console.log("retrieveMemberInfo.js loaded ....");


//-------------------------------------------------------
// fetch() API를 이용한 POST 방식 요청

const resultArea = document.getElementById("resultArea");

// 이메일을 입력 받아 일치하는 회원 정보 조회
const inputEmail = document.getElementById("inputMemberEmail");
const btn1 = document.getElementById("btn1");

btn1.addEventListener("click", ()=>{
    console.log(inputEmail.value, typeof inputEmail.value);
    fetch("/api/manager/selectMemberbyMemberEmail", {
        method : "POST",
        headers : {"Content-Type" : "application/json"},    // http 요청의 headers와 body
        body : JSON.stringify({"email" : inputEmail.value})
        
    })
    .then(resp => resp.json()) 
    .then(member => { 
        // 이메일로 회원 정보 조회 성공 헸을때
        console.log(member);

        printMemberInfo(member);

    })
    .catch(err => {
        resultArea.innerText = "일치하는 회원이 없습니다.";
        console.log(err)}
    )
    
})


function printMemberInfo(member){
        // ul내부 내용 모두 없애기
        resultArea.innerHTML = "";

        // 조회 내용 업데이트
        const li1 = document.createElement("li");
        li1.innerText = `회원번호 : ${member.memberNo}`;
        
        const li2 = document.createElement("li");
        li2.innerText = `이메일 : ${member.memberEmail}`;

        const li3 = document.createElement("li");
        li3.innerText = `이름 : ${member.memberName}`;

        const li4 = document.createElement("li");
        li4.innerText = `닉네임 : ${member.memberNickname}`;

        const li5 = document.createElement("li");
        li5.innerText = `전화번호 : ${member.memberTel}`;

        const li6 = document.createElement("li");
        li6.innerText = `role : ${member.role}`;

        const li7 = document.createElement("li");
        li7.innerText = `관리자여부 : ${member.memberAdmin}`;

        const li8 = document.createElement("li");
        li8.innerText = `이메일수신여부 : ${member.memberSubscribe}`;

        const li9 = document.createElement("li");
        li9.innerText = `탈퇴여부 : ${member.memberDelFl}`;

        const li10 = document.createElement("li");
        li10.innerText = `경력 : ${member.memberCareer}`;

        const li11 = document.createElement("li");
        li11.innerText = `프로필이미지 : ${member.profileImg}`;
        //
        const li12 = document.createElement("li");
        li12.innerText = `내소개 : ${member.myInfoIntro}`;

        const li13 = document.createElement("li");
        li13.innerText = `깃허브 : ${member.myInfoGit}`;

        const li14 = document.createElement("li");
        li14.innerText = `홈페이지 : ${member.myInfoHomepage}`;

        const li15 = document.createElement("li");
        li15.innerText = `구독료 : ${member.subscriptionPrice}`;

        const li16 = document.createElement("li");
        li16.innerText = `커피콩잔액 : ${member.beansAmount}`;

        const li17 = document.createElement("li");
        li17.innerText = `경험치 : ${member.currentExp}`;

        const li18 = document.createElement("li");
        li18.innerText = `가입일 : ${member.mCreateDate}`;

        const li19 = document.createElement("li");
        li19.innerText = `타이틀 : ${member.memberLevel.title}`;        

        resultArea.append(li1, li2, li3, li4, li5, li6,
            li7, li8, li9, li10, li11, li12,
            li13, li14, li15, li16, li17, li18 , li19
        ); // 화면 배치로 화면에 나타나게 된다

}

// 닉네임을 입력 받아 일치하는 회원 정보 조회
const inputNickname = document.getElementById("inputMemberNickname");
const btn2 = document.getElementById("btn2");

btn2.addEventListener("click", ()=>{
    console.log("닉네임 입력값 확인");
    console.log(inputNickname.value, typeof inputNickname.value);
    fetch("/api/manager/selectMemberByMemberNickname", {
        method : "POST",
        headers : {"Content-Type" : "application/json"},   
        body : JSON.stringify({"nickname" : inputNickname.value}) // 
                                                            
    })
    .then(resp => resp.json()) 
    .then(member => { 
        console.log(member);

        // ul내부 내용 모두 없애기
        resultArea.innerHTML = "";

        printMemberInfo(member);

    })
    .catch(err => {
        resultArea.innerText = "일치하는 회원이 없습니다.";
        console.log(err)}
    )
    
})



// 회원번호를 입력 받아 일치하는 회원 정보 조회
const inputMemberNo = document.getElementById("inputMemberNo");
const btn3 = document.getElementById("btn3");

btn3.addEventListener("click", ()=>{
    console.log("회원 번호 입력값 확인");
    console.log(inputMemberNo.value, typeof inputMemberNo.value);
    fetch("/api/manager/selectMemberByMemberNo", {
        method : "POST",
        headers : {"Content-Type" : "application/json"},    
        body : JSON.stringify({"memberNo" : Number(inputMemberNo.value) }) 
    })
    .then(resp => resp.json()) 
    .then(member => { 
        console.log(member);

        // ul내부 내용 모두 없애기
        resultArea.innerHTML = "";

        printMemberInfo(member);

    })
    .catch(err => {
        resultArea.innerText = "일치하는 회원이 없습니다.";
        console.log(err)}
    )
    
})
