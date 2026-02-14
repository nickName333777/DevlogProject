console.log("mySetting.js loaded");

document.addEventListener('DOMContentLoaded', () => {
    // -------------------------------------------------------
    // 1. 탭 전환 로직 (기존 UI 유지)
    // -------------------------------------------------------
    const menuItems = document.querySelectorAll('.menu-item');
    const sections = document.querySelectorAll('.profile-section');

    menuItems.forEach(item => {
        item.addEventListener('click', () => {
            const target = item.getAttribute('data-target');

            // 메뉴 활성화
            menuItems.forEach(mi => mi.classList.remove('active'));
            item.classList.add('active');

            // 섹션 활성화
            sections.forEach(sec => {
                sec.classList.remove('active');
                if (sec.id === target) sec.classList.add('active');
            });
        });
    });

    // -------------------------------------------------------
    // 2. [기능] 프로필 이미지 변경 (서버 업로드)
    // -------------------------------------------------------
    const fileInput = document.getElementById('fileInput');
    const profileDisplay = document.getElementById('profileDisplay');
    const deleteImgBtn = document.getElementById('deleteImgBtn');

    // (1) 파일 선택 시 -> 즉시 서버로 업로드 -> 이미지 변경
    if (fileInput) {
        fileInput.addEventListener('change', async (e) => {
            const file = e.target.files[0];
            if (!file) return;

            const formData = new FormData();
            formData.append("image", file);

            try {
                // 아까 만든 이미지 업로드 API 호출
                const response = await fetch('/api/myPage/update/image', {
                    method: 'POST',
                    body: formData
                });

                if (response.ok) {
                    const newUrl = await response.text(); // 서버가 돌려준 새 이미지 주소
                    profileDisplay.src = newUrl;
                    alert("프로필 이미지가 변경되었습니다.");

                    // (선택사항) 헤더에 있는 작은 프사도 같이 바꾸고 싶다면 아래 주석 해제
                    // const headerProfile = document.querySelector('.profile img');
                    // if(headerProfile) headerProfile.src = newUrl;
                } else {
                    alert("이미지 업로드에 실패했습니다.");
                }
            } catch (error) {
                console.error("Error:", error);
                alert("서버 오류가 발생했습니다.");
            }
        });
    }

    // (2) 이미지 삭제 버튼 (UI만 초기화 - DB 반영은 별도 API 필요하나 일단 기본 이미지로 보여줌)
    if (deleteImgBtn) {
        deleteImgBtn.addEventListener('click', () => {
            if (confirm("이미지를 기본 이미지로 변경하시겠습니까? (저장은 아직 안 됩니다)")) {
                profileDisplay.src = '/images/member/user.png';
                fileInput.value = "";
            }
        });
    }

    /* 닉네임 중복 검사 */
    const btnCheckNickname = document.getElementById("btnCheckNickname");
    const memberNickname = document.getElementById("memberNickname");
    const nicknameMessage = document.getElementById("nicknameMessage");
    const nicknameChecked = document.getElementById("nicknameChecked");
    const originNickname = document.getElementById("originNickname");

    // 1. 닉네임 입력값이 변경되면 "중복 확인 다시 하세요" 상태로 변경
    if (memberNickname) {
        memberNickname.addEventListener("input", () => {
            // 원래 내 닉네임과 같으면 굳이 중복검사 필요 없음 (true 처리)
            // 1. 지금 입력한 값이 원래 내 닉네임과 똑같니?
            if (memberNickname.value === originNickname.value) {
                // 똑같으면 검사 안 해도 됨 (통과)
                nicknameMessage.innerText = "";
                nicknameChecked.value = "true";
            } else {
                // 혹시 다른 사람이 쓰고 있을지 모르니까 '중복 확인' 버튼 누르게 유도함 (차단)
                nicknameMessage.innerText = "중복 확인이 필요합니다.";
                nicknameMessage.className = "form-msg error";
                nicknameChecked.value = "false"; // 여기서 false가 되면 '수정하기' 버튼이 안 먹힘
            }
        });
    }

    // 2. 중복 확인 버튼 클릭 이벤트
    if (btnCheckNickname) {
        btnCheckNickname.addEventListener("click", async () => {
            const nickname = memberNickname.value.trim();

            // 유효성 검사 (빈값 등)
            if (nickname.length === 0) {
                alert("닉네임을 입력해주세요.");
                return;
            }

            // 닉네임 정규식 (한글,영문,숫자 2~10글자)
            const regExp = /^[가-힣a-zA-Z0-9]{2,10}$/;
            if (!regExp.test(nickname)) {
                nicknameMessage.innerText = "닉네임은 한글,영어,숫자 2~10글자로 입력해주세요.";
                nicknameMessage.className = "form-msg error";
                nicknameChecked.value = "false";
                return;
            }

            try {
                // API 호출
                const resp = await fetch(`/api/myPage/check/nickname?nickname=${nickname}`);
                const result = await resp.text(); // 0 or 1

                if (Number(result) === 0) {
                    nicknameMessage.innerText = "사용 가능한 닉네임입니다.";
                    nicknameMessage.className = "form-msg success";
                    nicknameChecked.value = "true"; // 통과
                } else {
                    nicknameMessage.innerText = "이미 사용 중인 닉네임입니다.";
                    nicknameMessage.className = "form-msg error";
                    nicknameChecked.value = "false"; // 실패
                }

            } catch (e) {
                console.error(e);
                alert("중복 검사 중 오류가 발생했습니다.");
            }
        });
    }
});

// -------------------------------------------------------
// 3. [기능] 내 정보 변경 (버튼 클릭 시 호출)
// -------------------------------------------------------
async function handleUpdate(type) {

    // (1) 정보 수정
    if (type === 'info') {
        const nicknameChecked = document.getElementById("nicknameChecked");
        const memberNickname = document.getElementById("memberNickname");

        // [중요] 중복 확인 여부 검사
        if (nicknameChecked.value === "false") {
            alert("닉네임 중복 확인을 진행해주세요.");
            memberNickname.focus();
            return;
        }

        // 입력된 값 가져오기
        const data = {
            memberNickname: memberNickname.value,
            memberCareer: document.getElementById('memberCareer').value,
            myInfoIntro: document.getElementById('myInfoIntro').value,
            memberTel: document.getElementById('memberTel').value,
            myInfoGit: document.getElementById('myInfoGit').value,
            myInfoHomepage: document.getElementById('myInfoHomepage').value
        };

        try {
            // 정보 수정 API 호출
            const response = await fetch('/api/myPage/update/info', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            const result = await response.json();

            if (result.success) {
                alert(result.message); // "수정되었습니다."
                location.reload();     // 헤더 닉네임 갱신을 위해 새로고침
            } else {
                alert(result.message); // "중복된 닉네임입니다." 등
            }
        } catch (error) {
            console.error("Update failed:", error);
            alert("수정 중 오류가 발생했습니다.");
        }
    }
    // (2) 비밀번호 변경 (추후 구현)
    else if (type === 'pw') {

        const currentPw = document.getElementById("currentPw");
        const newPw = document.getElementById("newPw");
        const newPwConfirm = document.getElementById("newPwConfirm");

        if (!currentPw.value) {
            alert("현재 비밀번호를 입력해주세요.");
            currentPw.focus();
            return;
        }

        if (!regExp.test(newPw.value)) {
            alert("새 비밀번호 형식이 올바르지 않습니다.");
            newPw.focus();
            return;
        }

        if (newPw.value !== newPwConfirm.value) {
            alert("새 비밀번호가 서로 일치하지 않습니다.");
            newPwConfirm.focus();
            return;
        }

        const data = {
            currentPw: currentPw.value,
            newPw: newPw.value,
        }

        const resp = await fetch("/api/changePw", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(data)
        })

        const result = Number(await resp.text());

        if (result === 0) {
            alert("현재 비밀번호가 일치하지 않습니다.");
            currentPw.focus();
        } else if (result === 1) {
            alert("기존 비밀번호와 같은 비밀번호로 바꿀 수 없습니다.");
            newPw.value = '';
            newPwConfirm.value = '';
            newPw.focus();
        } else {
            alert("비밀번호가 변경되었습니다.");
            location.reload();
        }
    }
    // (3) 구독 설정 
    else if (type === 'sub') {
        const subPrice = document.getElementById("subscriptionPrice").value;

        if (subPrice < 5000) {
            alert("구독료는 최소 5000입니다.");
            return;
        }

        console.log(subPrice, "구독료 확인");

        fetch("/api/myPage/subscribe", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                price: subPrice
            })
        })
            .then(resp => {
                if (resp.ok) {
                    alert("구독 설정 완료");
                }
            })
            .catch(e => console.log("구독 설정 실패", e));

    }
}

// -------------------------------------------------------
// 4. 회원 탈퇴
// -------------------------------------------------------
async function handleWithdraw() {
    const agree = document.getElementById('agreeTerm');
    if (agree && !agree.checked) {
        alert("약관 동의가 필요합니다.");
        return;
    }

    const withdrawPw = document.getElementById("withdrawPw");
    if (withdrawPw.value.trim() == '') {
        alert("비밀번호를 입력해주세요.");
        return;
    }

    if (confirm("정말 탈퇴하시겠습니까? (복구 불가)")) {

        const resp = await fetch("/api/withdraw", {
            method: "DELETE",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                checkPw: withdrawPw.value
            })
        })

        const result = Number(await resp.text());

        if (result === 0) {
            alert("비밀번호가 일치하지 않습니다.");
            withdrawPw.focus();
        }
        else if (result === -1) {
            alert("탈퇴 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
        else {
            alert("탈퇴가 완료되었습니다.");
            location.href = "/";
        }


    }
}




const newPw = document.getElementById("newPw");
const newPwConfirm = document.getElementById("newPwConfirm");
const pwMsg = document.getElementById("pwMsg");
const pwConfirmMsg = document.getElementById("pwConfirmMsg");

const regExp = /^[\w!@#\-_]{6,20}$/;

function validatePassword() {
    const pw = newPw.value;
    const confirm = newPwConfirm.value;

    if (!regExp.test(pw)) {
        pwMsg.textContent = "비밀번호는 6~20자, 영문·숫자·!@#-_ 만 사용할 수 있습니다.";
        pwMsg.className = "form-msg error";
    } else {
        pwMsg.textContent = "사용 가능한 비밀번호입니다.";
        pwMsg.className = "form-msg success";
    }

    if (confirm.length === 0) {
        pwConfirmMsg.textContent = "";
        return;
    }

    if (pw === confirm) {
        pwConfirmMsg.textContent = "비밀번호가 일치합니다.";
        pwConfirmMsg.className = "form-msg success";
    } else {
        pwConfirmMsg.textContent = "비밀번호가 일치하지 않습니다.";
        pwConfirmMsg.className = "form-msg error";
    }
}

newPw.addEventListener("input", validatePassword);
newPwConfirm.addEventListener("input", validatePassword);
