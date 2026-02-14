console.log("blogDetail.js loaded")

// === 전역 변수 및 초기 데이터 로드 ===
const postId = document.getElementById('postId').value;
const loginUserId = document.getElementById('loginUserId').value;
const postPrice = parseInt(document.getElementById('postPrice').value || 0);
let userBalance = 0;
const balanceEl = document.getElementById('userBalance');
if (balanceEl) userBalance = parseInt(balanceEl.value || 0);

// DOM 요소 참조
const commentListEl = document.getElementById('commentList');
const commentTotalCountEl = document.getElementById('commentTotalCount');
const mainCommentInput = document.getElementById('mainCommentInput');
const btnPostLike = document.getElementById('btnPostLike');
const postHeartIcon = document.getElementById('postHeartIcon');

// 초기화
function init() {
    loadComments();
}

// === 게시글 기능 ===

// === blogDetail.js 하단에 추가 ===


// 게시글 스크랩 토글 함수
function togglePostScrap() {
    if (!loginUserId) {
        if (confirm("로그인이 필요합니다. 로그인 페이지로 이동하시겠습니까?")) {
            location.href = "/member/login";
        }
        return;
    }

    const scrapIcon = document.getElementById('postScrapIcon');
    const btnScrap = document.getElementById('btnPostScrap');

    fetch(`/api/blog/scrap/${postId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                if (data.isScraped) {
                    // 스크랩 성공 시 아이콘 변경 (꽉 찬 북마크)
                    scrapIcon.classList.replace('fa-regular', 'fa-solid');
                    btnScrap.classList.add('active');
                    alert("스크랩 되었습니다. '내 블로그 > 스크랩' 탭에서 확인 가능합니다.");
                } else {
                    // 스크랩 취소 시
                    scrapIcon.classList.replace('fa-solid', 'fa-regular');
                    btnScrap.classList.remove('active');
                    alert("스크랩이 취소되었습니다.");
                }
            }
        })
        .catch(err => {
            console.error("Scrap Error:", err);
            alert("스크랩 처리 중 오류가 발생했습니다.");
        });
}

function togglePostLike() {
    if (!loginUserId) return alert("로그인이 필요합니다.");
    fetch(`/api/blog/like/${postId}`, { method: 'POST' })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                if (data.isLiked) {
                    btnPostLike.classList.add('active');
                    postHeartIcon.classList.replace('fa-regular', 'fa-solid');
                } else {
                    btnPostLike.classList.remove('active');
                    postHeartIcon.classList.replace('fa-solid', 'fa-regular');
                }
                document.getElementById('postLikeCount').innerText = data.count;
            }
        });
}

// 게시글 삭제
function deletePost(boardNo) {
    // 1. 안전장치: 매개변수(boardNo)가 없으면, HTML 상단의 hidden input 값(postId)을 사용
    if (!boardNo) {
        // 전역변수 postId 혹은 DOM 요소에서 가져오기
        const hiddenInput = document.getElementById('postId');
        if (hiddenInput) {
            boardNo = hiddenInput.value;
        }
    }

    // 2. 그래도 번호가 없으면 에러 처리
    if (!boardNo) {
        alert("게시글 번호를 찾을 수 없습니다.");
        console.error("BoardNo is null or undefined");
        return;
    }

    if (!confirm("정말로 삭제하시겠습니까? 복구할 수 없습니다.")) {
        return;
    }

    // 3. 정확한 번호로 서버에 요청
    fetch(`/api/blog/delete/${boardNo}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (response.ok) {
                alert("게시글이 삭제되었습니다.");
                location.href = "/blog/list"; // 목록으로 이동
            } else {
                // 서버에서 에러 메시지를 보냈다면 띄워줌
                return response.text().then(text => alert("삭제 실패: " + text));
            }
        })
        .catch(error => {
            console.error("Delete Error:", error);
            alert("오류가 발생했습니다.");
        });
}

// 팔로우/언팔로우 토글 함수
function toggleFollow(btn) {
    // 1. 로그인 여부 체크 (상단에 정의된 loginUserId 활용)
    if (!loginUserId) {
        if (confirm("로그인이 필요합니다. 로그인 페이지로 이동하시겠습니까?")) {
            location.href = "/member/login";
        }
        return;
    }

    // 2. 작성자의 이메일(ID) 추출
    // HTML의 author-link href가 "/blog/{email}" 형식이므로 마지막 부분을 가져옵니다.
    const authorLink = document.querySelector('.author-link').getAttribute('href');
    const targetEmail = authorLink.split('/').pop();

    // 3. 본인 팔로우 방지
    // (서버에서도 체크하지만 클라이언트에서 먼저 막아주는 것이 UX에 좋습니다)

    // 4. API 호출
    fetch(`/api/blog/follow/${targetEmail}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(res => {
            if (res.status === 401) throw new Error("로그인 필요");
            return res.json();
        })
        .then(data => {
            if (data.success) {
                if (data.isFollowed) {
                    // 팔로우 성공 상태
                    btn.innerText = "팔로잉";
                    btn.classList.add('active'); // CSS에서 보라색 배경 처리용
                    alert(targetEmail + "님을 팔로우합니다.");
                } else {
                    // 언팔로우 상태
                    btn.innerText = "+ 팔로우";
                    btn.classList.remove('active');
                    alert("팔로우를 취소했습니다.");
                }
            }
        })
        .catch(err => {
            console.error("Follow Error:", err);
            alert("팔로우 처리 중 오류가 발생했습니다.");
        });
}

// === 댓글 기능 ===

function loadComments() {
    fetch(`/api/posts/${postId}/comments`)
        .then(res => res.json())
        .then(data => { renderComments(data); })
        .catch(err => console.error("댓글 불러오기 오류 :", err));
}

function renderComments(comments) {
    commentListEl.innerHTML = '';
    let totalCount = 0;
    comments.forEach(comment => {
        totalCount++;
        commentListEl.appendChild(createCommentElement(comment));
        if (comment.children && comment.children.length > 0) {
            comment.children.forEach(reply => {
                totalCount++;
                commentListEl.appendChild(createCommentElement(reply, true));
            });
        }
    });
    if (commentTotalCountEl) commentTotalCountEl.innerText = totalCount;
    const countEl = document.getElementById('commentCount');
    if (countEl) countEl.innerText = totalCount;
}

function createCommentElement(data, isReply = false) {
    const el = document.createElement('div');
    el.className = `comment-item ${isReply ? 'reply' : ''}`;
    
    // [중요] 전체 덩어리 ID: 'comment-item-숫자'
    el.id = `comment-item-${data.commentNo}`;

    const isMine = loginUserId && (String(data.memberNo) === String(loginUserId));
    const profileSrc = data.profileImg || '/images/user.png';
    const likeClass = data.isLiked ? 'active' : '';
    const heartIcon = data.isLiked ? 'fa-solid' : 'fa-regular';

    el.innerHTML = `
        <div class="comment-content">
            <div class="comment-header">
                <img src="${profileSrc}" class="avatar">
                <div class="comment-meta">
                    <span class="username">${data.memberNickname}</span>
                    <span class="comment-date">${data.cCreateDate || '방금 전'}</span>
                </div>
            </div>
            
            <div class="comment-text" id="comment-content-${data.commentNo}">${data.commentContent}</div>
            
            <div class="comment-actions">
                <button class="action-btn like-comment-btn ${likeClass}" onclick="toggleCommentLike(${data.commentNo}, this)">
                    <i class="${heartIcon} fa-heart"></i>
                    <span class="like-count">${data.likeCount}</span>
                </button>
                ${!isReply ? `<button class="action-btn" onclick="openReplyForm(${data.commentNo})">답글</button>` : ''}
                
                ${isMine ? `
                    <button class="action-btn" onclick="enableEditMode(${data.commentNo})">수정</button>
                    <button class="action-btn delete-btn" onclick="deleteComment(${data.commentNo})">삭제</button>
                ` : ''}
            </div>
            <div id="reply-form-area-${data.commentNo}"></div>
        </div>
    `;
    return el;
}

// 댓글 등록 함수 수정
function addMainComment() {
    if (!loginUserId) return alert("로그인이 필요합니다.");
    const content = mainCommentInput.value.trim();
    if (!content) return alert("내용을 입력해주세요.");

    const payload = {
        boardNo: parseInt(postId),        // ReplyDto의 @JsonProperty("boardNo")
        commentContent: content,          // ReplyDto의 @JsonProperty("commentContent")
        parentCommentNo: null             // 새 댓글은 부모 없음
    };

    fetch('/api/comments', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
        .then(res => {
            if (res.ok) {
                mainCommentInput.value = '';
                loadComments();
            } else {
                return res.text().then(text => { alert("등록 실패: " + text) });
            }
        })
        .catch(err => console.error("에러 발생:", err));
}

function toggleCommentLike(commentId, btn) {
    if (!loginUserId) return alert("로그인이 필요합니다.");
    fetch(`/api/comments/${commentId}/like`, { method: 'POST' })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                const icon = btn.querySelector('i');
                const countSpan = btn.querySelector('.like-count');
                let count = parseInt(countSpan.innerText);
                if (data.liked) {
                    btn.classList.add('active');
                    icon.classList.replace('fa-regular', 'fa-solid');
                    countSpan.innerText = count + 1;
                } else {
                    btn.classList.remove('active');
                    icon.classList.replace('fa-solid', 'fa-regular');
                    countSpan.innerText = Math.max(0, count - 1);
                }
            }
        });
}

// 답글창 열기
function openReplyForm(commentNo) {
    // 이미 열린 다른 답글 폼 닫기
    document.querySelectorAll('[id^="reply-form-area-"]').forEach(el => el.innerHTML = '');

    const area = document.getElementById(`reply-form-area-${commentNo}`);
    area.innerHTML = `
        <div class="reply-form-container">
            <textarea id="replyInput-${commentNo}" placeholder="답글을 작성하세요..."></textarea>
            <div class="reply-actions">
                <button class="btn-reply-cancel" onclick="this.closest('.reply-form-container').remove()">취소</button>
                <button class="btn-reply-submit" onclick="addReply(${commentNo})">등록</button>
            </div>
        </div>`;
}

// 답글 등록
function addReply(parentNo) {
    const input = document.getElementById(`replyInput-${parentNo}`);
    const content = input.value.trim();
    if (!content) return alert("내용을 입력하세요.");

    fetch('/api/comments', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            boardNo: parseInt(postId),
            commentContent: content,
            parentCommentNo: parentNo
        })
    }).then(res => {
        if (res.ok) loadComments();
        else alert("답글 등록 실패");
    });
}
// 댓글 삭제
function deleteComment(commentId) {
    if (!confirm("정말 삭제하시겠습니까?")) return;
    fetch(`/api/comments/${commentId}`, { method: 'DELETE' }).then(res => { if (res.ok) loadComments(); });
}


// LKS 댓글 추가
function scrollToHashIfExists() {
    const hash = location.hash; // 예: "#comment-6"
    if (!hash) return;

    const target = document.querySelector(hash);
    if (target) {
        target.scrollIntoView({ behavior: "smooth", block: "center" });
    }
}




// 게시글 수정
function enableEditMode(id) {
    const textEl = document.getElementById(`comment-content-${id}`);

    if (!textEl) {
        console.error(`수정할 요소를 찾을 수 없습니다. ID: comment-content-${id}`);
        return;
    }

    const currentText = textEl.innerText; // 현재 텍스트 가져오기

    // 버튼 숨기기 (선택 사항: UI 깔끔하게 하기 위해)
    const wrapper = document.getElementById(`comment-item-${id}`);
    if (wrapper) {
        const actionArea = wrapper.querySelector('.comment-actions');
        if (actionArea) actionArea.style.display = 'none';
    }

    // 텍스트 영역을 입력창(textarea)으로 교체
    textEl.innerHTML = `
        <div class="edit-form-wrapper" style="margin-top: 10px;">
            <textarea id="editInput-${id}" class="edit-textarea" style="width: 100%; height: 80px; padding: 10px; border: 1px solid #ddd; border-radius: 5px; resize: none;">${currentText}</textarea>
            <div class="comment-edit-actions" style="margin-top: 8px; text-align: right;">
                <button class="btn-cancel-edit" onclick="loadComments()" style="margin-right: 5px; padding: 5px 10px; cursor:pointer;">취소</button>
                <button class="btn-save-edit" onclick="saveEdit(${id})" style="background-color: #9b59b6; color: white; border: none; padding: 5px 15px; border-radius: 4px; cursor:pointer;">저장</button>
            </div>
        </div>`;

    // 입력창에 포커스
    const input = document.getElementById(`editInput-${id}`);
    if (input) input.focus();
}

// 게시글 수정 저장 api
function saveEdit(id) {
    const newContent = document.getElementById(`editInput-${id}`).value;
    if (!newContent.trim()) return alert("내용을 입력해주세요.");

    fetch(`/api/comments/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        // 필드명을 DTO의 @JsonProperty와 일치시킴
        body: JSON.stringify({ commentContent: newContent })
    }).then(res => {
        if (res.ok) {
            alert("수정되었습니다.");
            loadComments();
        } else {
            alert("수정 실패");
        }
    }).catch(err => console.error("수정 오류:", err));
}

// 결제 관련
function openPurchaseModal() {
    if (!loginUserId) return alert("로그인이 필요합니다.");
    document.getElementById('modalOverlay').classList.remove('hidden');
    document.getElementById('modalPurchase').classList.remove('hidden');
}

function closeAllModals() {
    document.getElementById('modalOverlay').classList.add('hidden');
    document.querySelectorAll('.modal-box').forEach(box => box.classList.add('hidden'));
}

// 신고
function openBlogReportModal(targetMemberNo, boardNo) {
    const loginUserId = document.getElementById("loginUserId").value;
    
    if (!loginUserId) {
        alert("로그인 후 이용해주세요.");
        return;
    }

    // 팀원이 만든 공통 함수 호출 (report.js)
    // fetch 요청을 통해 모달 HTML을 가져옴
    fetch(`/report/modal?memberNo=${targetMemberNo}`)
        .then((res) => res.text())
        .then((html) => {
            const root = document.getElementById("modal-root");
            root.innerHTML = html;
            
            const modal = root.querySelector("#reportModal");
            modal.classList.remove("display-none"); // 모달 보이기

            // ★ 중요: 백엔드로 보낼 때 "BOARD" 타입임을 명시
            modal.dataset.targetType = "BOARD"; 
            modal.dataset.targetNo = boardNo;
            modal.dataset.targetMemberNo = targetMemberNo;
            
            // 팀원 코드에 있는 이벤트 바인딩 함수 (닫기 버튼, 제출 버튼 등 활성화)
            if (typeof bindReportModalEvents === "function") {
                bindReportModalEvents();
            }
        })
        .catch(err => console.error("신고 모달 로드 실패:", err));
}

/**
 * 구매 확정 처리 (서버의 PayController로 요청 전송)
 */
function processPayment() {
    const postId = document.getElementById("postId").value;
    const price = document.getElementById("postPrice").value;
    const loginUserId = document.getElementById("loginUserId").value;

    if (!loginUserId) {
        alert("로그인이 필요한 서비스입니다.");
        return;
    }

    // 서버로 구매 거래 요청 (PayController의 /payment/trade 호출)
    fetch("/payment/trade", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            contentType: "POST",     // 거래 유형 (게시글)
            contentId: postId,       // 게시글 번호
            price: price             // 소모될 콩 개수
        })
    })
        .then(resp => {
            if (!resp.ok) {
                // 서버가 에러(400, 500 등)를 보낸 경우
                return resp.json().then(err => { throw new Error(err.message) });
            }
            return resp.json();
        })
        .then(result => {
            // 성공 시
            document.getElementById("modalPurchase").classList.add("hidden");
            document.getElementById("modalSuccess").classList.remove("hidden");
        })
        .catch(err => {
            // 여기서 에러 메시지에 따라 분기 처리
            if (err.message.includes("잔액")) {
                document.getElementById("modalPurchase").classList.add("hidden");
                document.getElementById("modalNoBalance").classList.remove("hidden");
            } else {
                alert("오류 발생: " + err.message);
            }
        });
}

init();