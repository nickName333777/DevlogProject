console.log('signUp.js loaded ...')


// 유효성 검사 진행 여부 확인 객체
const checkObj = {
    'memberEmail' : false,
    'memberPw' : false,
    'memberPwConfirm' : false,
    'memberName' : false,    
    'memberNickname' : false,
    'memberTel' : false,
    'memberCareer' : false,
    'memberSubscribe': true, // 필수아님
    'memberAdmin': true, // 필수아니나, 체크시 유효성검사필요 (관리자 계정 승인 코드 체크필수)
    'authKey' : false  // 이메일 인증에서 사용
}

// 이메일 유효성 검사
const memberEmail = document.getElementById("memberEmail");
const emailMessage = document.getElementById("emailMessage")

memberEmail.addEventListener("input", function(){

    // 입력된 이메일이 없을 경우 : "메일을 받을 수 있는 이메일을 입력해주세요." 까만글씨
    // 1) 이메일 미작성시
    if(memberEmail.value == ""){
        emailMessage.innerText = "메일을 받을 수 있는 이메일을 입력해주세요.";
        //emailMessage.style.color = "black";
        emailMessage.classList.remove("confirm", "error");

        checkObj.memberEmail = false;

        return;
    }
    
    // 입력된 이메일이 있는 경우 정규식 일치 여부 판별
    // 영어, 숫자, -, _ 4글자 이상 @ 영어.영어.영어 || 영어.영어
    const regExp = /^[\w\_\.]{4,}@[a-z]+(\.[a-z]+){1,2}$/; //"id는 4글자 이상" // 



    // 

    // 유효한 경우 : 유효한 이메일 형식 입니다. 초록글씨
    if (regExp.test(memberEmail.value)) { // input 창에서 입력받은 이메일
        // 유효한 경우
        console.log(memberEmail.value);
        
        // ***********************************
        // fetch() API를 이용한 ajax

        // Get 방식 ajax 요청(쿼리스트링으로 파라미터 전달 : ?키=값&키=값)
        fetch("/dupCheck/email?email=" + memberEmail.value) // 띄어쓰기 없어야함
        .then(resp => resp.text()) // 조회된 회원수 count이므로 단순 text
        .then(count => {
            console.log("public int dupCheckEmail(String email) 리턴값:") // public int dupCheckEmail(String email) 
            console.log(count);
            // count : 중복이면 1, 아니면 0
            if (count==1){
                // 유효한 경우, 중북이메일 인 경우
                emailMessage.innerText = "이미 사용중인 이메일 입니다.";
                emailMessage.classList.remove("confirm");
                emailMessage.classList.add("error");

                checkObj.memberEmail = false;
            } else {
                // 유효한 경우, 중북이메일이 아닌 경우
                emailMessage.innerText = "사용 가능한 이메일입니다.";
                emailMessage.classList.remove("error");
                emailMessage.classList.add("confirm");
        
                checkObj.memberEmail = true;
                
            }
        })
        .catch(err => console.log(err))  // 예외처리

        // 유효한 경우
        emailMessage.innerText = "유효한 이메일 형식 입니다.";
        emailMessage.classList.add("confirm");
        emailMessage.classList.remove("error");

        checkObj.memberEmail = true;

    } else {
        // 유효하지 않은 경우 : 이메일 형식이 유효하지 않습니다. 빨간 글씨
        emailMessage.innerText = "이메일 형식이 유효하지 않습니다.";
        emailMessage.classList.add("error");
        emailMessage.classList.remove("confirm");

        checkObj.memberEmail = false;
                        
    }

})


//---------------------------------------------------------------------------
// 이메일 인증
//
// 인증번호 발송
const sendAuthKeyBtn = document.getElementById("sendAuthKeyBtn");
const authKeyMessage = document.getElementById("authKeyMessage");
let authTimer;
let authMin = 4;
let authSec = 59;


// 인증번호를 발송한 이메일 저장
let tempEmail = "";


if (authTimer == undefined){
    console.log("tempEmail = " + tempEmail);
    console.log("authTimer = " + authTimer);
    sendAuthKeyBtn.disabled = false;

    sendAuthKeyBtn.addEventListener("click", function(){
        authMin = 4;
        authSec = 59;
    
    
        checkObj.authKey = false;
    
    
        if(checkObj.memberEmail){ // 유효하고, 중복이 아닌 이메일인 경우
    
            /* fetch() API 방식 ajax */
            fetch("/sendEmail/signUp?email="+memberEmail.value) // GET방식, key값은 email
            .then(resp => resp.text()) // text로 parsing
            .then(result => {  // result변수로 text값 받아온다.
                if(result > 0){
                    console.log("인증 번호가 발송되었습니다.")
                    tempEmail = memberEmail.value;
                }else{
                    console.log("인증번호 발송 실패")
                }
            })
            .catch(err => {
                console.log("이메일 발송 중 에러 발생");
                console.log(err);
            });
    
            alert("인증번호가 발송 되었습니다."); // 비동기 통신처리중 띄워주는 메시지
    
            authKeyMessage.innerText = "05:00"; // timer시작 (5분)
            authKeyMessage.classList.remove("confirm");
            clearInterval(authTimer); // 기존 것 지우고 다시시작 해라!
    
            //if (authTimer == 2){
                authTimer = window.setInterval(()=>{
                console.log("authTimer gen = " + authTimer); 
                                                //                                    04  또는       14
                    authKeyMessage.innerText = "0" + authMin + ":" + (authSec<10 ? "0" + authSec : authSec);
                    
                    // 남은 시간이 0분 0초인 경우
                    if(authMin == 0 && authSec == 0){
                        checkObj.authKey = false; // 인증 번호 유효성 검사만 무효화 

                        clearInterval(authTimer);
                        return;
                    }
        
                    // 0초인 경우
                    if(authSec == 0){
                        authSec = 60;
                        authMin--;
                    }
        
                    authSec--; // 1초 감소
        
                }, 1000)
            //}
    
    
        } else{
            alert("중복되지 않은 이메일을 작성해주세요.");
            memberEmail.focus();
        }
    
    
    });

} else { //authTimer != undefined 인 경우
    console.log("tempEmail = " + tempEmail);
    console.log("authTimer = " + authTimer);
    sendAuthKeyBtn.disabled = true;
}




// 인증 확인
const authKey = document.getElementById("authKey");
const checkAuthKeyBtn = document.getElementById("checkAuthKeyBtn");


checkAuthKeyBtn.addEventListener("click", function(){

    if(authMin > 0 || authSec > 0){ // 시간 제한이 지나지 않은 경우에만 인증번호 검사 진행
        /* fetch API */
        const obj = {"inputKey":authKey.value, "email":tempEmail}
        const query = new URLSearchParams(obj).toString()
        // ?inputKey=123456&email=user01@og.or.kr 로 바뀐다 by URLSearchParams(obj).toString()
        
        fetch("/sendEmail/checkAuthKey?" + query) // 비동기 통신 보낸다
        .then(resp => resp.text())
        .then(result => {
            if(result > 0){
                clearInterval(authTimer);
                authKeyMessage.innerText = "인증되었습니다.";
                authKeyMessage.classList.add("confirm");
                checkObj.authKey = true;


            } else{
                alert("인증번호가 일치하지 않습니다.")
                checkObj.authKey = false;
            }
        })
        .catch(err => console.log(err));

    } else{
        alert("인증 시간이 만료되었습니다. 다시 시도해주세요.")
    }


});





// --------------------------------------------------------------------------

// 비밀번호/비밀번호 확인 유효성 검사
// checkPw() 함수로 만들어 해봐라(숙제)

const memberPw = document.getElementById("memberPw");
const memberPwConfirm = document.getElementById("memberPwConfirm");
const pwMessage = document.getElementById("pwMessage");

memberPw.addEventListener("input", function(){
    // 비밀번호를 입력할 때 마다 검사 진행
    
    // 비밀번호 미 작성 시
    // 영어, 숫자, 특수문자(!, @, #, -, _) 6~20글자 사이로 입력해 주세요. 까만 글씨
    if (memberPw.value.trim().length == 0) {
        memberPw.innerText = "영어, 숫자, 특수문자(!, @, #, -, _) 6~20글자 사이로 입력해 주세요.";
        memberPw.classList.remove("confirm", "error");
        checkObj.memberPw = false;
        return;
    }
    
    // 비밀번호 입력시
    
    // 비밀번호 유효성 검사: 영어 대/소문자, 숫자, !, @, #, -, _ 포함한 6~20 글자 사이 (\w에 _ 포함되어있다)
    const regExp = /^[\w!@#\-_]{6,20}$/;

    if (regExp.test(memberPw.value)) {
        // 유효한 경우 : 사용 가능한 비밀번호 입니다. 초록 글씨
        checkObj.memberPw = true;

        // 비밀번호가 유효한 상태에서 비밀번호 확인이 입력되지 않은 경우
        if ( memberPwConfirm.value == "") {
            pwMessage.innerText = "사용가능한 비밀번호 입니다.";
            pwMessage.classList.add("confirm");
            pwMessage.classList.remove("error");

        } else { // 비밀번호 확인이 입력되어 있는 경우

            // 비밀번호와 비밀번호 확인이 같을 경우
            if (memberPw.value == memberPwConfirm.value) {
                pwMessage.innerText = "비밀번호가 일치합니다.";
                pwMessage.classList.add("confirm");
                pwMessage.classList.remove('error');
                checkObj.memberPwConfirm = true;
            } else {
                // 다를 경우
                pwMessage.innerText = "비밀번호가 일치하지 않습니다.";
                pwMessage.classList.add("error");
                pwMessage.classList.remove('confirm');
                checkObj.memberPwConfirm = false;                
            }

        }


    } else {
        // 유효하지 않은 경우 : 사용 불가능한 비밀번호 입니다. 빨간 글씨
        pwMessage.innerText = "사용 불가능한 비밀번호 입니다.";
        pwMessage.classList.add("error");
        pwMessage.classList.remove("confirm");
        checkObj.memberPw = false;        
    }

})


// 비밀번호 확인 유효성 검사
memberPwConfirm.addEventListener("input", () => {
    
    // 비밀번호가 입력되지 않은 경우
    if (memberPw.value == ""){
         alert("비밀번호를 입력해주세요.");
         memberPwConfirm.value = ""; // 남아있는값 지우기
         memberPw.focus();
         return;
    }
    
    // 비밀번호가 유효한 경우
    if (checkObj.memberPw) {
        if (memberPw.value == memberPwConfirm.value){
            // 비밃번호와 비밀번호 확인이 같을 경우
            pwMessage.innerText = "비밀번호가 일치합니다.";
            pwMessage.classList.add("confirm");
            pwMessage.classList.remove("error");

            checkObj.memberPwConfirm = true;

        } else {
            // 다를 경우
            pwMessage.innerText = "비밀번호가 일치하지 않습니다.";
            pwMessage.classList.add("error");
            pwMessage.classList.remove("confirm");

            checkObj.memberPwConfirm = false;

        }



    } else {
        // 비밀번호가 유효하지 않은 경우, 자동으로 memberPwConfirm false
        checkObj.memberPwConfirm = false;    
    }

})


// 이름 유효성 검사
const memberName = document.getElementById("memberName"); 
const nameMessage = document.getElementById("nameMessage");

memberName.addEventListener("input", () =>{
    // 입력된 이름이 없을 경우
    if (memberName == ""){
        nameMessage.innerText = "유효한 한글 이름 입력해 주세요"
        nameMessage.classList.remove("confirm", "error");
        checkObj.memberName = false;
        return;
    }

    // 입력된 이름이 있을 경우
    // 이름 작성시, 유효성 검사: 한글 2~15글자
    const regEx = /^[가-힣]{2,15}$/; //"한글2~15글자 사이"

    if (regEx.test(memberName.value)) {
        // 유효한 경우
        nameMessage.innerText = "유효한 한글 이름입니다."
        nameMessage.classList.add("confirm");
        nameMessage.classList.remove("error");
        checkObj.memberName = true;
        
    } else {
        // 유효하지 않은 경우 
        nameMessage.innerText = "유효하지 않은 한글이름입니다.  한글로만 2~15글자를 넣어주세요."
        nameMessage.classList.add("error");
        nameMessage.classList.remove("confirm");
        checkObj.memberName = false;        
    }

})

// 닉네임 유효성 검사
const memberNickname = document.getElementById("memberNickname"); 
const nicknameMessage = document.getElementById("nicknameMessage");

memberNickname.addEventListener("input", ()=> {

    // 입력된 닉네임이 없을 경우
    if (memberNickname == ""){
        nicknameMessage.innerText = "한글,영어,숫자로만 2~10글자로 입력해 주세요"
        nicknameMessage.classList.remove("confirm", "error");
        checkObj.memberNickname = false;
        return;
    }

    // 입력된 닉네임이 있을 경우
    // 닉네임 작성시, 유효성 검사: 한글,영어,숫자로만 2~10글자
    const regEx = /^[가-힣a-zA-Z0-9]{2,10}$/; //"영어/숫자/한글2~10글자 사이"

    if (regEx.test(memberNickname.value)) {
        // 유효한 경우

        //******************************************* */
        // fetch() API를 이용한 ajax
        // console.log("test : " + memberNickname.value)
        //console.log(memberNickname.value);

        // 요청주소 : /dupCheck/nickname
        // 중복인 경우 : "이미 사용 중인 닉네임입니다." 빨간 글씨
        // 중복이 아닌 경우 : "사용 가능한 닉네임입니다." 초록글씨
        fetch("/dupCheck/nickname?nickname=" + memberNickname.value) 
        .then(resp => resp.text()) // 0 or 1이므로 text로: 응답객체 -> 파싱(parsing, 데이터 형태 변환)
        .then(count => {
            // count : 중복1, 아니면 0
            console.log(count);

            //if (count == 1){
            if (count != 0) {
                nicknameMessage.innerText = "이미 사용 중인 닉네임입니다."
                nicknameMessage.classList.add("error");
                nicknameMessage.classList.remove("confirm");
                checkObj.memberNickname = false; 
            } else {
                nicknameMessage.innerText = "사용 가능한 닉네임입니다."
                nicknameMessage.classList.add("confirm");
                nicknameMessage.classList.remove("error");
                checkObj.memberNickname = true;
            }
        })
        .catch(err => console.log(err)) // 예외처리

    } else {
        // 유효하지 않은 경우 
        nicknameMessage.innerText = "유효하지 않은 닉네임입니다.  한글,영어,숫자로만 2~10글자를 넣어주세요."
        nicknameMessage.classList.add("error");
        nicknameMessage.classList.remove("confirm");
        checkObj.memberNickname = false;        
    }

})



// 전화번호 유효성 검사
const memberTel = document.getElementById("memberTel");
const telMessage = document.getElementById("telMessage");

memberTel.addEventListener("input", ()=>{

    // 전화번호 미 입력시
    if(memberTel.value == ""){
        telMessage.innerText = "전화번호를 입력해 주세요.";
        telMessage.classList.remove("confirm", "error");
        checkObj.memberTel = false;
        return;
    }

    // 전화번호 입력시, 유효성 검사: 전화번호를 입력해주세요.(- 제외)
    const regEx = /^0(1[01]|2|[3-6][1-5]|70)\d{7,8}$/; //" 하이픈제외"

    if (regEx.test(memberTel.value)) {
        telMessage.innerText = "유효한 전화번호입니다.";
        telMessage.classList.add("confirm");
        telMessage.classList.remove("error");
        checkObj.memberTel = true;
    } else {
        telMessage.innerText = "존재하지 않은 전화 번호입니다.";
        telMessage.classList.add("error");
        telMessage.classList.remove("confirm");
        checkObj.memberTel = false;        
    }

})

// 경력사항 유효성 검사
const memberCareer = document.getElementById("memberCareer");
const careerMessage = document.getElementById("careerMessage");


memberCareer.addEventListener("input", () =>{
    // 입력된 경력 사항이 없을 경우
    if (memberCareer == ""){
        careerMessage.innerText = "경력사항을 입력해 주세요(예시:벡엔드3년차)"
        careerMessage.classList.remove("confirm", "error");
        checkObj.memberCareer = false;
        return;
    }

    // 입력된 경력사항이 있을 경우
    // 경력사항 작성시, 유효성 검사: 한글 + 숫자 둘 다 반드시 포함 + 공백 허용 + 길이 2~40
    const regEx = /^(?=.*[가-힣])(?=.*[0-9])[가-힣0-9 ]{2,40}$/; //"경력(한글)과 년차(숫자)포함 2~40글자 사이"

    if (regEx.test(memberCareer.value)) {
        // 유효한 경우
        careerMessage.innerText = "유효한 경력사항입니다."
        careerMessage.classList.add("confirm");
        careerMessage.classList.remove("error");
        checkObj.memberCareer = true;
        
    } else {
        // 유효하지 않은 경우 
        careerMessage.innerText = "유효하지 않은 경력사항입니다.  경력(한글)과 년차(숫자)포함 2~40글자를 넣어주세요."
        careerMessage.classList.add("error");
        careerMessage.classList.remove("confirm");
        checkObj.memberCareer = false;        
    }

})

// 관리자 계정 신청시 관리자 승인코드 유효성 검사
const memberAdmin = document.getElementById("memberAdmin");

const adminCode = document.getElementById("adminCode");
const adminCodeMessage = document.getElementById("adminCodeMessage");

memberAdmin.addEventListener("change", () => {
    if (memberAdmin.checked) {
        console.log("관리자 계정 신청 체크됨");
        // 체크됐을 때 실행할 코드: 입력 adminCode유효성 검사
        checkObj.memberAdmin = false;

        adminCode.addEventListener("input", () => {
            if (adminCode.value == ""){
                adminCodeMessage.innerText = "발급된 관리자계정 승인코드 입력"
                adminCodeMessage.classList.remove("confirm", "error");
                checkObj.memberAdmin = false;
                return;
            }
            // 요청주소 : /checkCode/adminCode
            // 관리자승인코드 비유효 : "승인되지 않은 코드입니다." 빨간 글씨
            // 관리자승인 코드 유효 : "승인된 코드입니다." 초록글씨
            fetch("/checkCode/adminCode?adminCode=" + adminCode.value) // 입력파라미터명은 "adminCode"이 된다
    
            .then(resp => resp.text()) // 0 or 1이므로 text로: 응답객체 -> 파싱(parsing, 데이터 형태 변환)
            .then(result => {
                // result : 유효한 관리자 승인코드1, 아니면 0
                console.log("유효한 관리자승인코드 ? : " + result);
    
                //if (result == 1){
                if (result != 0) {
                    adminCodeMessage.innerText = "승인된 코드입니다."
                    adminCodeMessage.classList.add("confirm");
                    adminCodeMessage.classList.remove("error");
                    checkObj.memberAdmin = true;

                    console.log(" 지금까지 유효성 검사 결과: ")
                    console.log(checkObj)

                } else {
                    adminCodeMessage.innerText = "승인되지 않은 코드입니다."
                    adminCodeMessage.classList.add("error");
                    adminCodeMessage.classList.remove("confirm");
                    checkObj.memberAdmin = false; 
                }
            })
            .catch(err => console.log(err)) // 예외처리
        })

    } else {
        console.log("관리자  계정 신청 체크 해제됨");
        // 해제됐을 때 실행할 코드
        adminCodeMessage.innerText = ""
        adminCodeMessage.classList.remove("confirm", "error");        
        checkObj.memberAdmin = true;
    }
});

// 맨처음 로딩됬을때 상태 => 물론 아직 이벤트발생이 일어난적 없으니 모두 False 초기값임
console.log(" 초기 유효성 검사 상태: ")
console.log(checkObj)

document.getElementById("signUpFrm").addEventListener("submit", e => {

    // checkObj에 모든 value가 true인지 검사

    for (let key in checkObj) {
        console.log(key);

        console.log(checkObj[key]); 

        if(!checkObj[key]){ // 유효하지 않은 경우

            switch(key) {

                case 'memberEmail': alert("이메일이 유효하지 않습니다."); break;
                case 'memberPw': alert("비밀번호가 유효하지 않습니다."); break;
                case 'memberPwConfirm': alert("비밀번호확인이 유효하지 않습니다."); break;
                case 'memberName': alert("이름이 유효하지 않습니다."); break;
                case 'memberNickname': alert("닉네임이 유효하지 않습니다."); break;
                case 'memberTel': alert("전화번호가 유효하지 않습니다."); break;
                case 'memberCareer': alert("경력사항이 유효하지 않습니다."); break;
                case 'memberSubscribe': alert("이메일 수신동의가 유효하지 않습니다."); break;
                case 'memberAdmin': alert("관리자 승인코드가 유효하지 않습니다."); break;
                case 'authKey': alert("이메일 인증 authKey가 유효하지 않습니다."); break;
            }

            // 유효하지 않은 input 태그 포커스
            // -> key와 input의 id가 똑같음
            document.getElementById(key).focus();
            
            // form태그 기본 이벤트 제거
            e.preventDefault(); // 기본이벤트 제거(form submit 방지)
            return; // 유효하지 않은것 확인했으므로 종료하고 더이상 진행않함
        }

    }

})
