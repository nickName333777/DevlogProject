

let followList = false;

document.addEventListener('DOMContentLoaded', () => {
    
    connectSocket();
    // 화면 입장 시 채팅방 목록 리스트 조회
    selectChatList();

    
    // 이벤트 바인딩
    bindChatContainerEvents();


    // 알리 클릭 해서 올 경우 
    // /devtalk>roomNo=123&targetMst=123 이런 식
    const params = new URLSearchParams(location.search); //요청 주소 쿼리스트링 부분 
    const roomNo = params.get("roomNo");
    const targetMsg = params.get("targetMsg");

    console.log(roomNo, targetMsg);

    // 방번호가 없다면 종료
    if (!roomNo) return;

    // 방 목록이 그려질 때까지 대기
    const interval = setInterval(() => {

        // roomNo를 가진 요소가 생겼는지 확인
        const roomEl = document.querySelector(`[data-room-no="${roomNo}"]`);

        // 생성되지 않았으ㅕㄴ 대기
        if (!roomEl) return;

        // 요소가 생성되었음 중단
        clearInterval(interval);


        // 채팅방 선택 함수
        enterChatRoom(roomNo);
        // 선택 효과 
        // ui
        // 채팅방 정보 로딩
        // stomp 구독
        // 읽음 처리


    }, 50);
});


function waitAndScrollToMessage(targetMsg) {

    const listInterval = setInterval(() => {
        const chatArea = document.querySelector(".message-list");
        if (!chatArea) return;

        clearInterval(listInterval);

        const observer = new MutationObserver(() => {
            const target = chatArea.querySelector(
                `.message-item[data-message-no="${targetMsg}"]`
            );
            if (target) {
                target.scrollIntoView({ behavior: "smooth", block: "center" });
                observer.disconnect();
            }
        });

        observer.observe(chatArea, { childList: true, subtree: true });
    }, 100);
}


async function selectChatList(query = null){

    try {
        const resp = await fetch('/devtalk/chatList?query='+query);
        const html = await resp.text();

        document.getElementById('roomList').outerHTML = html;

    } catch(e) {
        console.log('채팅방 목록 조회 실패', e)
    } 

}


// roomList 컨테이너에 한 번만 이벤트 걸기
document.addEventListener('click', (e) => {
    const item = e.target.closest('.room-item');
    if (!item) return; // room-item이 아닌 곳 클릭이면 무시

    const roomNo = Number(item.dataset.roomNo);


    enterChatRoom(roomNo);
});

// 검색된 메세지 클릭 시 해당 메세지로 이동
document.addEventListener('click', e => {
    const item = e.target.closest('.search-msg-item');
    if (!item) return;

    const messageNo = item.dataset.messageNo;
    moveToMessage(messageNo);
});


// window.addEventListener('load', () => {
//     
//     connectSocket();
// });



/* 메세지 하단 고정 함수 */
/* 채팅 영역 함수 */
function scrollToBottom() {
    const messageArea = document.getElementsByClassName('message-list')[0]
    if (!messageArea) return;
    messageArea.scrollTop = messageArea.scrollHeight
}

/* ============================================================================ */
/* 채팅방 추가 부분 */

/*채팅방 추가 버튼 클릭 시 */

const chatAddBtn = document.getElementById('chat-add-btn');
const createRoom = document.querySelector('.create-room');

chatAddBtn.addEventListener('click', () => {
    createRoom.classList.toggle('hide');
    userList.innerHTML = '';

    for (let check of radioCheck) {
            check.checked = false;
        }
    
    if(followList) return ;

    fetch('/devtalk/followSelect')
    .then(resp => resp.text())
    .then(html => {
        followList = true;

        document.getElementById('chatFollowList').outerHTML = html;

        followCheckbox()

    })
    .catch(e => console.log('팔로우 조회 실패', e))
    
});


/* 유저 선택 시 */

const userList = document.getElementsByClassName('select-user-list')[0]
const radioCheck = document.getElementsByName('invite')
/* 개인 그룹 선택 */
const private = document.querySelector('.private');
const group = document.querySelector('.group');
const roomNameARea = document.querySelector('.roomNameArea')

const roomNameArea = document.querySelector('.roomNameArea');
const roomImageArea = document.querySelector('.roomImageArea');

const imageInput = document.getElementById('roomImageInput');
const imagePreview = document.getElementById('roomImagePreview');


let chatType = 'private'

const followListContainer = document.getElementById('chatFollowList');

/* 개인 버튼 클릭 시 */
private.addEventListener('click', e=>{
    if(group.classList.contains('type-select')){
        group.classList.remove('type-select')
    }

    roomNameArea.classList.add('display-none');
    roomImageArea.classList.add('display-none');

    private.classList.add('type-select')
    roomNameARea.classList.add('display-none')

    chatType = 'private'
    userList.innerHTML = '';

    for (let check of radioCheck) {
            check.checked = false;
        }
})


/* 그룹 버튼 클릭 시 */
group.addEventListener('click', e=>{
    if(private.classList.contains('type-select')){
        private.classList.remove('type-select')
    }

    group.classList.add('type-select');

    roomNameArea.classList.remove('display-none');
    roomImageArea.classList.remove('display-none');
    
    roomNameARea.classList.remove('display-none');

    chatType = 'group';

    userList.innerHTML = '';

    for (let check of radioCheck) {
            check.checked = false;
        }
    


})




function followCheckbox() {
    
    for (let item of radioCheck) {
        
        
        
        item.addEventListener("change", e => {
    
            
    
            const followItem =  e.target.closest('.follow-item');
            const userName = followItem.querySelector('.name').innerText;
    
            if(chatType == 'private') {
                for (let check of radioCheck) {
                check.checked = false;
                }
    
                item.checked = true
                userList.innerHTML = '';
    
                addUser(userName, item);
            }
    
    
            if(chatType == 'group') {
                
                if(item.checked) {
    
                    if(!exist(userName)) {
                        addUser(userName, item);
                    }
                } else {
    
                    deleteUser(userName);
                }
    
    
            }
            
        })
    
    }    
}


/* 유저 추가 함수 */
function addUser(userName, checkbox) {

    const div = document.createElement('div');
    div.classList.add('user-item');

    const span = document.createElement('span');
    span.innerHTML = userName;

    const deleteBtn = document.createElement('span');
    deleteBtn.classList.add('list-delete-btn');
    deleteBtn.innerText = ' x';

    deleteBtn.addEventListener('click', () => {
    checkbox.checked = false;
    div.remove();
    });

    div.append(span, deleteBtn);
    userList.appendChild(div);
}



/* 유저 존재 하는지 */
function exist(userName) {
    const items = userList.getElementsByClassName('user-item');

    for (let item of items) {
        if (item.innerText.includes(userName)) {
            return true;
        }
    }
    return false;
}


/* 삭제 */
function deleteUser(userName) {
    const items = userList.getElementsByClassName('user-item');

    for (let item of items) {
        if(item.innerText.includes(userName)) {
            item.remove();
            return;
        }
    }
}


/* 생성버튼 클릭 시 */
document.getElementById('room-create-btn').addEventListener('click', async e => {

    if (chatType === 'private') {
        const result = await createPrivate();

        
        console.log(result); // 서버 응답 확인
        createRoom.classList.add('hide');

        await selectChatList();

        enterChatRoom(result);

        showChatRoomUI();

        await loadChatRoom(result);
        
    }

    if(chatType === 'group') {

        const result = await createGroup();

        console.log(result);

        createRoom.classList.add('hide');

        await selectChatList();

        enterChatRoom(result);


    }
    
});


/* 개인 채팅방 추가 함수 */
async function createPrivate(){
    
    try {

    
        const checked = document.querySelector('input[name="invite"]:checked');
        if (!checked) return alert('대화할 사용자를 선택하세요.');

        const targetMemberNo = Number(checked.dataset.memberNo);
        console.log(targetMemberNo)

        const resp =  await fetch("/devtalk/create/private",{
        method : "POST",
        headers: {'Content-Type' : 'application/json'},
        body : JSON.stringify(
            targetMemberNo)
        })

        const result = await resp.text();

        return result;

    } catch(e) {
        console.error(e);
        alert('채팅방 생성 실패');
    }
    
}

const deleteBtn = document.getElementById('image-delete-btn');
const defaultImage = imagePreview.src;

// 이미지 미리보기
imageInput.addEventListener('change', e => {
    const file = e.target.files[0];
    if (!file) return;

    // 확실하지 않음: 파일 용량 제한은 서버 정책에 따라 다름
    const reader = new FileReader();
    reader.onload = () => {
        imagePreview.src = reader.result;
    };
    reader.readAsDataURL(file);
});

deleteBtn.addEventListener('click', e => {
    e.preventDefault();
    e.stopPropagation();   // label 클릭 방지

    imagePreview.src = defaultImage;   // 기본 이미지 복원
    imageInput.value = "";             
});


/* 그룹 채팅방 추가 함수 */
async function  createGroup(){
    
    try{

        const checkedUsers = document.querySelectorAll('input[name="invite"]:checked');
    
        if (checkedUsers.length < 2) {
            alert('그룹 채팅은 최소 2명 이상 선택해야 합니다.');
            return;
        }
        
        const memberNos = [];
        for (let user of checkedUsers) {
    
            memberNos.push(Number(user.dataset.memberNo));
        }
    
        console.log(memberNos);
    
    
        const roomName = document.getElementById('roomName').value.trim();
        if (!roomName) {
            alert('채팅방 이름을 입력하세요.');
            return;
        }
    
        console.log(roomName);
    
        const img = imageInput.files[0];
    
        console.log(img);
    
        const formData = new FormData();
        formData.append('roomName', roomName);
    
        for (let memberNo of memberNos) {
            formData.append('memberNo', memberNo);
        }
    
        if(img){
            
            formData.append('roomImg', img);
        }
    
        const resp = await fetch('/devtalk/create/group', {
            method : "POST",
            body : formData
        });
    
        const roomNo = await resp.text();
    
        return roomNo;
    } catch(e) {
        console.error(e);
        alert('채팅방 생성 실패');
    }

}

/* 
    FormData
    - JavaScript의 내장 객체 웹폼의 데이터와 동일한 형식으로 key value 쌍을 쉽게 캡슐화하기 위해 설계
    - 파일 데이터를 포함하여 텍스트 데이터와 함께 서버로 전송할 수 있도록 데이터 표준화

    -FormData 객체는 append(키, 값) 메서드를 사용하여 필요한 모든 데이터를 추가
    - 같은 key 값으로 여러개 append 하면 배열처럼 쌓임 
    - 비동기 요청 보낼때 headers 따로 작성 x 자동으로 설정해줘서 
    - key 값하고 서버에서 받을 변수명 일치 시키면 편함
*/


// 해당 채팅방 이동 효과
function enterChatRoom(roomNo) {
    // 1. UI 선택 효과
    document
        .querySelectorAll('.room-item.is-selected')
        .forEach(el => el.classList.remove('is-selected'));

    const target = document.querySelector(`[data-room-no="${roomNo}"]`);
    if (!target) return;

    target.classList.add('is-selected');

    const unreadCountEl = target.querySelector('.unread-count');
    if(unreadCountEl) unreadCountEl.remove();

    // 2. 채팅 UI 생성
    showChatRoomUI();

    // 3. 채팅 정보 로딩
    loadChatRoom(roomNo);

    // 4. STOMP 구독
    subscribeRoom(roomNo);
    
    // 5. 채팅방 입장 시 마지막 읽은 메세지 업데이트
    sendReadSignal(roomNo);


}




document.getElementById('room-cancle-btn')?.addEventListener('click', e => {
    
    document.querySelector('.create-room').classList.add('hide')
})


/* ======================================================================================================== */



/* 메세지 수정 공감 삭제 드롭다운 */
/* 채팅 영역 함수 */
let openBox = null;




function bindMessageContextMenu() {

    const chatArea = document.getElementById('chattingArea');
    if (!chatArea) return;

    chatArea.addEventListener('contextmenu', e => {

        const box = e.target.closest('.bubble');
        if (!box) return;

        e.preventDefault();
        e.stopPropagation();

        const container = box.closest('.message-content');
        if (!container) return;

        const option = container.querySelector('.msg-option');
        const emojiArea = container.querySelector('.emoji-area');
        if (!option || !emojiArea) return;

        if (openBox === option) {
            option.classList.add('display-none');
            openBox = null;
            return;
        }

        if (openBox) {
            openBox.classList.add('display-none');
        }

        option.classList.toggle('display-none');
        openBox = option;

        const reactionBtn = option.querySelector('.msg-reaction-btn');
        if (reactionBtn) {
            reactionBtn.onclick = ev => {
                ev.stopPropagation();

                option.classList.add('display-none');
                openBox = null;

                emojiArea.classList.remove('display-none');
                emojiAreaClose(emojiArea);
            };
        }
    });
}






/* 이모지 영역 바깥을 클릭 했을 떄 닫히게 하는 함수 */
function emojiAreaClose(emojiArea) {

    if (emojiArea._outsideHandler) {
        document.removeEventListener('click', emojiArea._outsideHandler);
    }

    /* 문서 전체 클릭 감지해서 이모지 영역 밖을 클릭하면 이모지 영역 닫음 */
    function outsideClick(e) {

        /* 이모지 영역 본인 클릭 시 함수 종료 */
        if (emojiArea.contains(e.target)) return;

        emojiArea.classList.add('display-none');
        document.removeEventListener('click', outsideClick);
    }

    setTimeout(() => {
        document.addEventListener('click', outsideClick);
    });
}

/* 이모지 클릭 시 닫히게 하는 함수 */
// function emojiClickClose(emojiArea) {

//     const emojis = emojiArea.querySelectorAll('span');

//     for (let emoji of emojis) {
//         emoji.onclick = e => {
//             e.stopPropagation();

//             const emojiCode = emoji.dataset.emojiCode;
//             const messageNo = emoji.closest('.message-item').dataset.messageNo;

//             console.log(emojiCode, messageNo);

//             sendEmoji(Number(emojiCode), Number(messageNo));

            
//             emojiArea.classList.add('display-none');
//         };
//     }
// }

document.addEventListener("click", e => {

    const emoji = e.target.closest(".emoji-area span");
    if (!emoji) return;

    e.stopPropagation();

    const emojiCode = Number(emoji.dataset.emojiCode);
    const messageEl = emoji.closest(".message-item");
    if (!messageEl) return;

    const messageNo = Number(messageEl.dataset.messageNo);

    console.log(emojiCode, messageNo);

    sendEmoji(emojiCode, messageNo);

    const emojiArea = emoji.closest(".emoji-area");
    if (emojiArea) {
        emojiArea.classList.add("display-none");
    }
});


async function sendEmoji(emojiCode, messageNo) {

    if(!emojiCode || !messageNo) return ;

    const data = {
        emojiCode : emojiCode,
        messageNo : messageNo
    }

    const resp = await fetch('/devtalk/sendEmoji', {
                method : "POST",
                headers : {"Content-Type" : "application/json"},
                body : JSON.stringify(data)
    })

    if(!resp.ok) {
        console.log("이모지 전송 실패");
        return ;
    }

}


/* ===========================================
    채팅 UI 이벤트 바인딩
    fragment 로딩 후 반드시 호출
=========================================== */
function bindChatUIEvents() {

    /* ---------- 검색 / 메뉴 슬라이드 ---------- */

    const searchPanel  = document.querySelector('.chat-search-panel');
    const menuPanel    = document.querySelector('.chat-menu-panel');
    const searchBtn    = document.getElementById('text-search-btn');
    const chatMenuBtn  = document.getElementById('chat-menu-btn');

    if (searchBtn && searchPanel && menuPanel) {
        searchBtn.addEventListener('click', e => {
            e.stopPropagation();

            menuPanel.classList.remove('is-open');
            searchPanel.classList.toggle('is-open');
        });
    }

    if (chatMenuBtn && searchPanel && menuPanel) {
        chatMenuBtn.addEventListener('click', e => {
            e.stopPropagation();

            searchPanel.classList.remove('is-open');
            menuPanel.classList.toggle('is-open');
        });
    }

    /* ---------- 채팅방 나가기 ---------- */

    const exitBtn     = document.getElementById('exit-btn');
    const exitArea    = document.querySelector('.exit-check');
    const chatOverlay = document.getElementById('chat-overlay');
    const noBtn       = document.getElementById('no');
    const yesBtn      = document.getElementById('yes');

    if (exitBtn && exitArea && chatOverlay) {
        exitBtn.addEventListener('click', () => {
            exitArea.classList.remove('display-none');
            chatOverlay.classList.add('active');
        });
    }

    if (noBtn && exitArea && chatOverlay) {
        noBtn.addEventListener('click', () => {
            exitArea.classList.add('display-none');
            chatOverlay.classList.remove('active');
        });
    }

    if (yesBtn && exitArea && chatOverlay) {
        yesBtn.addEventListener('click', async () => {
            exitArea.classList.add('display-none');
            chatOverlay.classList.remove('active');

            try{

                
                const resp = await fetch('/devtalk/roomExit?roomNo=' + Number(currentRoomNo))

                if(!resp.ok) return ;
                    
                if(stompClient){
                    
                    stompClient.send("/devtalk/chat.leave", {}, JSON.stringify({
                        room_no : currentRoomNo ,
                        member_no : myNo}));
                }

                setTimeout(location.reload(), 200);
            } catch(e) {
                console.error(e)
            }


            


        });
    }



    // -----------------------------------------------
    // 메세지 검색
    const chatMsgBtn = document.getElementById("search-message");
    const msgInput = document.getElementById("msgSearch");

    msgInput.addEventListener("keydown", async e=> {

        if(e.key == "Enter"){

            const query = msgInput.value.trim();
    
            if (!query) return;
            
            console.log(query);
            const data = {
                roomNo : currentRoomNo,
                query : query
    
            }
    
            const resp = await fetch("/devtalk/searchMessageList", {
                            method : "POST",
                            headers : {"Content-Type" : "application/json"},
                            body : JSON.stringify(data)
            })
            
            const html = await resp.text();

            document.getElementById("searchMsgArea").outerHTML = html;


            msgInput.value=''
        }

    })

}


function moveToMessage(messageNo){

    const target = document.querySelector(`.message-item[data-message-no="${messageNo}"]`)

    target.scrollIntoView({
        behavior : "smooth",
        block : "center"
    })


    document.querySelector('.chat-search-panel').classList.remove('is-open');
    document.querySelector('.search-msg-area').innerHTML = '';

}



/* ------------------------------------------- */
let currentEditMessageNo = null;
let currentEditRoomNo = null;

/* 메세지 수정 함수 */
function bindMessageEditEvents() {

    const sendArea = document.querySelector('.send-area');
    const editArea = document.querySelector('.edit-area');
    const editCancelBtn = document.getElementById('edit-cancle-btn');
    const chatArea = document.getElementById('chattingArea');
    const editTextarea = document.getElementById("edit-message");
    const editBtn = document.getElementById('edit-btn');

    if (!sendArea || !editArea || !chatArea) return;

    chatArea.addEventListener('click', e => {

        const editBtn = e.target.closest('.msg-edit-btn');
        if (!editBtn) return;

        const messageContainer = editBtn.closest('.message-item');
        if (!messageContainer) return;

        const bubble = messageContainer.querySelector('.bubble');
        if (!bubble) return;

        const msgContent = messageContainer.querySelector('.msg-content');

        const originText = msgContent.innerText;

        currentEditMessageNo = messageContainer.dataset.messageNo;
        currentEditRoomNo = currentRoomNo;

        openEditMode(originText, sendArea, editArea);

        const opt = editBtn.closest('.msg-option');
        if (opt) opt.classList.add('display-none');
    });

    if (editCancelBtn) {
        editCancelBtn.addEventListener('click', () => {
            closeEditMode(sendArea, editArea);
        });
    }


    // 메세지 클릭 or 엔터 입력 시 제출
    editBtn.addEventListener('click', e=> {
        submitEdit();
    })

    editTextarea.addEventListener('keydown', e => {
        
        if(e.key ==='Enter') submitEdit();
    })
}


async function submitEdit() {

    console.log("메세지 수정 함수 호출 확인 ");

    if (!currentEditMessageNo) return;
    
    const newText = document.getElementById('edit-message').value.trim();

    if(!newText) return;

    console.log(newText);
    console.log(currentEditMessageNo    );
    const data = {
        message_no : currentEditMessageNo,
        content : newText
    }
    const resp = await fetch('/devtalk/message/edit', {
                    method:'POST',
                    headers: {'Content-Type' : 'application/json'},
                    body : JSON.stringify(data)
                });
            
    if(!resp.ok) {
        console.log('메세지 수정 실패 ');
    }
    
    newText.innerHTML = '';

    closeEditMode(
        document.querySelector('.send-area'),
        document.querySelector('.edit-area')
    )

}



function openEditMode(originText, sendArea, editArea) {
    sendArea.classList.add('display-none');
    editArea.classList.remove('display-none');

    const input = document.getElementById('edit-message');
    if (!input) return;

    input.value = originText;
    input.focus();
}

function closeEditMode(sendArea, editArea) {
    editArea.classList.add('display-none');
    sendArea.classList.remove('display-none');

    const input = document.getElementById('edit-message');
    if (input) input.value = '';
}

//================================================================
// 메세지 신고 

function bindMessageReportEvent() {
    const chatArea = document.getElementById('chattingArea');
    if (!chatArea) return;


    chatArea.addEventListener('click', e=> {

        const btn = e.target.closest('.msg-report-btn');
        if (!btn) return;

        const li = btn.closest('.message-item');
        if (!li) return;

        const option = btn.closest('.msg-option');

        const targetMemberNo = li.dataset.memberNo;
        const targetNo = li.dataset.messageNo;

        openReportModal(targetMemberNo, targetNo);


        option.classList.add('display-none');
    })

}



function bindInviteEvents() {

    const inviteBtn = document.getElementById('invite-btn');
    const inviteList = document.getElementsByName('roomInvite');
    const selectedArea = document.querySelector('.select-user-area');
    const chatOverlay = document.getElementById('chat-overlay');

    inviteBtn?.addEventListener('click', () => {
        for (let item of inviteList) item.checked = false;
        selectedArea.innerHTML = '';
        chatOverlay.classList.add('active');
        document.querySelector('.user-invite-box')?.classList.remove('display-none');

        /* 초대할 유저 목록 조회 */
        fetch("/devtalk/followSelect?roomNo="+currentRoomNo)
        .then(resp => resp.text())
        .then(html => {
            console.log("유저 목록 조회 성공");
            document.getElementById('inviteUserList').outerHTML = html;
            
            chatRoomInvite();
        })
        .catch(e => console.log(e))
    });

    

    function chatRoomInvite() {
        for (let checkbox of inviteList) {
            checkbox.addEventListener('change', e => {
                const listBox = e.target.closest('.user-list');
                const nameEl =
                    listBox.querySelector('.user-name') ||
                    listBox.querySelector('span');
    
                const userName = nameEl.innerText;
    
                if (e.target.checked) {
                    if (!inviteExist(userName)) {
                        inviteAddUser(userName, e.target);
                    }
                } else {
                    inviteDeleteUser(userName);
                }
            });
        }
    
        document.getElementById('invite-cancel')
            ?.addEventListener('click', () => {
                document.querySelector('.user-invite-box')
                    ?.classList.add('display-none');
                chatOverlay.classList.remove('active');
            });
    
        /* 초대 버튼 클릭 시  */
        document.getElementById('invite-user')?.addEventListener('click', e => {
            document.getElementsByClassName('user-invite-box')[0].classList.add('display-none');
            chatOverlay.classList.remove('active');
            
            const checkedUsers = document.querySelectorAll('input[name="roomInvite"]:checked');
            const memberNos = [];
            for (let user of checkedUsers) {
    
            memberNos.push(Number(user.dataset.memberNo));
            }
            
            const data = {
                room_no : currentRoomNo,
                member_no : memberNos
            }

            /* 비동기 요청 ------------------------------------ */
            fetch('/devtalk/inviteChat', {
                method : "POST",
                headers : {'Content-Type' : 'application/json'},
                body : JSON.stringify(data)
            })
            .then(resp => {
                if(resp.ok) {
                    loadChatRoom(currentRoomNo);
                }
            })
            .catch(e => console.log('회원 초대 실패', e))

    

        
            alert("초대 되었습니다 ! ");
        })
    
    
    
    
        function inviteAddUser(userName, checkbox) {
            const div = document.createElement('div');
            div.classList.add('user-item');
    
            const span = document.createElement('span');
            span.innerText = userName;
    
            const deleteBtn = document.createElement('span');
            deleteBtn.classList.add('list-delete-btn');
            deleteBtn.innerText = ' x';
    
            deleteBtn.addEventListener('click', () => {
                checkbox.checked = false;
                div.remove();
            });
    
            div.append(span, deleteBtn);
            selectedArea.appendChild(div);
        }
    
        /* 존재하면 false 반환 */
        function inviteExist(userName) {
            const items = selectedArea.getElementsByClassName('user-item');
            for (let item of items) {
                if (item.innerText.includes(userName)) {
                    return true;
                }
            }
            return false;
        }
    
        /* 유저 삭제 */
        function inviteDeleteUser(userName) {
            const items = selectedArea.getElementsByClassName('user-item');
            for (let item of items) {
                if (item.innerText.includes(userName)) {
                    item.remove();
                    return;
                }
            }
        }
    }
    
}





/* 방 이름 수정 버튼 클릭 시 */
function bindTeamNameEditEvent() {

    /* 방 이름 수정 버튼 */
    const editBtn = document.getElementById('team-name-change');
    const teamNameSpan = document.querySelector('.team-name');
    const panelTitle = document.querySelector('.member-panel-title');

    if (!editBtn || !teamNameSpan || !panelTitle) return;

    editBtn.addEventListener('click', () => {

        /* 이미 input 상태면 중복 생성 방지 */
        if (panelTitle.querySelector('input')) return;

        const currentName = teamNameSpan.innerText;

        /* 기존 span 숨김 */
        teamNameSpan.classList.add('display-none');

        /* input 생성 */
        const input = document.createElement('input');
        input.type = 'text';
        input.classList.add('team-name-input');
        input.name = 'teamNameInput'
        input.value = currentName;

        /*
            insertBefore(새요소, 기준요소)
            → editBtn 앞에 input 삽입
        */
        panelTitle.insertBefore(input, editBtn);
        input.focus();

        /* 편집 완료 처리 함수 */
        const finishEdit = async () => {

            const newName = input.value.trim();

            if (newName) {
                teamNameSpan.innerText = newName;
            }

            const resp = await fetch("/devtalk/roomName", {
                method : "POST",
                headers : {"Content-Type" : "application/json"},
                body : JSON.stringify({
                    newName : newName,
                    roomNo : currentRoomNo
                })
            });

            if(!resp.ok) {
                console.error("에러 발생");
                alert("서버 에러 발생");
                return;
            }
            

            input.remove();
            teamNameSpan.classList.remove('display-none');
        };

        /* Enter 키 입력 시 완료 */
        input.addEventListener('keydown', e => {
            if (e.key === 'Enter') {
                finishEdit();
            }
        });

        /* 포커스 해제 시 완료 */
        input.addEventListener('blur', finishEdit);
    });
}


// /* 고정 핀 클릭 시 */
// const pinnedBtn = document.getElementById('pinned-btn');
// const unpinnedBtn = document.getElementById('unpinned-btn');
// 
// 
// /* 각각 db 상태값 변경 후 채팅방 목록 재정렬 해야함*/
// pinnedBtn.addEventListener('click', e => {
//     pinChange()
// })
// 
// unpinnedBtn.addEventListener('click', e => {
//     pinChange()
// })
// 
// function pinChange() {
//     pinnedBtn.classList.toggle('display-none')
//     unpinnedBtn.classList.toggle('display-none')
// }


document.addEventListener("click", async e=> {

    const pin = e.target.closest('#unpinned-btn');
    if (!pin) return;

    const data = {
        memberNo : Number(myNo),
        roomNo : Number(currentRoomNo)
    };

    const resp = await fetch('/devtalk/pinUpdate', {
        method : "POST",
        headers : { 'Content-Type' : 'application/json' },
        body : JSON.stringify(data)
    });

    if (resp.ok) {
        await selectChatList();

        const listItem = document.querySelector(
            `[data-room-no="${currentRoomNo}"]`
        );

        listItem?.classList.add('is-selected');
    }
})


// 메세지 삭제 이벤트
function bindMessageDeleteEvents() {

    const chatArea = document.getElementById('chattingArea');
    const delCheck = document.querySelector('.del-check');
    const msgDelYes = document.getElementById("msg-del-yes");
    const msgDelNo = document.getElementById("msg-del-no");
    const chatOverlay = document.getElementById('chat-overlay');

    if (!chatArea || !delCheck || !msgDelYes || !msgDelNo) return;

    let targetMessageNo = null;

    // 삭제 버튼 클릭 (이벤트 위임)
    chatArea.addEventListener('click', e => {

        const delBtn = e.target.closest('.msg-delete-btn');
        if (!delBtn) return;

        const opt = delBtn.closest('.msg-option');
        opt?.classList.add('display-none');

        const msgItem = delBtn.closest('.message-item');
        targetMessageNo = msgItem?.dataset.messageNo;

        delCheck.classList.remove('display-none');
        chatOverlay.classList.add('active');
    });

    // 삭제 확인 - 예
    msgDelYes.onclick = () => {

        if (!targetMessageNo) return;

        fetch("/devtalk/delete-msg?messageNo="+targetMessageNo)
        .then(resp => {
            if(!resp.ok) {
                throw new Error("Msg Delete Error");
            }
        })
        .catch(e => console.log(e))

        delCheck.classList.add('display-none');
        chatOverlay.classList.remove('active');

        targetMessageNo = null;
    };

    // 삭제 확인 - 아니오
    msgDelNo.onclick = () => {
        delCheck.classList.add('display-none');
        chatOverlay.classList.remove('active');
        targetMessageNo = null;
    };
}

let profileCardBound = false;

function bindProfileCardEvents() {

    if(profileCardBound) return;
    profileCardBound = true;

    document.addEventListener('click', async e => {

        const img = e.target.closest('.profile-img');
        if(!img) return;

        e.stopPropagation();

        const messageItem = img.closest('.message-item');
        const profileCard = messageItem.querySelector('.profile-card');
        const memberNo = messageItem.dataset.memberNo;

        document.querySelectorAll('.profile-card').forEach(card => {
            if(card !== profileCard) card.classList.add('display-none');
        });

        if(profileCard.classList.contains('display-none')) {
            const resp = await fetch(`/member/profile?memberNo=${memberNo}`);
            const data = await resp.json();
            openProfile(data, profileCard);
        }

        profileCard.classList.toggle('display-none');
    });

    document.addEventListener('click', () => {
        document.querySelectorAll('.profile-card')
            .forEach(card => card.classList.add('display-none'));
    });

    document.addEventListener('click', e => {
        if(e.target.closest('.profile-card')) e.stopPropagation();
    });
}




function openProfile(data, card) {
    card.innerHTML = `
        <div class="flex-center">
            <img class="profile-card-img" src="${data.profile_img}">
        </div>

        <div class="profile-info">
            <span class="name">${data.member_nickname}</span>
            <span class="level">LV${data.level}</span>
        </div>

        <p class="job-title">${data.level_title}</p>

        <div class="button-group">
            <button onclick="linkProfile('${data.email}')" class="btn fw-600 fs-14">프로필 보기</button>
            <button class="btn fw-600 fs-14 start-private-chat" 
                    data-profile-no="${data.member_no}">
                1:1 채팅 시작
            </button>
        </div>
    `;
}

function linkProfile(email) {

    location.href = `/blog/${email}`


}

function bindChatContainerEvents(){
    const container = document.getElementById('chatting-space');

    container.addEventListener('click', async e => {

        const btn = e.target.closest('.start-private-chat');
        if(!btn) return;

        e.stopPropagation();

        const memberNo = Number(btn.dataset.profileNo);
        const roomNo = await createPrivateWith(memberNo);

        await selectChatList();

        enterChatRoom(roomNo);

        showChatRoomUI();

        await loadChatRoom(roomNo)
    });
}


// 1 ㄷ 1 채팅 연결
async function createPrivateWith(targetMemberNo){
    try {
        const resp = await fetch("/devtalk/create/private", {
            method : "POST",
            headers: {'Content-Type' : 'application/json'},
            body : JSON.stringify(targetMemberNo)
        });

        return await resp.text();

    } catch(e){
        console.error(e);
        alert('채팅방 생성 실패');
    }
}


// 채팅방내 이미지 클릭 시 가운데에 띄우기
function imagebigViewer() {


    const viewer = document.getElementById('imageViewer');
    const viewerImg = document.getElementById('imageViewerImg');

    document.addEventListener('click', e => {
        const img = e.target.closest('.bubble.image img');
        if (!img) return;

        e.stopPropagation();
        viewerImg.src = img.src;
        viewer.classList.remove('display-none');
    });

    viewer.addEventListener('click', () => {
        viewer.classList.add('display-none');
        viewerImg.src = '';
    });

    document.addEventListener('keydown', e => {
        if (e.key === 'Escape') {
            viewer.classList.add('display-none');
            viewerImg.src = '';
        }
    });
}




/* =========================================================== */
/* 채팅방 정보 조회 이름 메세지 목록 회원 등등  */

async function loadChatRoom(roomNo) {

    const resp = await fetch('/devtalk/roomInfoLoad?roomNo='+ roomNo)

    if (!resp.ok) {
        console.error('채팅방 로드 실패');
        return;
    }

    const html = await resp.text(); 

    const chattingArea = document.querySelector('#chatting-space');
    chattingArea.innerHTML  = html;

    afterFuncLoad();
    console.log("roomNo : ",roomNo)


    bindChatSendInputEvents(roomNo);


    // scrollToBottom();
    

    


}

// 함수 재바인딩
function afterFuncLoad(){
    bindMessageEditEvents();
    bindMessageContextMenu();
    bindTeamNameEditEvent();
    bindMessageDeleteEvents();
    bindChatUIEvents();
    bindInviteEvents();
    bindSendImage();
    imagebigViewer();
    bindMessageReportEvent();
    bindProfileCardEvents();
    bindMentionInput();
    bindTypingEvent();

    requestAnimationFrame(() => {
        requestAnimationFrame(() => {
            lockScrollToBottom();
        });
    });
};

function lockScrollToBottom() {
    const list = document.querySelector(".message-list");
    if (!list) return;

    let lastHeight = -1;
    let stableCount = 0;

    function stabilize() {
        const currentHeight = list.scrollHeight;

        if (currentHeight === lastHeight) {
            stableCount++;
        } else {
            stableCount = 0;
            lastHeight = currentHeight;
        }

        list.scrollTop = currentHeight;

        if (stableCount < 6) {
            requestAnimationFrame(stabilize);
        }
    }

    stabilize();
}





function showChatRoomUI() {
    document.querySelector('.room-empty')?.classList.add('display-none');
    document.querySelector('.room-exist')?.classList.remove('display-none');
}


let stompClient = null;


// 웹소켓 + STOMP 연결
function connectSocket(){

    // 서버 WebSocket 엔드포인트(/ws-chat)로 연결 생성
    const socket = new SockJS('/ws-chat');

    // webSocket 위에 STOMP 프로토콜을 올려서 메세지 통신 구조 생성
    // websocket위에 STOMP  규칙을 얹어서 메세지 교환 규칙을 만듦
    stompClient= Stomp.over(socket);


    // STOMP 연셜 요청
    // 연결 성공 시 console창
    stompClient.connect({}, () => {
        console.log('STOMP connected');


        stompClient.subscribe(
            '/topic/chat-list/' + myNo,
            onChatListUpdate
        );
    })



}



// 현재 구독중인 채팅방 관리 변수
let currentSubscription = null;
let currentRoomNo = null;

// 채팅방 입장시 해당 채팅방 구독
function subscribeRoom(roomNo) {

    // 이전방 퇴장
    if(currentRoomNo !== null) {
        const data = {
            room_no : currentRoomNo,
            member_no : myNo

        }
        stompClient.send(
            '/devtalk/chat.leave',
            {},
            JSON.stringify(data)
        );
    }

    // 이미 다른 채팅방을 보고 있다면
    // 이전 채팅방 구독 해제
    if (currentSubscription) {

        currentSubscription.unsubscribe();
    }

    // 선택한 채팅방의 topic을 구독
    // topic: 여러 클라이언트가 동시에 구독하는 브로드캐스트 메시지 채널 (1:N)
    // queue: 특정 사용자에게 전달되는 1:1 메시지 채널
    // 이 시점부터 해당 채팅방(topic)으로 발행되는 모든 메시지를 실시간으로 수신
    currentSubscription = stompClient.subscribe(
        '/topic/room/' + roomNo, // 채팅방 고유 메세지 채널
        onMessageReceived // 이 채팅방으로 들어오는 모든 메세지 수신 처리기
    );

    enterRoomCount(roomNo);

    currentRoomNo = roomNo;




    

    console.log('subscribed to room:', roomNo);
    console.log('subscription object:', currentSubscription);
}


function enterRoomCount(roomNo) {

    console.log('roomNo : ', roomNo);

    const data = {
        room_no : roomNo,
        member_no : myNo

    }
    stompClient.send(
        '/devtalk/chat.enter',{},
        JSON.stringify(data)
    )
}


// 채팅방 리스트 정보 최신화
async function onChatListUpdate(payload) {
    
    const updateInfo = JSON.parse(payload.body);
    console.log("채팅방 리스트 업데이트용 : " + payload.body);

    await selectChatList();



    const listItem = document.querySelector(`[data-room-no="${currentRoomNo}"]`)


    listItem?.classList.add('is-selected')


}

// 채팅방 상단 이동 함수
function moveTop(roomNo) {

    const list = document.querySelector('.room-list');
    const item = list.querySelector(`[data-room-no="${roomNo}"]`);


    if(!item) return;


    list.prepend(item);

}


// 메세지 전송 함수
function sendMessage(chatRoomNo, content) {
    const totalMember = document.querySelector('.member-counting').innerText;
    const msg = {
        chatRoomNo : chatRoomNo,
        sender : myNo,
        content : content,
        total_count : Number(totalMember)
    };

    console.log("msg : ", msg);

    stompClient.send('/devtalk/chat.send', {}, JSON.stringify(msg));
    
    // 메세지 전송 시 타이핑 false
    sendTyping(false);
    isTyping = false;
    
}


// 채팅 전송 이벤트

function bindChatSendInputEvents(chatRoomNo) {
    const textArea = document.getElementById("send-message");
    const sendBtn = document.getElementById("send-btn");

    if(!textArea || !sendBtn) return;

    sendBtn.addEventListener('click', e => {
        const content = textArea.value.trim();
        if(!content) return;

        sendMessage(chatRoomNo, content);
        textArea.value = '';
    })

    
    textArea.addEventListener('keydown', e => {
        if(e.key === 'Enter'){
            if(!e.shiftKey) {
                e.preventDefault();
                sendBtn.click();
                textArea.value = '';
            }
        }
    })
}

function updateUserCount(msg) {
    console.log('확인');
    
    const memberCountSpan = document.querySelector('.member-counting');

    memberCountSpan.innerText = msg.count;
}



// 메세지 수신기
function onMessageReceived(payload) {
    const msg = JSON.parse(payload.body);

    if(msg.type == 'READ') {
        console.log('LastReadNo raw:', msg.LastReadNo);
        console.log('LastReadNo num:', Number(msg.LastReadNo));

        updateUnreadChange(msg);
        return;
    }
    if(msg.type =='LEAVE') {
        console.log(msg ,'확인');
        updateUserCount(msg);
        return;
    }
    if(msg.status == 'MOD' || msg.status == 'DEL'){

        applyMessageStatus(msg);

    } else{

        if(msg.type == 'Typing') {
            handleTyping(msg);

            return;
        }

        if(msg.type == 'SYSTEM'){

            addSystemMessage(msg);

            return;
        }

        if(msg.type == 'Emoji'){
            updateEmojiUI(msg);

            return ;
        }
        appendMessage(msg);
        sendReadSignal(msg.room_no);
    }
}

const typingMembers = new Map(); 

function handleTyping(msg) {
    if (msg.memberNo === myNo) return;

    const typingBox = document.querySelector(".typing-box");
    if (!typingBox) return;

    if (msg.typing) {
        typingMembers.set(msg.memberNo, msg.memberNickname);
    } else {
        typingMembers.delete(msg.memberNo);
    }

    if (typingMembers.size === 0) {
        typingBox.classList.add("display-none");
        return;
    }

    const names = [...typingMembers.values()];
    typingBox.innerText = names.join(", ") + " 입력중...";
    typingBox.classList.remove("display-none");
}



function appendMessage(msg) {
    const isMine = msg.sender_no === myNo;
    const el = isMine ? createMyMessage(msg) : createOtherMessage(msg);

    const area = document.querySelector('.message-list');
    area.appendChild(el);
    area.scrollTop = area.scrollHeight;
}


function createLiBase(className, msg) {
    const li = document.createElement('li');
    li.className = `message-item flex gap-12 ${className}`;
    li.dataset.memberNo = msg.sender_no;
    li.dataset.messageNo = msg.message_no;
    return li;
}



function applyMessageStatus(msg){
    console.log('여기까지 오기는 하는거니 ? ');

    const li = document.querySelector(`[data-message-no="${msg.message_no}"]`);

    const bubble = li.querySelector('.bubble');

    
    if(msg.status == 'MOD') {

        const msgContent = bubble.querySelector('.msg-content');
        msgContent.innerText = msg.content

        const edited = bubble.querySelector('.edited');
        if(edited) return;


        const span = document.createElement('span');
        span.className = 'edited fs-12';
        span.innerText = '(수정됨)';

        bubble.append(span);
    }

    if(msg.status == 'DEL') {

        bubble.innerHTML = ""


        bubble.className = 'bubble deleted';
        
        const span = document.createElement('span');

        span.innerText = '삭제된 메세지입니다.';

        li.querySelectorAll('.msg-option, .reaction-badge')
            .forEach(el => el.style.display = 'none');

        bubble.append(span);


        


    }

}

// 이모지 업데이트 함수
function updateEmojiUI(msg) {

    const li = document.querySelector(`[data-message-no="${msg.message_no}"]`)

    if(!li) return ;

    const bubble = li.querySelector('.bubble');

    let badge = li.querySelector('.reaction-badge');

    if(badge) badge.remove();

    badge = document.createElement('div');
    badge.className = 'reaction-badge';

    for(const [emoji, count] of Object.entries( msg.reactions)) {
        const span = document.createElement('span');
        span.className = 'flex-center gap-4';

        const e = document.createElement('span');
        e.innerText = emoji;

        const c = document.createElement('span');

        c.innerText = count;

        span.append(e, c);

        badge.append(span);

    }

    bubble.after(badge);


}

// 시스템 메세지 추가함수
function addSystemMessage(msg) {
    const area = document.querySelector('.message-list');
    const li = document.createElement('li');
    
    li.className = 'message-item flex gap-12 my';
    li.dataset.messageNo = msg.message_no;

    const div = document.createElement('div');
    div.className = 'system-message flex-center';

    const span = document.createElement('span');
    span.innerText = msg.content

    div.appendChild(span);

    li.appendChild(div);

    area.appendChild(div);

    area.scrollTop = area.scrollHeight;

}

/* 내 메세지 */
function createMyMessage(msg) {

    const li = document.createElement('li');
    li.className = 'message-item flex gap-12 my';
    li.dataset.messageNo = msg.message_no;

    const content = document.createElement('div');
    content.className = 'message-content flex-col gap-12';

    // bubble
    const bubble = document.createElement('div');
    if(msg.type == 'TEXT') {
        bubble.className = 'bubble';
        const msgSpan = document.createElement('span');
        msgSpan.className = 'msg-content'
        msgSpan.innerText = msg.content;
        bubble.appendChild(msgSpan);

    } else if(msg.type =='IMG') {
        bubble.className = 'bubble image';
        const img = document.createElement('img');
        img.src = msg.img_path;

        img.onload = () => {
        scrollToBottom();
    };

        bubble.appendChild(img);
    }


    // reaction badge
    const reaction = document.createElement('div');
    reaction.className = 'reaction-badge';

    // options
    const option = document.createElement('ul');
    option.className = 'list-none msg-option display-none';

    option.innerHTML = `
        <li class="msg-edit-btn">수정하기</li>
        <li class="msg-delete-btn">삭제하기</li>
        <li class="msg-reaction-btn">공감하기</li>
    `;

    // emoji area
    const emoji = document.createElement('div');
    emoji.className = 'emoji-area flex display-none';
        emoji.innerHTML = `
        <span data-emoji-code="1">❤️</span><span data-emoji-code="2">👍</span><span data-emoji-code="3">😀</span>
        <span data-emoji-code="4">😂</span><span data-emoji-code="5">😮</span><span data-emoji-code="6">😡</span>
    `;

    // 안 읽은 사람 수
    
    let unreadCount = null;
    if(msg.unread_count > 0) {

        unreadCount = document.createElement('span');
        unreadCount.className = 'unread-people fs-12'
        unreadCount.innerText = msg.unread_count;

    }


    // time 
    const time = document.createElement('span');
    time.className = 'fs-12 send-time';
    time.innerText = formatTime(msg.sendtime);


    if (unreadCount) {
    content.append(bubble, reaction, option, emoji, unreadCount, time);
    } else {
        content.append(bubble, reaction, option, emoji, time);
    }
    li.appendChild(content);

    return li;
}


/* 쟤 메세지 */
function createOtherMessage(msg) {

    const li = createLiBase('other', msg);

    // 프로필 이미지
    const img = document.createElement('img');
    img.className = 'profile-img';
    img.src = msg.profile_img ?? '/images/user.png';
    li.appendChild(img);

    const cardDiv = document.createElement('div');
    cardDiv.className = 'profile-card flex-col display-none'
    li.appendChild(cardDiv);

    const content = document.createElement('div');
    content.className = 'message-content flex-col gap-12';

    // 이름
    const name = document.createElement('span');
    name.className = 'fw-600';
    name.innerText = msg.sender_name;

    // bubble
    const bubble = document.createElement('div');
    if(msg.type == 'TEXT') {
    bubble.className = 'bubble';
    const msgSpan = document.createElement('span');
    msgSpan.className = 'msg-content'
    msgSpan.innerText = msg.content;
    bubble.appendChild(msgSpan);
        
    } else if(msg.type =='IMG') {
        bubble.className = 'bubble image';
        const img = document.createElement('img');
        img.src = msg.img_path;

        img.onload = () => {
        scrollToBottom();
    };
        bubble.appendChild(img);
    }

    // reaction badge
    const reaction = document.createElement('div');
    reaction.className = 'reaction-badge';

    // options
    const option = document.createElement('ul');
    option.className = 'list-none msg-option display-none';
    option.innerHTML = `
        <li class="msg-reaction-btn">공감하기</li>
        <li class="msg-report-btn">신고하기</li>
    `;

    // emoji area
    const emoji = document.createElement('div');
    emoji.className = 'emoji-area flex display-none';
    emoji.innerHTML = `
        <span data-emoji-code="1">❤️</span><span data-emoji-code="2">👍</span><span data-emoji-code="3">😀</span>
        <span data-emoji-code="4">😂</span><span data-emoji-code="5">😮</span><span data-emoji-code="6">😡</span>
    `;

    let unreadCount = null;
    if(msg.unread_count > 0) {

        unreadCount = document.createElement('span');
        unreadCount.className = 'unread-people fs-12'
        unreadCount.innerText = msg.unread_count;

    }

    const time = document.createElement('span');
    time.className = 'fs-12 send-time';
    time.innerText = formatTime(msg.sendtime);

    if (unreadCount) {
    content.append(name, bubble, reaction, option, emoji, unreadCount, time);
    } else {
        content.append(name, bubble, reaction, option, emoji, time);
    }
    li.appendChild(content);

    return li;
}


// 채팅방 유저 들어올 시 메세지 카운트 업데이트
function updateUnreadChange(msg) {
    console.log("여기 오긴 하니 ? ? ");

    if((msg.LastReadNo == msg.roomLastReadNo) ||(msg.memberNo == myNo) ) return;


    const interval = setInterval(() => {
        
        console.log("이건 실행 되니 ?");

        const targets = getMessagesAfter(Number(msg.LastReadNo));
        // roomNo를 가진 요소가 생겼는지 확인

        console.log("여기는?  ? 2051")
        console.log(targets.length);
        // 생성되지 않았으ㅕㄴ 대기
        if (targets.length === 0) return;

        // 요소가 생성되었음 중단
        clearInterval(interval);

        console.log('타겟들 확인 : ', targets);


        for (const target of targets) {
            const unread = target.querySelector('.unread-people');
            if (!unread) continue;
    
            const prev = Number(unread.innerText) || 0;
            const next = Math.max(prev - 1, 0);
    
            unread.innerText = next === 0 ? '' : next;
        }

    }, 100);
    
}


function getMessagesAfter(baseNo) {
    return [...document.querySelectorAll('.message-item')]
        .filter(el => Number(el.dataset.messageNo) > baseNo);
}






// 시간 변환 함수
function formatTime(timeStr) {
    if (!timeStr) return '';

    const safe = timeStr.replace(/\.(\d{3})\d*/, '.$1');

    const d = new Date(safe);

    if (isNaN(d)) {
        console.error('Invalid Date:', timeStr);
        return '';
    }

    const hh = String(d.getHours()).padStart(2, '0');
    const mm = String(d.getMinutes()).padStart(2, '0');

    return `${hh}:${mm}`;
}



/* 채팅방 입장 or 채팅방 참여 시 마지막 읽은 메세지 업데이트 */
function sendReadSignal(roomNo) {

    if (!stompClient || !stompClient.connected) {
        console.warn('stomp 연결 x');
        return;
    }

    const payload = {
        room_no: roomNo,
        member_no : myNo
    };

    console.log(payload.room_no);
    console.log(payload.member_no);

    stompClient.send(
        '/devtalk/chat.read',
        {},
        JSON.stringify(payload)
        
    );

}


/* ------------------------------------------- */
/* 채팅방 검색 */
const searchChat = document.getElementById("chatting-search-area");
    

searchChat.addEventListener("keydown", e => {

    if (e.key === 'Enter') {
        const keyword = searchChat.value;

       // if (!keyword) return;   // 비어있으면 종료

        console.log("검색어:", keyword);
        selectChatList(keyword);
    }

}); 


/* ------------------------------------------------- */
/* 이미지 전송 */

function bindSendImage(){
    const imgInput = document.getElementById('chatImg');
    const imgDiv = document.querySelector('.send-image');
    const previewArea = document.getElementById('send-image-preview');
    const textarea = document.getElementById('send-message');
    const imgDelete = document.getElementById('send-image-delete');
    const imgSendBtn = document.getElementById('send-btn');
    const MAX = 3 * 1024 * 1024;
    let selectFile = null;
    if(!imgInput) return ;

    imgInput.addEventListener('change' , e => {
        const file = imgInput.files[0];
        if(!file) return;

        if(file.size > MAX) {
            alert('이미지는 3MB 이하만 업로드할 수 있습니다.');
            imgInput.value = '';
            return ;
        }
        
        selectFile = file;
        imgDiv.classList.remove('display-none');
        textarea.disabled = true;

        // 이미지 미리보기
        const reader = new FileReader();
        reader.onload = () => {
        previewArea.src = reader.result;
        };
        reader.readAsDataURL(file);

    })

    // 선택한 이미지 삭제

    imgDelete.addEventListener('click', e=> {
        // 파일 초기화
        imgInput.value = '';

        // ui 복구
        imgDiv.classList.add('display-none');
        previewArea.src = '';
        textarea.disabled = false;
        textarea.focus();
    })

    /*  전송 버튼 클릭 시 파일 서버에 전송 */
    imgSendBtn.addEventListener('click', e => {
        const totalMember = document.querySelector('.member-counting').innerText;
        if(!selectFile) return;

        const formData = new FormData;
        formData.append('img', selectFile);
        formData.append('roomNo', currentRoomNo);
        formData.append('totalCount', Number(totalMember));
        formData.append('memberNo', myNo)

        fetch('/devtalk/send-img', {
            method :"POST",
            body : formData
        })
        .then(resp => {
        if(!resp.ok) throw new Error('upload failed');
        })
        .catch(err => {
            console.error('이미지 전송 실패:', err);
        });


        selectFile = null;
        imgInput.value = '';
        previewArea.src = '';
        imgDiv.classList.add('display-none');
        textarea.disabled = false;
        textarea.value = '';
        textarea.focus();
    })

}






// ======================================================================
// 신고 모달 띄우기
function openReportModal(reportedNo, targetNo) {

     fetch(`/report/modal?memberNo=${reportedNo}`)
        .then(res => res.text())
        .then(html => {
         const root = document.getElementById("modal-root");
         root.innerHTML = html;              
         const modal = root.querySelector("#reportModal");
        modal.classList.remove("display-none");

         modal.dataset.targetType = 'MESSAGE';
         modal.dataset.targetNo = targetNo;
 
         bindReportModalEvents();
     });
 }

 const reOverlay = document.getElementById("reportModal");
 
 reOverlay?.addEventListener('click', e => {
     if (e.target.id === "reportModal") {
         closeReportModal();
     }
 });
 




// ======================================================
// 멘션 함수    

// 아래 함수에서 공통으로 쓸 변수들 전역으로 뺴둠
let mentionContext = {
    textarea: null,
    mentionBox: null
};

// 채팅방 정보 로드 후 바인딩 될 함수
function bindMentionInput() {

    // 현재 채팅방의 메세지 입력창 멘션 박스 
    const textarea = document.getElementById('send-message');
    const mentionBox = document.getElementById('mention-box');

    mentionContext.textarea = textarea;
    mentionContext.mentionBox = mentionBox;

    if (!textarea || !mentionBox) return;

    // 입력 이벤트 감지
    textarea.addEventListener('input', e => {
        const value = textarea.value;
        // 현재 커서 위치
        const cursor = textarea.selectionStart;

        // 커서 위치 직전까지 문자열
        const text = value.slice(0, cursor);

        // 커서 앞에 @로 시작하는 단어 검사
        // 멘션 트리거
        const match = text.match(/@([\w가-힣]*)$/);
        if (!match) {
            mentionBox.classList.add('display-none');
            return;
        }

        const keyword = match[1];
        showMentionCandidates(keyword);
    });
}


// 멘션 후보 목록 조회
function showMentionCandidates(keyword) {

    const { mentionBox } = mentionContext;

    fetch(`/devtalk/mention?keyword=${keyword}&roomNo=${currentRoomNo}`)
        .then(resp => resp.json())
        .then(list => {

            // 기존 목록 초기화
            mentionBox.innerHTML = '';

            for (const user of list) {
                const div = document.createElement('div');
                div.className = 'mention-item';
                div.innerText = user.member_nickname;
                div.dataset.memberNo = user.member_no;
                div.onclick = () => insertMention(user.member_nickname);
                mentionBox.appendChild(div);
            }

            mentionBox.classList.remove('display-none');
        });
}


// 멘션 목록 클릭 시 입력창 실제로 삽입 @닉네임 이런식
function insertMention(nickname) {

    const { textarea, mentionBox } = mentionContext;

    const cursor = textarea.selectionStart;
    const text = textarea.value;

    const before = text.slice(0, cursor)
        .replace(/@[\w가-힣]*$/, `@${nickname} `);

    const after = text.slice(cursor);

    textarea.value = before + after;
    mentionBox.classList.add('display-none');
    textarea.focus();
}





// -=================================================
// 타이핑 이벤트

let isTyping = false;
function bindTypingEvent() {

    const textArea = document.getElementById('send-message');
    if (!textArea) return;



    textArea.addEventListener('input', () => {

        const value = textArea.value.trim().length > 0;

        if (value && !isTyping) {
            
            sendTyping(true);
            isTyping = true;
        }


        if(!value && isTyping) {
            sendTyping(false)
            isTyping = false;
        }

    });
}

// 타이핑 전송
function sendTyping(state) {
    stompClient.send("/devtalk/chat.typing", {}, JSON.stringify({
        roomNo: currentRoomNo,
        memberNo: myNo,
        // memberNickname: myNick,
        typing: state
    }));
}


window.addEventListener('beforeunload', () => {
        stompClient.send("/devtalk/chat.leave", {}, JSON.stringify({
        room_no : currentRoomNo ,
        member_no : myNo}));

});