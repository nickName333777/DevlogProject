console.log("fbChatbot.js loaded");

console.log("BeansAmount", window.beansAmount)


// 기본 설정
const chatBox = document.getElementById("chatBox"); // 주요소1
const chatInput = document.getElementById("chatInput"); // 주요소2
const sendBtn = document.getElementById("sendBtn"); // 주요소3

let currentSessionId = null;  // 서버에서 받은 실제 세션 ID
let accumulated_usedBeans = 0;
let beansAmount2update = 0; // window.beansAmount - accumulated_usedBeans

let totalServerTokens = 0;
let totalClientTokens = 0;
let lastQuestion = "";
let lastAiAnswer = "";

///////////////
// 기본 설정

// 페이지 로드 시 커피콩 체크 및 세션 시작
window.addEventListener('DOMContentLoaded', function() {
    checkBeansAndStartSession(); // 최초 가용 beansAmount 체크
    setupTextareaLimit();  // 입력 글자 수 제한 설정
});

// 창 닫을 때 세션 종료
window.addEventListener('beforeunload', function() {
    if(currentSessionId) {
        endChatbotSession();
    }
});




/**
 * 커피콩 체크 후 세션 시작
 */
function checkBeansAndStartSession() {
    const chatbotType = document.getElementById("chatbotType")?.value || "basic";
    
    // 로그인 체크 (커피콩 챗봇의 경우)
    if(chatbotType === "kong" && !window.loginMemberNo) {
        alert("커피콩 충전형 챗봇은 로그인이 필요합니다.");
        window.close();
        return;
    }
    
    // 커피콩 챗봇인 경우 잔액 체크
    if(chatbotType === "kong") {
        const beansAmount = window.beansAmount || 0;
        
        if(beansAmount <= 0) {
            //alert("커피콩 충전 후 이용해 주세요");
            alert( `
                커피콩 잔액이 ${beansAmount}입니다.\n커피콩충전형 챗봇은 커피콩 충전 후 이용해 주세요.
                `.trim());
            
            // 부모 창이 있으면 부모 창을 리다이렉트, 없으면 현재 창
            if(window.opener) {
                window.opener.location.href = "/coffeebeans"; // 커피콩 충전화면으로 이동
                window.close();
            } else {
                window.location.href = "/coffeebeans";
            }
            return;
        }
    }
    
    // 커피콩이 충분하면 세션 시작
    startChatbotSession();
}

/**
 * 입력 글자 수 제한 설정 (BASIC 타입만)
 */
function setupTextareaLimit() {
    const chatbotType = document.getElementById("chatbotType")?.value || "basic";
    const textarea = document.getElementById("chatInput");
    
    if(!textarea) return;
    
    if(chatbotType === "basic") {  // html의 select태그에서
        // // BASIC 타입: 500자 제한
        // textarea.maxLength = 500;
        // BASIC 타입: 20자 제한 (테스트용)
        textarea.maxLength = 20;
        
        // 글자 수 표시 추가
        const charCounter = document.createElement('div');
        charCounter.id = 'charCounter';
        charCounter.style.cssText = 'text-align: right; font-size: 12px; color: #777; padding: 5px 10px;';
        // charCounter.textContent = '0 / 500자';
        charCounter.textContent = '0 / 20자';
        
        const inputArea = document.querySelector('.input-area');
        if(inputArea && !document.getElementById('charCounter')) {
            inputArea.insertBefore(charCounter, textarea);
        }
        
        // 입력 시 글자 수 업데이트
        textarea.addEventListener('input', function() {
            let value = this.value;

            if (value.length > 20) {
                this.value = value.slice(0, 20) + '...';
            }

            const length = this.value.length;
            // charCounter.textContent = `${length} / 500자`;
            charCounter.textContent = `${length} / 20자`; // 테스트용
            
            // if(length >= 500) {
            //     charCounter.style.color = 'red';
            // } else if(length >= 450) {
            //     charCounter.style.color = 'orange';
            // } else {
            //     charCounter.style.color = '#777';
            // }
            if(length >= 20) {
                charCounter.style.color = 'red';
            } else if(length >= 15) {
                charCounter.style.color = 'orange';
            } else {
                charCounter.style.color = '#777';
            }
        });
    } else { 
        // KONG 타입: 4000자 제한 (기존 maxlength)
        textarea.maxLength = 4000;
        
        // 기존 글자 수 표시 제거
        const existingCounter = document.getElementById('charCounter');
        if(existingCounter) {
            existingCounter.remove();
        }
    }
}


/**
 * 챗봇 세션 시작
 */
function startChatbotSession() {

    // 자식 팝업 (챗봇 basic 팝업)에서 부모(수정화면에서 정의된 전역변수 받아오기)
    if (window.opener && window.opener.globalData) {
        const boardNo = window.opener.globalData.boardNo;
        const loginMemberNo = window.opener.globalData.loginMemberNo;

        console.log("window.opener.globalData.boardNo = ", boardNo);
        console.log("window.opener.globalData.loginMemberNo = ", loginMemberNo);
    }

    console.log("챗봇 세션 시작위한 세션 정보 수집.....")
    const chatbotType = document.getElementById("chatbotType")?.value || "basic";
    //// 이거 로컬에는 없다. global에서 받아와야한다 (freeboard객체는 없고, loginMember객체는 있다?!)
    const cbBoardType = window.opener?.globalData?.boardNo != null ? "UPDATE" : "INSERT"; // 어쩌면 popup window에서는 않되는듯
    console.log(" window.boardNo = ", window.boardNo);
    console.log(" cbBoardType = ", cbBoardType);
    console.log(" window.loginMemberNo = ", window.loginMemberNo);
    console.log("수집된 챗봇 팝업창 보드 타입(UPDATE or INSERT) cbBoardType = ", cbBoardType);
    
    const requestData = {
        cbSessionType: chatbotType.toUpperCase(),  // BASIC, KONG
        cbBoardType: cbBoardType,                   // INSERT, UPDATE
        boardNo: window.boardNo || null
    };
    console.log("수집된 챗봇 세션 정보", requestData);
    
    fetch('/api/chatbot/session/start', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestData)
    })
    .then(res => res.json())
    .then(data => {
        if(data.success) {
            currentSessionId = data.sessionId; // Long currentSessionId 임
            console.log("챗봇 세션 시작, 현 세션번호 = ", currentSessionId);
            
            // 세션 시작 후 커피콩 정보 업데이트
            if(window.loginMemberNo && chatbotType === "kong") {
                updateBeansDisplay();
            }
        } else {
            console.error("세션 시작 실패:", data.error);
            alert("챗봇 세션 시작에 실패했습니다.");
        }
    })
    .catch(err => {
        console.error("세션 시작 오류:", err);
        alert("챗봇을 시작할 수 없습니다.");
    });
}


/**
 * 챗봇 세션 종료
 * fetch with keepalive 사용 - beforeunload에서도 확실히 전송됨
 */
function endChatbotSession() {
    if(!currentSessionId) return;
    
    const chatbotType = document.getElementById("chatbotType")?.value;
    
    // 1. KONG 타입만 과금 처리
    if(chatbotType === "kong" && window.loginMemberNo) {

        console.log("챗봇 세션 종료 시작 - 누적 사용 커피콩:", accumulated_usedBeans);
        
        // 최종 잔여 커피콩 계산 (매우 중요!)
        const initialBeans = window.beansAmount || 0;
        const finalBeansAmount = Math.max(0, initialBeans - accumulated_usedBeans);

        console.log("=== 챗봇 세션 종료 정보 ===");
        console.log("초기 커피콩:", initialBeans);
        console.log("누적 사용 커피콩:", accumulated_usedBeans);
        console.log("최종 잔여 커피콩:", finalBeansAmount);
        console.log("========================");

        // 아무 질문도 안 했으면 업데이트 안 함
        if(accumulated_usedBeans > 0) {
            
            // fetch with keepalive 사용 (sendBeacon 대신)
            // keepalive: true -> 페이지가 닫혀도 요청 완료까지 유지
            fetch('/api/chatbot/freeboard/updateBeansAmount', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    loginMemberNo: window.loginMemberNo,
                    updatedBeansAmount: finalBeansAmount
                }),
                keepalive: true  // 페이지 종료 후에도 요청 유지
            })
            .then(response => {
                if(response.ok) {
                    console.log(" Member 테이블 업데이트 성공");
                } else {
                    console.error(" Member 테이블 업데이트 실패:", response.status);
                }
            })
            .catch(err => {
                console.error("Member 테이블 업데이트 오류:", err);
            });
            
            console.log("Member 테이블 업데이트 요청 전송:", {
                loginMemberNo: window.loginMemberNo,
                updatedBeansAmount: finalBeansAmount
            });
            
        } else {
            console.log(" 커피콩 사용 없음 - DB 업데이트 생략");
        }
    }
    
    // 2. KONG 타입만 과금 처리 결과 COFFEE_BEANS_TRADE 테이블에 업데이트
    //==>실제 devlog 프로젝트에서 COFFEE_BEANS_TRADE 결제 내역 삽입
    // 1. KONG 타입만 & 로그인멤버 과금 처리(DB기록)
    if(chatbotType === "kong" && window.loginMemberNo) {
         if(accumulated_usedBeans > 0) { // 누적 콩사용 내역 있을때만 전송

             const paymentBlob = new Blob([JSON.stringify({
                 contentType: "CHATBOT",
                 contentId: currentSessionId,
                 price: accumulated_usedBeans
             })], { type: 'application/json' });
             
             navigator.sendBeacon('/payment/trade', paymentBlob);
             
             console.log("챗봇 사용 데이터 전송 완료");

         }
    }    


    // 3. 세션 종료 (항상 실행)
    fetch(`/api/chatbot/session/end/${currentSessionId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({}),
        keepalive: true  //  이것도 keepalive 사용
    })
    .then(response => {
        if(response.ok) {
            console.log(" 챗봇 세션 종료 성공:", currentSessionId);
        }
    })
    .catch(err => {
        console.error("세션 종료 오류:", err);
    });
}

// /////////// 유틸
function tokenCalc(text) {
    return Math.ceil(text.length / 4); // 4ch.-> 1 token, ballpark guessing
}


/**
 * 토큰 사용량 표시 업데이트
 */
function updateTokenDisplay(promptTokens, completionTokens, totalTokens, accumulatedUsedBeans) {
    // 전역변수 업데이트 
    accumulated_usedBeans = accumulatedUsedBeans || 0;
    
    // 잔여 커피콩 계산
    const initialBeans = window.beansAmount || 0;
    beansAmount2update = Math.max(0, initialBeans - accumulated_usedBeans); // 음수 방지

    console.log("=== 토큰 업데이트 상세 ===");
    console.log("현재 턴 토큰:", {
        promptTokens,
        completionTokens,
        totalTokens
    });
    console.log("누적 정보:", {
        accumulatedUsedBeans,
        initialBeans,
        beansAmount2update
    });
    console.log("=======================");



    // 화면 표시 업데이트
    const row1 = document.getElementById("beansAmount");
    if(row1) {
        row1.textContent = `콩 잔액: ${beansAmount2update.toLocaleString()} 포인트`;
    }

    const row2 = document.querySelector('.tu-row2 .token-info');
    if(row2) {
        row2.textContent = `사용 토큰: ${totalTokens} (질문: ${promptTokens}, 답변: ${completionTokens}), 사용 콩: ${accumulatedUsedBeans}`;
    }
    
    totalServerTokens += totalTokens;
    
    //// 커피콩이 0 이하가 되면 경고
    //if(beansAmount2update <= 0) {
    // 유료형 챗봇의 경우만(KONG 타입만 과금 처리), 커피콩이 0 이하가 되면 경고
    if(chatbotType === "kong" && data.remainingBeans <= 0) {        
        alert("커피콩이 모두 소진되었습니다. 충전 후 이용해 주세요.");

        // 세션 종료 전에 DB 업데이트
        endChatbotSession();
        
        // 충전 페이지로 이동
        if(window.opener) {
            window.opener.location.href = "/coffeebeans";
            window.close();
        } else {
            window.location.href = "/coffeebeans";
        }
    }
}



function updateBeansDisplay() {
    fetch('/api/chatbot/freeboard/usage')
        .then(res => res.json())
        .then(data => {
            const row1Divs = document.querySelectorAll('.tu-row1 .token-info');
            if(row1Divs.length >= 1 && data.remainingBeans !== undefined) {
                row1Divs[0].textContent = `커피콩 잔액: ${data.remainingBeans.toLocaleString()} 포인트`;
                
                // 실시간으로 beansAmount 업데이트
                window.beansAmount = data.remainingBeans;
                
                // 커피콩이 0 이하가 되면 경고
                // if(data.remainingBeans <= 0) {                
                // 유료형 챗봇의 경우만(KONG 타입만 과금 처리), 커피콩이 0 이하가 되면 경고
                if(chatbotType === "kong" && data.remainingBeans <= 0) {
                    alert("커피콩이 모두 소진되었습니다. 충전 후 이용해 주세요.");
                    
                    if(window.opener) {
                        window.opener.location.href = "/coffeebeans";
                        window.close();
                    } else {
                        window.location.href = "/coffeebeans";
                    }
                }
            }
            if(row1Divs.length >= 2 && data.totalBeans !== undefined) {
                row1Divs[1].textContent = `(총 사용: ${data.totalBeans.toLocaleString()} 커피콩)`;
            }
        })
        .catch(err => {
            console.warn('커피콩 정보 업데이트 실패:', err);
        });
}



function scrollToBottom() { // 맨밑으로 채팅창 스크롤 내리기
    chatBox.scrollTop = chatBox.scrollHeight;
}

function now() { // timestamp
    return new Date().toLocaleString();
}




/**
 * 말풍선 복사 메뉴 표시
 */
function showCopyMenu(event, bubbleElement) {
    event.stopPropagation();
    
    // 기존 메뉴 제거
    const existingMenu = document.querySelector('.copy-menu');
    if(existingMenu) {
        existingMenu.remove();
    }
    
    // 복사 메뉴 생성
    const menu = document.createElement('div');
    menu.className = 'copy-menu';
    menu.innerHTML = '<button onclick="copyBubbleText(event)">📋 복사</button>';
    
    // 메뉴 위치 설정
    menu.style.position = 'absolute';
    menu.style.left = event.pageX + 'px';
    menu.style.top = event.pageY + 'px';
    menu.style.zIndex = '1000';
    
    // 복사할 텍스트를 data 속성에 저장
    const textContent = bubbleElement.innerText
        .replace(bubbleElement.querySelector('.time')?.innerText || '', '')
        .replace(bubbleElement.querySelector('.name')?.innerText || '', '')
        .trim();
    menu.dataset.copyText = textContent;
    
    document.body.appendChild(menu);
    
    // 다른 곳 클릭하면 메뉴 닫기
    setTimeout(() => {
        document.addEventListener('click', closeCopyMenu);
    }, 100);
}

/**
 * 말풍선 텍스트 복사
 */
function copyBubbleText(event) {
    event.stopPropagation();
    
    const menu = event.target.closest('.copy-menu');
    const textToCopy = menu.dataset.copyText;
    
    navigator.clipboard.writeText(textToCopy)
        .then(() => {
            alert('복사되었습니다!');
            menu.remove();
        })
        .catch(err => {
            console.error('복사 실패:', err);
            // 폴백: textarea 사용
            const textarea = document.createElement('textarea');
            textarea.value = textToCopy;
            document.body.appendChild(textarea);
            textarea.select();
            document.execCommand('copy');
            document.body.removeChild(textarea);
            alert('복사되었습니다!');
            menu.remove();
        });
}

/**
 * 복사 메뉴 닫기
 */
function closeCopyMenu() {
    const menu = document.querySelector('.copy-menu');
    if(menu) {
        menu.remove();
    }
    document.removeEventListener('click', closeCopyMenu);
}





/////////////////////////////////////////////////////////////
// ////////// 메시지 UI
function addUserMessage(text) {
    const row = document.createElement("div");
    row.className = "chat-row right";

    const nickname = window.memberNickname || "유저";

    row.innerHTML = `
        <div class="bubble user onclick="showCopyMenu(event, this)">
            ${text}
            <div class="time">${now()}</div>
            <div class="name">${nickname}</div>
        </div>
        <img src=${profileImg} class="bot-img">
    `;

    chatBox.appendChild(row);
    scrollToBottom();

}

function addBotMessage(text) {
    const row = document.createElement("div");
    row.className = "chat-row left";

    // <img src="/images/board/freeboard/chatbot1.png" class="bot-img">
    const botImg = window.cbtProfileImg || "/images/board/freeboard/chatbot1.png";
    //const botName = window.chatbotId || "DevLog 챗봇";
    // chatbotId = "BASIC" 또는 "KONG"

    row.innerHTML = `
        <img src=${botImg} class="bot-img">
        <div>
            <div class="bubble bot" onclick="showCopyMenu(event, this)">
                ${text.replace(/\n/g, "<br>")}
            </div>
            <div class="time">${now()}</div>
            <div class="name">DevLog 챗봇</div>
        </div>
    `;

    chatBox.appendChild(row);
    scrollToBottom();

}




////////////////////////////////////////////////////////////
// OpenAI (Spring AI) 연동:==> 실제 질문보내고, 돌아온 응답 (한 라운드) 받아처리
function sendMessage() {
    const msg = chatInput.value.trim();
    if (!msg) return;

    // 세션 체크
    if(!currentSessionId) {
        alert("챗봇 세션이 시작되지 않았습니다. 페이지를 새로고침해주세요.");
        return;
    }    


    // 메세지를 보내는 도중 커피콩 떨어진 경우: 과금 DB작업되서 실시간 beansAmount업데이트 될때 유효
    // 커피콩 챗봇인 경우 매 메시지 전송 전 잔액 체크 
    const chatbotType = document.getElementById("chatbotType")?.value;
    if(chatbotType === "kong") {
        const currentBeans = beansAmount2update > 0 ? beansAmount2update : window.beansAmount;
        if(currentBeans <= 0) {
            alert("커피콩이 부족합니다. 충전 후 이용해 주세요.");
            if(window.opener) {
                window.opener.location.href = "/coffeebeans";
                window.close();
            }
            return;
        }
    }

    

    chatInput.value = "";

    // 1) 유저 질문 화면에 보여주기(Question)
    addUserMessage(msg);

    // 2) 유저 질문을 실제 openAI에 보내기
    fetch(`/api/chatbot/freeboard/${currentSessionId}`, {
        method: "POST",
        headers: { "Content-Type": "text/plain" },
        body: msg
        //headers: { "Content-Type": "application/json" },
        //body: JSON.stringify({ message: msg })
        })
        .then(res => {
            if(!res.ok) {
                throw new Error(`서버 응답 오류: ${res.status}`);
            }
            return res.json();
        }) 
        .then(data => {
            console.log("chatbot answer(서버 응답):");
            console.log(data); // 챗봇으로 부터 온 대답이 뭔지 확인

            if(data.error) {
                addBotMessage(`!!! ${data.error}`);
                return;
            }


            // 3) 챗봇 대답 화면에 보여주기(Answer)
            addBotMessage(data.reply); 
            //addBotMessage(data.reply ?? data.content);

            // 토큰 사용량 표시: 실제 질문보내고, 돌아온 응답 (한 라운드) 받아처리
            // 토큰 사용량 업데이트
            if (data.usage) {
                const { 
                    prompt_tokens, 
                    completion_tokens, 
                    total_tokens, 
                    accumulated_tokens,
                    accumulated_usedBeans: serverUsedBeans
                } = data.usage;
                // 반드시 체크
                console.log("##### 서버에서 받은 누적 커피콩:", serverUsedBeans);

                //// 무료형 & 유료형 모두 화면에 커피콩사용 내역 업데이트
                // updateTokenDisplay(
                //     prompt_tokens, 
                //     completion_tokens, 
                //     total_tokens, 
                //     serverUsedBeans || 0
                // );

                // 유료형 챗봇의 경우만(KONG 타입만), 화면에 token사용량 표시
                if(chatbotType === "kong") {
                    updateTokenDisplay(
                        prompt_tokens, 
                        completion_tokens, 
                        total_tokens, 
                        serverUsedBeans || 0
                    );
                }                



            }

        })
        .catch(err => {
            addBotMessage("!!! !!! 서버와 통신 중 오류가 발생했습니다.");
            console.error(err);
            console.error("에러 메시지:", err.message);
        });
}


// 이벤트
sendBtn.addEventListener("click", sendMessage);

chatInput.addEventListener("keydown", e => {
    if (e.key === "Enter" && !e.shiftKey) {
        e.preventDefault();
        sendMessage();
    }
});


// 페이지 로드 시 초기화
window.onload = function() {
    scrollToBottom();
    
};


// basic vs. kong chatbot popup window
function openChatbotType() {
    const select = document.getElementById("chatbotType");
    const selectedValue = select.value;

    let url = "";
    let pWinName = "";

    if (selectedValue === "basic") {
        url = "/api/chatbot/freeboard/popupBasicChatbot";
        // pWinName = "chatbotBasic";
        pWinName = "chatbot";
    } else if (selectedValue === "kong") {
        url = "/api/chatbot/freeboard/popupKongChatbot";
        // pWinName ="chatbotKong";
        pWinName ="chatbot";
    }

    if (!url) return;

    // 부모(수정화면창) -> 자식 팝업(챗봇 basic 팝업창)으로 전역 변수 전달하기 위함
    //  자식 팝업(챗봇 basic 팝업창 -> 자식 팝업(챗봇 basic 팝업창)으로 전역 변수 전달하기 위함
    window.globalData = {
        boardNo: window.boardNo,
        loginMemberNo: window.loginMemberNo
        // more variables
    };

    window.open(
        url,
        //"helper", // 창이름 (같은이름의 창존재-> 기존 창 재사용, 없으면 새 창 생성)
        pWinName,
        "width=520,height=760"
    );
}
