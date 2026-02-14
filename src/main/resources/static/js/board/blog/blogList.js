console.log("blogList.js loaded")

const blogGrid = document.getElementById('blogGrid');
const scrollTopBtn = document.getElementById('scrollTopBtn');
const loader = document.getElementById('loader');
const filterButtons = document.querySelectorAll('.filter-btn');

// [수정] HTML에서 초기 상태값 읽어오기
const initialIsLastInput = document.getElementById('initialIsLast');
let isLastPage = initialIsLastInput ? (initialIsLastInput.value === 'true') : false;

let currentPage = 1;
let currentSort = 'id';
let isLoading = false;

// [삭제됨] cleanContent 함수는 이제 필요 없습니다! (백엔드 DTO가 summary를 줌)

// 1. 카드 생성 함수
function createCard(post) {
    const card = document.createElement('div');
    card.className = 'blog-card';

    const dateStr = post.bCreateDate || post.bcreate_date;

    // 태그 HTML 생성 로직
    let tagsHtml = '';
    const tagList = post.tagList || post.tag_list;

    if (tagList && tagList.length > 0) {
        tagsHtml = '<div class="card-tags" style="margin-top:10px; display:flex; flex-wrap:wrap; gap:5px;">';
        // 최대 3개까지만 표시
        tagList.slice(0, 3).forEach(tag => {
            tagsHtml += `<span class="tag-pill" style="font-size:12px; background:#f1f3f5; color:#495057; padding:3px 8px; border-radius:12px;">#${tag}</span>`;
        });
        if (tagList.length > 3) {
            tagsHtml += `<span style="font-size:11px; color:#888; align-self:center;">+${tagList.length - 3}</span>`;
        }
        tagsHtml += '</div>';
    }

    // 썸네일 처리
    let displayImg = '/images/logo.png';
    if (post.thumbnail_url && post.thumbnail_url.trim() !== '') {
        displayImg = post.thumbnail_url;
    }

    const detailUrl = `/blog/detail/${post.boardNo || post.board_no}`;
    const paidIcon = (post.isPaid === 'Y' || post.is_paid === 'Y') ? '<i class="fa-solid fa-crown" style="color:#ffd700; margin-right:5px;"></i>' : '';

    // [핵심 변경] JS가 청소하는 게 아니라, 백엔드가 준 'summary'를 그대로 씁니다.
    // DTO의 getSummary() 덕분에 JSON에 'summary' 필드가 자동으로 생깁니다.
    const cleanText = post.summary || "";


    card.innerHTML = `
        <a href="${detailUrl}" class="card-link">
            <div class="card-image">
                <img src="${displayImg}" alt="썸네일" onerror="this.src='/images/logo.png'">
            </div>

            <div class="card-content">
                <h3 class="card-title">${paidIcon}${post.boardTitle || post.board_title}</h3> 
                
                <p class="card-desc">${cleanText}</p>
                
                <div class="card-meta">
                    <span class="author">${post.memberNickname || post.member_nickname}</span>
                    <div class="stats">
                        <span><i class="fa-solid fa-eye"></i> ${post.boardCount || post.board_count || 0}</span>
                        <span><i class="fa-solid fa-comment"></i> ${post.commentCount || post.comment_count || 0}</span>
                    </div>
                </div>
                <div class="card-footer-time">
                    <span>${dateStr}</span>
                </div>
            </div>
        </a>
    `;
    return card;
}

// 2. 데이터 페치 (무한 스크롤)
function fetchPosts(isReset = false) {
    if (isLoading) return;

    if (isReset) {
        blogGrid.innerHTML = '';
        currentPage = 0;
        isLastPage = false;
        loader.style.display = 'block';
    }

    if (isLastPage) {
        loader.style.display = 'none'; // 마지막 페이지면 로더 숨김
        return;
    }
    
    isLoading = true;

    const url = `/api/blog/list?page=${currentPage}&size=12&sort=${currentSort},desc`;

    fetch(url)
        .then(res => {
            if (!res.ok) throw new Error('불러오기 실패');
            return res.json();
        })
        .then(data => {
            const posts = data.content;

            if (!posts || posts.length === 0) {
                isLastPage = true;
                loader.style.display = 'none';
                if (isReset) blogGrid.innerHTML = '<div class="no-data"><p>등록된 게시글이 없습니다.</p></div>';
                return;
            }

            if (data.last) {
                isLastPage = true;
                loader.style.display = 'none';
            }

            posts.forEach(post => {
                blogGrid.appendChild(createCard(post));
            });
            currentPage++;
        })
        .catch(err => {
            console.error(err);
            loader.style.display = 'none';
        })
        .finally(() => isLoading = false);
}

// 3. 무한 스크롤 옵저버
const observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting && !isLastPage && !isLoading) {
        fetchPosts();
    }
}, { threshold: 0.1 });

if (loader) observer.observe(loader);

// 4. 필터 버튼
filterButtons.forEach(btn => {
    btn.addEventListener('click', function () {
        filterButtons.forEach(b => b.classList.remove('active'));
        this.classList.add('active');

        const sort = this.getAttribute('data-sort');
        if (sort === 'view') currentSort = 'viewCount';
        else if (sort === 'like') currentSort = 'likeCount';
        else if (sort === 'comment') currentSort = 'commentCount';
        else currentSort = 'id';

        fetchPosts(true);
    });
});

// TOP 버튼
if (scrollTopBtn) {
    window.addEventListener('scroll', () => {
        scrollTopBtn.style.display = (window.scrollY > 500) ? 'flex' : 'none';
    });
    scrollTopBtn.addEventListener('click', () => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    });
}
