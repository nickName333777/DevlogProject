console.log("pwCheck.js loaded");

document.addEventListener('DOMContentLoaded', () => {
    const confirmBtn = document.getElementById('confirmBtn');
    const passwordInput = document.getElementById('passwordInput');
    const errorModal = document.getElementById('errorModal');

    // 실제 서버 검증 로직
    const verifyPassword = async () => {
        const inputVal = passwordInput.value;

        if (!inputVal) {
            alert("비밀번호를 입력해주세요.");
            return;
        }

        try {
            // 1. 서버로 비밀번호 전송
            const response = await fetch('/api/myPage/verify-password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ password: inputVal })
            });

            if (!response.ok) throw new Error("서버 오류");

            const isMatch = await response.json(); // true or false

            if (isMatch) {
                // 2. 일치하면 설정 페이지로 이동
                // alert('확인되었습니다.'); // (선택사항)
                window.location.href = "/myPage/settings"; 
            } else {
                // 3. 불일치하면 모달 띄우기
                errorModal.classList.remove('hidden');
            }

        } catch (error) {
            console.error("비밀번호 검증 실패:", error);
            alert("오류가 발생했습니다.");
        }
    };

    confirmBtn.addEventListener('click', verifyPassword);

    passwordInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') verifyPassword();
    });
});

function closeModal() {
    document.getElementById('errorModal').classList.add('hidden');
    document.getElementById('passwordInput').focus();
    document.getElementById('passwordInput').value = ''; // 틀렸으니 비워주기
}