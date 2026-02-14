const notiBtn = document.getElementById('noti-btn');
let sse = null;
let notiOpened = false;

/* ===================== 알림 패널 ===================== */

notiBtn.addEventListener('click', () => {

    const panel = document.querySelector('.alarm-panel');

    if (panel.classList.contains('display-none')) {
        panel.classList.remove('display-none');

        if (!notiOpened) {
            notiOpened = true;
            selectNotiList("ALL");
        }
    }
    else {
        panel.classList.add('display-none');
    }
});

/* ===================== 알림 조회 ===================== */

function selectNotiList(type) {

    fetch("/notification/selectList?type=" + type)
        .then(resp => resp.text())
        .then(html => {


            document.getElementById('alarm-panel').outerHTML = html;




            bindAlarmEvents();
        })
        .catch(console.log);
}

/* ===================== 이벤트 재바인딩 ===================== */

const filterMap = {
    allNoti: "ALL",
    likeNoti: "LIKE",
    commentNoti: "COMMENT",
    followNoti: "FOLLOW",
    mentionNoti: "MENTION"
};

function bindAlarmEvents() {

    const filters = document.querySelectorAll('.filter');

    for (const btn of filters) {
        btn.onclick = (e) => {
            e.stopPropagation();

            // UI 상태 갱신
            filters.forEach(b => b.classList.remove('is-active'));
            btn.classList.add('is-active');

            const type = filterMap[btn.id];
            if (!type) return;

            selectNotiList(type);
        };
    }


        /* 해당 알림 클릭 시 읽은 느낌  */
    const alarmItems = document.querySelectorAll('.alarm-item')



    for (let alarmItem of alarmItems) {
        alarmItem.addEventListener('click', async e => {

            if (e.target.closest('.close')) {
                return;
            }

            e.preventDefault();

            const notiNo = Number(alarmItem.dataset.id)                                ;

            const resp = await fetch(`/notification/click/${notiNo}`, {method : 'POST'})

            const redirectUrl = await resp.text();

            console.log("url확인 : ", redirectUrl);

            alarmItem.classList.add('read-noti');

            notiCount();

            location.href = redirectUrl;
        })
        
            /* ✕ 버튼 클릭 → 알림 삭제 */
        const closeBtn = alarmItem.querySelector('.close');
        if (closeBtn) {
            closeBtn.addEventListener('click', async e => {
                e.stopPropagation();
        
                const notiNo = Number(alarmItem.dataset.id);
        
                const resp = await fetch(`/notification/${notiNo}`, { method: 'DELETE' });
        
                if (resp.ok) {
                    alarmItem.remove();
                    notiCount();
                } else {
                    alert('알림 삭제 실패');
                }
            });
        }
    }


}

/* ===================== SSE ===================== */

function connectSse() {

    if (!loginCheck) return;

    if (sse) {
        sse.close();
        sse = null;
    }

    sse = new EventSource("/sse/connect");

    sse.addEventListener("message", e => {
        console.log("SSE 수신:", e.data);
        const data = JSON.parse(e.data);

        const badge = document.getElementById("notiUnread");

        if (data.unreadCount > 0) {
            badge.classList.add("notiUnread");
            badge.innerText = data.unreadCount;
        } else {
            badge.classList.remove("notiUnread");
            badge.innerText = "";
        }
    });

    sse.onerror = () => {
        sse.close();
        sse = null;
        setTimeout(connectSse, 5000);
    };
}

/* ===================== 초기화 ===================== */

document.addEventListener("DOMContentLoaded", () => {
    connectSse();
    notiCount();
});

/* ===================== 알림 개수 ===================== */

function notiCount() {

    fetch("/notiCount")
        .then(resp => resp.text())
        .then(count => {
            const badge = document.getElementById("notiUnread");

            if (count > 0) {
                badge.classList.add("notiUnread");
                badge.innerText = count;
            } else {
                badge.classList.remove('notiUnread')
                badge.innerText = '';
            }
        })
        .catch(() => console.log("안 읽은 알림 조회 실패"));
}





const notiMenuBtn = document.querySelector('.noti-menu-btn')
const notiMenuArea = document.querySelector('.noti-menu-dropdown')


/* 전체 읽음 삭제 메뉴 드롭다운 */
notiMenuBtn.addEventListener('click', e=>{
    notiMenuArea.classList.toggle('display-none')
})



// 알림 전체 삭제
document.getElementById("allDeleteNoti").addEventListener("click", async e => {

    
    if(confirm("정말 알림을 모두 삭제하시겠습니까 ? ")) {
        const resp = await fetch("/notification/allDelete", {method : "DELETE"});
    
        if(resp.ok) {
            notiCount();
            selectNotiList("ALL");
            notiMenuArea.classList.toggle('display-none');
        }

    }

    
        


})


// 알림 전ㅊ ㅔ읽기
document.getElementById("allReadNoti").addEventListener("click", async e=> {

    const resp = await fetch("/notification/allRead", {method : "POST"});

    if(resp.ok) {
        notiCount();
        selectNotiList("ALL");
        notiMenuArea.classList.toggle('display-none');
    }

})







