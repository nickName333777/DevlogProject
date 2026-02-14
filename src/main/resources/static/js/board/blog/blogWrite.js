console.log('blogWrite.js loaded');

// 1. Toast UI Editor 초기화
const editor = new toastui.Editor({
    el: document.querySelector('#editor'),
    height: '600px',
    // 마크다운 대신 '위지윅' 모드로 시작 (수정 시 보기 편함)
    initialEditType: 'wysiwyg', 
    previewStyle: 'vertical',
    placeholder: '내용을 입력하세요. 오른쪽 하단에 Markdown 탭을 클릭해 더욱 편하게 작성하실 수 있습니다.',
    // 수정 모드일 때 기존 내용 불러오기
    initialValue: document.getElementById('initialContent').value || '',
    hooks: {
        addImageBlobHook: (blob, callback) => {
            // 폼 데이터 생성
            const formData = new FormData();
            formData.append('image', blob);

            // 서버로 업로드 요청
            fetch('/api/blog/imageUpload', {
                method: 'POST',
                body: formData
            })
            .then(response => {
                if (!response.ok) throw new Error('업로드 실패');
                return response.text(); // 서버가 반환한 URL (String) 받기
            })
            .then(url => {
                const encodedUrl = encodeURI(url);
                callback(encodedUrl, '이미지'); 
            })
            .catch(error => {
                console.error('이미지 업로드 실패:', error);
                alert('이미지 업로드에 실패했습니다.');
            });
        }
    }
});

// ---------------------- 오탈자 검출 - 소연!!!!!! ------------------------

let spellOn = false;
let debounceTimer = null;
let lastRequestId = 0;
let currentFixes = [];

const CHECK_DELAY = 1000;
const MIN_TEXT_LENGTH = 10;

// 토글
const spellToggle = document.getElementById("spellToggle");

spellToggle.addEventListener("change", (e) => {
    spellOn = e.target.checked;
    console.log("맞춤법 검사:", spellOn ? "ON" : "OFF");

    clearSpellOverlay();

    if (!spellOn && debounceTimer) {
        clearTimeout(debounceTimer);
        debounceTimer = null;
    }
});

// 에디터 입력
editor.on("change", () => {
    if (!spellOn) return;

    if (debounceTimer) clearTimeout(debounceTimer);
    debounceTimer = setTimeout(checkSpelling, CHECK_DELAY);
});

// 에디터 텍스트 추출해내기
function getEditorText() {
    return editor
        .getMarkdown()
        .replace(/```[\s\S]*?```/g, "")
        .replace(/[#>*_\-`]/g, "")
        .replace(/\s+/g, " ")
        .trim();
}


// 맞춤법 검사 요청보내기
async function checkSpelling() {
    const text = getEditorText();
    if (text.length < MIN_TEXT_LENGTH) return;

    const requestId = ++lastRequestId;
    console.log("[SpellCheck] send:", text);

    try {
        const res = await fetch("/api/ai/writing/spell-check", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "same-origin",
            body: JSON.stringify({ content: text }),
        });

        if (!res.ok || requestId !== lastRequestId) return;

        const { fixes } = await res.json();
        console.log("[SpellCheck] fixes:", fixes);

        currentFixes = fixes;
        renderSpellErrors(fixes);
    } catch (err) {
        console.error("SpellCheck Error:", err);
    }
}


function getEditorRoot() {
    // WYSIWYG 기준
    return document.querySelector(".toastui-editor-contents");
}

function createSpellOverlay() {
    let overlay = document.querySelector(".spell-overlay");
    if (overlay) return overlay;

    const editorRoot = getEditorRoot();
    if (!editorRoot) return null;

    overlay = document.createElement("div");
    overlay.className = "spell-overlay";

    Object.assign(overlay.style, {
        position: "absolute",
        top: "0",
        left: "0",
        width: "100%",
        height: "100%",
        pointerEvents: "none",
        zIndex: 20,
    });

    editorRoot.style.position = "relative";
    editorRoot.appendChild(overlay);

    return overlay;
}

function clearSpellOverlay() {
    const overlay = document.querySelector(".spell-overlay");
    if (overlay) overlay.remove();
}

function renderSpellErrors(fixes) {
    clearSpellOverlay();
    if (!fixes || fixes.length === 0) return;

    const editorRoot = getEditorRoot();
    const overlay = createSpellOverlay();
    if (!editorRoot || !overlay) return;

    const editorRect = editorRoot.getBoundingClientRect();

    fixes.forEach((fix) => {
        const ranges = findTextRanges(fix.before);

        ranges.forEach(({ rect, range }) => {
            const underline = document.createElement("div");

            const left = rect.left - editorRect.left;
            const top =
                rect.bottom - editorRect.top - 2;

            Object.assign(underline.style, {
                position: "absolute",
                left: `${left}px`,
                top: `${top}px`,
                width: `${rect.width}px`,
                height: "2px",
                backgroundColor: "red",
                pointerEvents: "auto",
                cursor: "pointer",
            });

            underline.title = `교정: ${fix.after}`;

            underline.addEventListener("click", () => {
                applySingleFix(range, fix.after);
            });

            overlay.appendChild(underline);
        });
    });
}

function findTextRanges(targetText) {
    const results = [];
    if (!targetText) return results;

    const editorRoot = getEditorRoot();
    if (!editorRoot) return results;

    const walker = document.createTreeWalker(
        editorRoot,
        NodeFilter.SHOW_TEXT,
        null
    );

    let node;
    while ((node = walker.nextNode())) {
        let index = node.textContent.indexOf(targetText);

        while (index !== -1) {
            const range = document.createRange();
            range.setStart(node, index);
            range.setEnd(node, index + targetText.length);

            const rects = range.getClientRects();
            Array.from(rects).forEach((rect) => {
                results.push({ rect, range });
            });

            index = node.textContent.indexOf(
                targetText,
                index + targetText.length
            );
        }
    }

    return results;
}


function applySingleFix(range, afterText) {
    range.deleteContents();
    range.insertNode(document.createTextNode(afterText));

    setTimeout(() => {
        renderSpellErrors(currentFixes);
    }, 0);
}

// ------------------ 오탈자 검출 끝 --------------------------

// 썸네일 처리 변수
const thumbInput = document.getElementById('thumbInput');
const thumbPreview = document.getElementById('thumbPreview');
const thumbnailUrlInput = document.getElementById('thumbnailUrl');

// 썸네일 파일 업로드 이벤트
thumbInput.addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('image', file); // 기존 이미지 업로드 API 재활용

    fetch('/api/blog/imageUpload', {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if(!response.ok) throw new Error("업로드 실패");
        return response.text();
    })
    .then(url => {
        thumbPreview.src = url;         // 미리보기 변경
        thumbnailUrlInput.value = url;  // hidden input에 URL 저장
        console.log("썸네일 업로드 성공:", url);
    })
    .catch(error => {
        console.error('Thumbnail Upload Error:', error);
        alert('썸네일 업로드에 실패했습니다.');
    });
});


// 2. 태그 입력 시스템
const tagInput = document.getElementById('tagInput');
const tagList = document.getElementById('tagList');
let tags = new Set();

// 초기 태그 로딩
const initialTagsVal = document.getElementById('initialTags').value;
if (initialTagsVal) {
    const loadedTags = initialTagsVal.split(',');
    loadedTags.forEach(tag => {
        if(tag.trim()) tags.add(tag.trim());
    });
    renderTags(); // 화면에 그리기
}

function addTag(text) {
    const cleanText = text.trim().replace(/,/g, '');
    if (cleanText.length > 0 && !tags.has(cleanText)) {
        tags.add(cleanText);
        renderTags();
    }
    tagInput.value = '';
}

function removeTag(text) {
    tags.delete(text);
    renderTags();
}

function renderTags() {
    tagList.innerHTML = '';
    tags.forEach(tag => {
        const tagElem = document.createElement('div');
        tagElem.className = 'tag-item';
        tagElem.innerHTML = `
            ${tag}
            <span class="btn-remove-tag" onclick="removeTag('${tag}')">&times;</span>
        `;
        tagList.appendChild(tagElem);
    });
}

tagInput.addEventListener('keydown', (e) => {
    if (e.key === ',' || e.key === 'Enter') {
        e.preventDefault();
        addTag(tagInput.value);
    }
    if (e.key === 'Backspace' && tagInput.value === '' && tags.size > 0) {
        const lastTag = Array.from(tags).pop();
        removeTag(lastTag);
    }
});

// 3. 유료/무료 설정 UI
const radioButtons = document.querySelectorAll('input[name="content-type"]');
const priceWrapper = document.getElementById('priceWrapper');
const priceInput = document.getElementById('priceInput');

// [추가] 페이지 로드 시 유료 체크 상태라면 가격창 활성화 (수정 모드 대비)
if(document.querySelector('input[name="content-type"]:checked').value === 'paid') {
    priceWrapper.classList.add('active');
    priceInput.disabled = false;
}

radioButtons.forEach(radio => {
    radio.addEventListener('change', (e) => {
        if (e.target.value === 'paid') {
            priceWrapper.classList.add('active');
            priceInput.disabled = false;
            priceInput.focus();
        } else {
            priceWrapper.classList.remove('active');
            priceInput.disabled = true;
            priceInput.value = '';
        }
    });
});

priceInput.addEventListener('input', function () {
    if (this.value < 0) this.value = 0;
});

// 4. 모달 제어 (생략 가능, 기존 유지)
const modal = document.getElementById('guidelineModal');
const btnOpenModal = document.getElementById('btnOpenModal');
const btnCloseModal = document.querySelector('.close-modal');

if (btnOpenModal) {
    btnOpenModal.addEventListener('click', (e) => {
        e.preventDefault();
        modal.classList.add('show');
    });
    btnCloseModal.addEventListener('click', () => modal.classList.remove('show'));
    modal.addEventListener('click', (e) => {
        if (e.target === modal) modal.classList.remove('show');
    });
}

// 5. 저장 버튼 이벤트 연결 (중복 방지를 위해 하나만 남김)
const btnSave = document.getElementById('btnSave');
if(btnSave) {
    btnSave.addEventListener('click', () => savePost(false));
}

document.querySelector('.btn-draft').addEventListener('click', () => savePost(true));
document.querySelector('.btn-exit').addEventListener('click', () => {
    if (confirm('작성중인 글이 사라집니다. 정말 나가시겠습니까?')) history.back();
});

// ============================================================
// [핵심] 게시글 저장 함수 (데이터 이름 수정)
// ============================================================
let isSaving = false;

function savePost(isTemp) {
    if(isSaving){
        return;
    }
    
    const boardNo = document.getElementById('boardNo').value; 
    const title = document.querySelector('.input-title').value.trim();
    const content = editor.getHTML().trim();
    const isPaidChecked = document.querySelector('input[name="content-type"]:checked').value === 'paid';

    // 썸네일 URL 값 가져오기
    const thumbnailUrl = document.getElementById('thumbnailUrl').value;

    if (!title) {
        alert('제목을 입력해주세요.');
        return document.querySelector('.input-title').focus();
    }
    if (content.length === 0) {
        return alert('내용을 입력해주세요.');
    }
    if (isPaidChecked && (!priceInput.value || priceInput.value == 0)) {
        alert('유료 콘텐츠의 가격을 설정해주세요.');
        return priceInput.focus();
    }

    isSaving = true;

    // [수정 3] 백엔드가 스네이크 케이스(board_no)를 원할 경우를 대비해
    // 데이터를 보낼 때 변수명을 DB 컬럼 스타일로 맞춰줍니다.
    const postData = {
        // 수정일 때는 board_no 값 넣기, 아닐 때는 null
        "board_no": boardNo ? parseInt(boardNo) : null,
        "board_title": title,
        "board_content": content,
        "thumbnail_url": thumbnailUrl,
        "tag_list": Array.from(tags),
        "is_paid": isPaidChecked ? "Y" : "N",
        "price": isPaidChecked ? parseInt(priceInput.value) : 0,
        "temp_fl": isTemp ? "Y" : "N"
    };

    console.log('전송 데이터(수정됨):', postData);

    let url = '/api/blog/write';
    let method = 'POST';
    
    // 수정 모드일 때
    if (boardNo) {
        url = '/api/blog/update'; 
    }

    fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(postData),
    })
    .then(response => {
        if (response.ok) {
            alert(boardNo ? '수정되었습니다.' : (isTemp ? '임시저장 되었습니다.' : '발행되었습니다!'));
            location.href = boardNo ? `/blog/detail/${boardNo}` : "/blog/list";
        } else {
            // 서버 에러 메시지를 확인하기 위해 text()로 읽음
            return response.text().then(text => { throw new Error(text) });
        }
    })
    .catch(error => {
        console.error('Save Error:', error);
        alert('저장 실패: ' + error.message);
    })
    .finally(() => {
        // 3. [저장 끝] 성공하든 실패하든 깃발 뽑기 (잠금 해제)
        isSaving = false;
    });
}