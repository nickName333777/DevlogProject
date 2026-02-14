console.log("myActivity.js loaded");


document.addEventListener('DOMContentLoaded', () => {
    const tabs = document.querySelectorAll('.tab-item');
    const listContainer = document.getElementById('list-container');
    const sectionTitle = document.getElementById('section-title');
    const loadingSpinner = document.getElementById('loading-spinner');

    // 모달 관련
    const modal = document.getElementById('delete-modal');
    const btnCancel = document.getElementById('btn-cancel-delete');
    const btnConfirm = document.getElementById('btn-confirm-delete');
    let targetDeleteId = null;

    // 탭 클릭 이벤트 리스너 등록
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            // 모든 탭 비활성화 후 클릭한 탭 활성화
            tabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');

            // data-tab 속성값 가져오기 (likes, history, drafts, purchases)
            const tabType = tab.dataset.tab;
            
            // 타이틀 변경 및 데이터 로드
            updateTitle(tabType);
            fetchData(tabType);
        });
    });

    // 섹션 타이틀 변경 함수
    function updateTitle(type) {
        const titles = {
            'likes': '좋아요 한 게시물',
            'history': '최근 본 게시물',
            'drafts': '임시 저장한 글',
            'purchases': '구매 내역'
        };
        if(sectionTitle) sectionTitle.textContent = titles[type] || '내 활동';
    }

    // 서버에서 데이터 가져오기 (AJAX)
    function fetchData(type) {
        // 목록 초기화 및 로딩중 표시
        listContainer.innerHTML = '';
        if(loadingSpinner) {
            listContainer.appendChild(loadingSpinner);
            loadingSpinner.style.display = 'flex';
        }

        // API 호출
        fetch(`/api/myPage/activity-list?type=${type}`)
            .then(res => {
                if(!res.ok) throw new Error("데이터를 불러오는데 실패했습니다.");
                return res.json();
            })
            .then(data => {
                // 데이터 렌더링
                renderList(data, type);
            })
            .catch(err => {
                console.error(err);
                listContainer.innerHTML = '<div style="padding:40px; text-align:center; color:#888;">데이터를 불러오지 못했습니다.</div>';
            })
            .finally(() => {
                // 로딩중 숨기기
                if(loadingSpinner) loadingSpinner.style.display = 'none';
            });
    }

    // 리스트 HTML 그리기
    function renderList(items, type) {
        // 로딩중 제외하고 내용 비우기 (혹은 innerHTML=''로 덮어쓰기)
        listContainer.innerHTML = '';

        // 데이터가 없을 경우 처리
        if (!items || items.length === 0) {
            listContainer.innerHTML = '<div style="padding:60px 0; text-align:center; color:#999; font-size:15px;">내역이 없습니다.</div>';
            return;
        }

        const isDraft = (type === 'drafts');
        const isPurchase = (type === 'purchases');

        items.forEach(item => {
            const itemEl = document.createElement('div');
            itemEl.className = 'post-item';

            // DTO 변수명 매핑 (Backend MyActivityDto와 일치)
            const bNo = item.board_no;            
            const bTitle = item.board_title || '제목 없음';      
            const bContent = item.board_content || '';
            const bThumb = item.thumbnail_url || '/images/logo.png'; // 썸네일 없으면 기본 로고
            const bDate = item.activity_date || '';
            const bAuthor = item.member_nickname || '익명';
            const bCount = item.board_count || 0;
            const isPaidVal = item.is_paid || 'N';

            // 링크 설정 (임시저장은 수정 페이지, 나머지는 상세 페이지)
            const link = isDraft ? `/blog/write?no=${bNo}` : `/blog/detail/${bNo}`;

            // 유료 아이콘 (구매내역이거나 유료글일 때)
            const crownHtml = (isPurchase || isPaidVal === 'Y') 
                ? '<i class="fa-solid fa-crown" style="color:#ffd700; margin-right:6px; font-size:14px;"></i>' 
                : '';
            
            // 삭제 버튼 (임시저장 탭에서만 보임)
            const deleteBtn = isDraft 
                ? `<div class="delete-btn-wrapper" title="삭제" onclick="openDeleteModal(${bNo})">
                    <i class="fa-regular fa-trash-can"></i>
                    </div>` 
                : '';

            // 메타 정보 (작성자, 날짜, 조회수) - 임시저장은 안 보여줌
            let metaInfo = '';
            if(!isDraft) {
                metaInfo = `
                    <div style="font-size:13px; color:#666; margin-top:8px; display:flex; align-items:center;">
                        <span style="font-weight:600; color:#333;">${bAuthor}</span>
                        <span style="margin:0 8px; color:#ddd;">|</span>
                        <span>${bDate}</span>
                        <span style="margin:0 8px; color:#ddd;">|</span>
                        <span><i class="fa-regular fa-eye"></i> ${bCount}</span>
                    </div>
                `;
            }

            // 본문 요약 (HTML 태그 제거 후 60자 제한)
            // 정규식: <[^>]*>? -> HTML 태그 제거
            let summary = bContent.replace(/<[^>]*>?/g, '').replace(/\s+/g, ' ').trim();
            if(summary.length > 60) summary = summary.substring(0, 60) + '...';

            // HTML 조립
            itemEl.innerHTML = `
                <a href="${link}" class="thumb-link">
                    <img src="${bThumb}" alt="썸네일" class="post-thumb" onerror="this.src='/images/logo.png'">
                </a>
                <div class="post-info">
                    <div class="post-header">
                        <a href="${link}" class="title-link">
                            <span style="display:flex; align-items:center;">${crownHtml}${bTitle}</span>
                        </a>
                        ${deleteBtn}
                    </div>
                    <p style="font-size:14px; color:#888; margin:6px 0; line-height:1.4;">${summary}</p>
                    ${metaInfo}
                </div>
            `;
            listContainer.appendChild(itemEl);
        });
    }

    // 4. 모달 열기 (전역 함수로 연결)
    window.openDeleteModal = (id) => {
        targetDeleteId = id;
        if(modal) modal.classList.remove('hidden');
    };

    // 모달 닫기 (취소)
    if(btnCancel) {
        btnCancel.addEventListener('click', () => {
            if(modal) modal.classList.add('hidden');
            targetDeleteId = null;
        });
    }

    // 모달 확인 (삭제 실행)
    if(btnConfirm) {
        btnConfirm.addEventListener('click', () => {
            if(!targetDeleteId) return;

            // 실제 삭제 API 호출
            fetch(`/api/blog/delete/${targetDeleteId}`, {
                method: 'DELETE',
                headers: { 'Content-Type': 'application/json' }
            })
            .then(res => {
                if(res.ok) {
                    alert("삭제되었습니다.");
                    fetchData('drafts'); // 목록 새로고침
                } else {
                    return res.text().then(text => alert("삭제 실패: " + text));
                }
            })
            .catch(err => {
                console.error(err);
                alert("오류가 발생했습니다.");
            })
            .finally(() => {
                if(modal) modal.classList.add('hidden');
                targetDeleteId = null;
            });
        });
    }

    // 5. 초기 실행: 페이지 로드 시 '좋아요' 탭 데이터 불러오기
    fetchData('likes');
});