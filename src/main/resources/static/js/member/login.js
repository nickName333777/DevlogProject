console.log("login.js loaded...");



const loginFrm = document.getElementById("loginFrm");
const memberEmail = document.querySelector("input[name='memberEmail']");
const memberPw = document.getElementsByName("memberPw")[0];
const saveId = document.getElementById("saveId");

if (loginFrm != null) {
    loginFrm.addEventListener("submit", e => {
        // 기본 폼 제출 막기
        e.preventDefault();

        // 이메일 유효성 검사
        if (memberEmail.value.trim().length == 0) {
            alert("이메일을 입력해 주세요");
            memberEmail.focus();
            memberEmail.value = '';
            return;  // 이메일 미 입력시 무조건 return해야 아래 비밀번호 검사 안함
        }

        // 비밀번호 유효성 검사
        if (memberPw.value.trim().length == 0) {
            alert("비밀번호를 입력해 주세요");
            memberPw.focus();
            memberPw.value = '';
            return;
        }

        const formData = new URLSearchParams();
        formData.append("memberEmail", memberEmail.value.trim());
        formData.append("memberPw", memberPw.value);

        // 체크되어 있으면 saveId 전송
        if (saveId.checked) {
            formData.append("saveId", "on"); // 값은 아무 문자열이나 OK(null 만아니면됨)
        }

        // 디버깅
        console.log("saveId checked:", saveId.checked);
        console.log(formData.toString());

        fetch('/member/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: formData
        })
        .then(async response => {
            if (!response.ok) {
                // 실패 시 Body에서 메시지 추출
                const data = await response.json();
                if(data == null) {
                    console.log('data is null');
                }
                else {
                    console.log(data); 
                }
                throw new Error(data.message || '로그인 실패');
            }
            return response.json();
        })
        .then(data => {
            console.log('로그인 성공:', data);
            // 메인 페이지로 이동
            window.location.href = '/'; //  브라우저가 해당 URL로 새로 요청 -> 현재 페이지에서 / 경로로 브라우저가 이동 (redirect)
            //window.location.href = '/member/login'; //  session.loginMember 갱신 테스트용
            // 클라이언트 측에서 페이지 이동이 일어나므로 redirect 동작
        })
        .catch(error => {
            console.error('로그인 오류:', error);
            alert(error.message); // 로그인 실패(401 Unauthorized) 또는 기타 서버 오류(500 INTERNAL_SERVER_ERROR)
            // 실패 시 비밀번호 초기화
            memberPw.value = '';
            memberPw.focus();
        });
    });
}


// 카카오 소셜로그인
const kakaoLoginBtn = document.getElementById("kakaoLoginBtn");

kakaoLoginBtn.addEventListener("click", function () {
            // DevLog 서비스 서버로 이동
            window.location.href = "/app/login/kakao";
});


// 자바스크립트 쿠키 얻어오기: key를 전달하면, value얻는 JS함수
function getCookie(key) {
    const cookies = document.cookie;
    // 현재 페이지에서 접근 가능한 모든 쿠키를 "문자열"로 가지고 옴(특정쿠키만 가져오는건 spring-boot version 3이상에서 않됨)

    console.log("inside getCookie() function: ");
    console.log(cookies);

    // saveId=user01@og.or.kr; test=123; temp=abc;
    // 배열.map() : 배열의 모든 요소에 순차 접근하여 함수 수행 후
    //             수행 결과를 이용해서 새로운 배열을 만드는 함수
    const cookieList = cookies.split("; ").map(cookie => cookie.split("="))
    // {saveId=user01@og.or.kr, test=123, temp=abc}
    // 
    //console.log(cookieList); // 

    // [[a, 1], [b, 1]]
    const obj = {}; // 비어있는 JS 객체 생성

    for(let i=0; i<cookieList.length; i++) {
        obj[cookieList[i][0]] = cookieList[i][1]; // K값에 V를 대입 형식으로 담겨지게 된다. 
    } // obj = {a:1, b:1}

    return obj[key];
}


// 쿠키에 saveId가 있을 경우
if( document.getElementsByName("memberEmail")[0] != null ) {
    // 화면에 memberEmail이 있을 경우

    const saveId = getCookie("saveId");
    // 있으면 이메일, 없으면 undefined 나온다

    console.log("cookie(saveId) in current view by getCookie() 함수: ");
    console.log(saveId);

    if (saveId != undefined) { // 쿠키에 저장된 이메일이 있는 경우
        // memberEmail input에 값 세팅
        // document.getElementsByName("memberEmail")[0].value = saveId;
        document.querySelector("input[name='memberEmail'").value = saveId;
        // 아이디 저장 checkbox 체크하기
        // document.getElementsByName("saveId")[0].checked = true;
        document.querySelector("input[name='saveId']").checked = true;

    }
}