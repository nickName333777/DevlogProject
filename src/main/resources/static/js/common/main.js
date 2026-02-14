/* 오늘의 인기글 */
const postGrid = document.getElementById("postGrid");

fetch("/api/blog/list?page=0&size=6&sort=like")
  .then((res) => res.json())
  .then((data) => renderPopularPosts(data.content));

function renderPopularPosts(posts) {
  postGrid.innerHTML = "";

  posts.forEach((post) => {
    const card = `
      <div class="post-card"
           onclick="location.href='/blog/detail/${post.board_no}'"
           style="cursor:pointer">
        <div class="post-img">
          <img src="${post.thumbnail_url ?? "/images/logo.png"}">
        </div>
        <div class="post-info">
          <div class="post-top">
            <div class="likes">
              <img src="/images/common/like.png">
              <span>${post.like_count ?? 0}</span>
            </div>
            <span class="writer">Writer. ${
              post.member_nickname ?? "익명"
            }</span>
          </div>
          <p class="post-preview">${post.board_title}</p>
        </div>
      </div>
    `;
    postGrid.insertAdjacentHTML("beforeend", card);
  });
}

/* 최신 피드  */
const latestFeedGrid = document.getElementById("latestFeedGrid");

fetch("/api/blog/list?page=0&size=6")
  .then((res) => res.json())
  .then((data) => renderLatestFeed(data.content))
  .catch(console.error);

function renderLatestFeed(posts) {
  latestFeedGrid.innerHTML = "";

  posts.forEach((post) => {
    const preview = post.board_content
      ? post.board_content.replace(/<[^>]*>/g, "").slice(0, 60)
      : "";

    const card = `
      <div class="post-card"
           onclick="location.href='/blog/detail/${post.board_no}'"
           style="cursor:pointer">
        <div class="post-img">
          <img src="${post.thumbnail_url ?? "/images/logo.png"}">
        </div>
        <div class="post-info">
          <div class="post-top">
            <div class="likes">
              <img src="/images/common/like.png">
              <span>${post.like_count ?? 0}</span>
            </div>
            <span class="writer">Writer. ${
              post.member_nickname ?? "익명"
            }</span>
          </div>
          <p class="post-preview">${preview}</p>
        </div>
      </div>
    `;
    latestFeedGrid.insertAdjacentHTML("beforeend", card);
  });
}

/* 지식 뉴스는 컨트롤러 통해서 메인에서 타임리프로 불러옴 */

const cafes = [
  {
    name: "콘티뉴이티",
    address: "서울 중구 수표로 30 OK빌딩 3층",
    phone: "0507-1359-5675",
    hours: "영업시간 11:30 - 22:00",
    image: "cafe1.jpeg",
  },
  {
    name: "리프카페",
    address: "서울 종로구 대학로 8길 3",
    phone: "02-333-4444",
    hours: "영업시간 11:00 - 21:30",
    image: "cafe1.jpeg",
  },
  {
    name: "브루잉랩",
    address: "서울 서대문구 연희로 7길 10",
    phone: "02-555-9999",
    hours: "영업시간 09:00 - 22:30",
    image: "cafe1.jpeg",
  },
  {
    name: "카페 테라스",
    address: "서울 마포구 양화로 162",
    phone: "02-123-4567",
    hours: "영업시간 10:00 - 21:00",
    image: "cafe1.jpeg",
  },
  {
    name: "커피앤코드",
    address: "서울 성수동 12-3",
    phone: "02-222-1234",
    hours: "영업시간 11:00 - 23:00",
    image: "cafe1.jpeg",
  },
  {
    name: "하루디저트",
    address: "서울 강남구 논현로 55길 12",
    phone: "0507-1122-3344",
    hours: "영업시간 09:30 - 22:00",
    image: "cafe6.jpg",
  },
];

const cafeList = document.getElementById("cafeList");

cafes.forEach((cafe) => {
  const item = `
    <div class="cafe-item">
      <div class="cafe-thumb">
        <img src="${cafe.image}" alt="${cafe.name}" />
      </div>
      <div class="cafe-info">
        <h3 class="cafe-name">${cafe.name}</h3>
        <p class="cafe-address">${cafe.address}</p>
        <p class="cafe-phone">전화번호 ${cafe.phone}</p>
        <p class="cafe-hours">${cafe.hours}</p>
        <button class="cafe-btn">
          <img src="map-icon.svg" alt="길찾기 아이콘" /> 길찾기
        </button>
      </div>
    </div>
  `;
  cafeList.insertAdjacentHTML("beforeend", item);
});

//########################## 현재 활동 ###########################

let stompClient = null;

function connectOnlineStatus() {
  const socket = new SockJS("/ws");
  stompClient = Stomp.over(socket);

  stompClient.connect({}, () => {
    console.log("웹소켓 연결 성공");

    // 구독을 먼저 함
    stompClient.subscribe("/topic/online/" + memberNo, updateFriendsUI);
    stompClient.send(
      "/online/requestOnline",
      {},
      JSON.stringify({
        memberNo: memberNo,
      })
    );
  });
}

// 로그아웃
window.addEventListener("beforeunload", () => {
  if (stompClient && stompClient.connected) {
    stompClient.disconnect();
  }
});

// 화면에 친구 목록
function updateFriendsUI(payload) {
  const onlinelist = JSON.parse(payload.body);
  console.log(onlinelist);
  const friendsList = document.getElementById("friendsList");
  if (!friendsList) return;

  friendsList.innerHTML = "";

  const myEmail = document.getElementById("loginUserEmail")?.value || "";

  if (onlinelist.length === 0) {
    friendsList.innerHTML =
      "<p style='font-size:12px; color:#999;'>현재 활동중인 친구가 없어요.</p>";
    return;
  }

  onlinelist.forEach((user) => {
    //
    // 이메일 나중에 추가
    const profileUrl = `/blog/${user.member_email}`;
    const friendHtml = `
        <div class="friend active" title="${user.member_nickname}"
        onclick ="location.href='${profileUrl}'"
        style = "cursor: pointer;">
          <img src="${user.profile}" alt="${user.member_nickname}">
          <span class="friend-name" style="font-size:11px; display:block; text-align:center; margin-top:4px;">
            ${user.member_nickname}
          </span>
        </div>
      `;
    friendsList.insertAdjacentHTML("beforeend", friendHtml);
  });
}

document.addEventListener("DOMContentLoaded", function () {
  connectOnlineStatus();
});
