let selectedPaymentData = { id: null, no: null };

document.addEventListener("DOMContentLoaded", () => {
  // 요소 선택
  const chargeBtn = document.getElementById("charge-btn");
  const exchangeBtn = document.getElementById("exchange-btn");
  const chargeModal = document.getElementById("charge-modal");
  const exchangeModal = document.getElementById("exchange-modal");
  const completeModal = document.getElementById("exchange-complete-modal");
  const submitExchangeBtn = document.querySelector(".exchange-submit-btn");
  const completeConfirmBtn = document.getElementById("complete-confirm-btn");

  const cancelModal = document.getElementById("cancel-modal");
  const cancelSuccessModal = document.getElementById("cancel-success-modal");
  const cancelFailModal = document.getElementById("cancel-fail-modal");
  const submitCancelBtn = document.querySelector(".cancel-submit-btn");
  const successConfirmBtn = document.getElementById("cancel-success-confirm");
  const failBackBtn = document.getElementById("cancel-fail-back");

  const amountButtons = document.querySelectorAll(".amount-options button");
  const amountInput = document.querySelector(".input-wrapper input");
  const filterButtons = document.querySelectorAll(".filter-btn");
  const historyRows = document.querySelectorAll("#history-tbody tr");
  // const amountInput = document.getElementById("charge-amount-input");
  const finalChargeSubmit = document.getElementById("final-charge-submit");
  fetchBankList();
  // 모달 열기 제어
  chargeBtn?.addEventListener(
    "click",
    () => (chargeModal.style.display = "flex")
  );

  exchangeBtn?.addEventListener("click", () => {
    exchangeModal.style.display = "flex";

    // 보유 콩 텍스트에서 숫자만 추출
    const currentBeansText = document.querySelector(".mybeans-val").innerText;
    const amount = parseInt(currentBeansText.replace(/[^0-9]/g, ""));

    if (isNaN(amount) || amount === 0) {
      document.getElementById("calcOrigin").innerText = "0 콩";
      document.getElementById("calcFee").innerText = "-0 콩";
      document.getElementById("calcFinal").innerText = "0 원";
      return;
    }

    // 10% 수수료 계산
    const fee = Math.floor(amount * 0.1);
    const finalAmount = amount - fee;

    // 모달 내 계산 영역 업데이트
    document.getElementById("calcOrigin").innerText =
      amount.toLocaleString() + " 콩";
    document.getElementById("calcFee").innerText =
      "-" + fee.toLocaleString() + " 콩";
    document.getElementById("calcFinal").innerText =
      finalAmount.toLocaleString() + " 원";
  });

  // historyRows를 돌면서 클릭 이벤트 설정
  historyRows.forEach((row) => {
    const rowType = row.getAttribute("data-type"); // 충전(+) 내역만 클릭 가능하게

    if (rowType === 'charge'){
      row.style.cursor = "pointer";
      // 마우스 올렸을 때: 언더라인 추가
      row.addEventListener("mouseenter", () => {
        row.style.textDecoration = "underline";
      });

      //  마우스 뗐을 때: 언더라인 제거
      row.addEventListener("mouseleave", () => {
        row.style.textDecoration = "none";
      });
      row?.addEventListener("click", () => {
      // 행에 심어진 데이터 가져오기
      const pId = row.getAttribute("data-id");
      const pNo = row.getAttribute("data-no");
      const pPrice = row.getAttribute("data-price");
      const used = parseInt(row.getAttribute("data-used") || 0);

      // 이미 사용했다면 실패 모달
      if (used > 0) {
        document.getElementById("cancel-fail-modal").style.display = "flex";
        return;
      }

      // 전역 바구니에 저장 (취소 버튼 클릭 시 사용)
      selectedPaymentData.id = pId;
      selectedPaymentData.no = pNo;

      // 취소 모달 내부 텍스트 변경
      const beansVal = document.querySelector("#cancel-modal .val");
      const priceVal = document.querySelector("#cancel-modal .val.minus");
      if (beansVal) beansVal.innerText = Number(pPrice).toLocaleString() + "콩";
      if (priceVal)
        priceVal.innerText = "-" + Number(pPrice).toLocaleString() + "원";

      cancelModal.style.display = "flex";
    });
  }
});

  // 환전 및 취소 완료 처리
  submitExchangeBtn?.addEventListener("click", () => {
    // 데이터 수집
    const beansText = document.querySelector(".mybeans-val").innerText;
    const amount = parseInt(beansText.replace(/[^0-9]/g, "")); // "12,450 콩" -> 12450
    const returnBank = document.querySelector("#returnBank").value;
    const exchangeAccount = document.querySelector("#exchangeAccount").value;
    const exchangeHolder = document.querySelector("#exchangeHolder").value;
    console.log("보내는 금액:", amount);

    if (!returnBank || !exchangeAccount || !exchangeHolder) {
      alert("은행, 계좌번호, 예금주를 모두 입력해주세요.");
      return;
    }

    const data = {
      requestAmount: amount,
      returnBank: returnBank,
      exchangeAccount: exchangeAccount,
      exchangeHolder: exchangeHolder,
    };

    fetch("/payment/exchange", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    })
      .then((resp) => resp.json())
      .then((result) => {
        if (result > 0) {
          // 성공 시 모달 전환
          exchangeModal.style.display = "none";
          completeModal.style.display = "flex";

          // 확인 버튼 누르면 새로고침
          // completeConfirmBtn.onclick = () => location.reload();
        } else if (result === -2) {
          alert("보유하신 커피콩 잔액이 부족합니다.");
        } else if (result === -3) {
          alert("최소 5,000콩 이상부터 환전 가능합니다.");
        } else {
          alert("환전 신청에 실패했습니다.");
        }
      })
      .catch((err) => {
        console.error("환전 에러:", err);
        alert("서버 통신 중 오류가 발생했습니다.");
      });
  });

  // 모달 닫기 제어
  [completeConfirmBtn, successConfirmBtn, failBackBtn].forEach((btn) => {
    btn?.addEventListener("click", () => {
      [completeModal, cancelSuccessModal, cancelFailModal].forEach(
        (m) => (m.style.display = "none")
      );
    });
  });

  window.addEventListener("click", (e) => {
    const allModals = [
      chargeModal,
      exchangeModal,
      completeModal,
      cancelModal,
      cancelSuccessModal,
      cancelFailModal,
    ];
    allModals.forEach((m) => {
      if (e.target === m) m.style.display = "none";
    });
  });

  // 내역 필터링
  filterButtons.forEach((btn) => {
    btn.addEventListener("click", () => {
      const type = btn.dataset.type; // 'all', 'charge', 'use', etc.

      filterButtons.forEach((b) => b.classList.remove("active"));
      btn.classList.add("active");

      historyRows.forEach((row) => {
        const rowType = row.dataset.type; // tr에 심어진 data-type 값

        if (type === "all") {
          // '전체' 탭은 무조건 모든 행을 보여줌
          row.style.display = "";
        } else if (type === "use") {
          // '사용' 탭은 use, POST, SUBSCRIBE 세 가지를 모두 포함
          if (
            rowType === "use" ||
            rowType === "POST" ||
            rowType === "SUBSCRIBE"
          ) {
            row.style.display = "";
          } else {
            row.style.display = "none";
          }
        } else {
          // 그 외(charge, exchange 등)는 타입이 정확히 일치할 때만 보여줌
          row.style.display = rowType === type ? "" : "none";
        }
      });
    });
  });

  //  금액 옵션 버튼 클릭 시 input 값 변경
  amountButtons.forEach((btn) => {
    btn.addEventListener("click", () => {
      amountButtons.forEach((b) => b.classList.remove("selected"));
      btn.classList.add("selected");

      // 금액 쉼표 제거 후 input에 대입
      const val = btn.textContent.replace(/,/g, "");
      amountInput.value = val;
    });
  });

  // 포트원 결제 실행
  finalChargeSubmit?.addEventListener("click", async () => {
    const amount = parseInt(amountInput.value.replace(/[^0-9]/g, ""));

    if (isNaN(amount) || amount < 100) {
      alert("최소 충전 금액은 100원입니다.");
      return;
    }

    try {
      const response = await PortOne.requestPayment({
        storeId: portoneConfig.storeId,
        channelKey: portoneConfig.channelKey,
        paymentId: `pay-${crypto.randomUUID().split("-")[0]}`,
        orderName: "커피콩 충전",
        totalAmount: amount,
        currency: "CURRENCY_KRW",
        payMethod: "CARD",
      });
      console.log(response);
      // 결제 실패 시
      if (response.code !== undefined) {
        alert(`결제 실패: ${response.message}`);
        return;
      }

      // 저장
      const serverResponse = await fetch("/payment/complete", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          paymentId: response.paymentId, // 예시 : 'pay-c6329ad7'
          price: amount,
          payMethod: "CARD", // 일단 상수로 테스트
          usedAmount: 0,
          payStatus: "1", //1:충전
        }),
      });

      if (serverResponse.ok) {
        alert("충전이 완료되었습니다!");
        location.reload();
      } else {
        alert("DB 저장 중 오류가 발생했습니다.");
      }
    } catch (e) {
      console.error(e);
      alert("결제 요청 중 알 수 없는 오류가 발생했습니다.");
    }
  });

  // 결제 취소 실행 버튼
  submitCancelBtn?.addEventListener("click", async () => {
    // 필요한 파라미터 준비 (취소 모달을 열 때 미리 저장해둔 값)
    const paymentId = selectedPaymentData.id; // 예: pay-a08199f6
    const beansPayNo = selectedPaymentData.no; // DB PK

    const currentBeansVal = parseInt(document.querySelector(".mybeans-val").innerText.replace(/[^0-9]/g, ""));
    const cancelAmount = parseInt(document.querySelector("#cancel-modal .val").innerText.replace(/[^0-9]/g, ""));
    if (currentBeansVal < cancelAmount) {
      alert("보유하신 커피콩이 취소하려는 금액보다 적습니다. 충전 후 이미 사용하신 경우 결제 취소가 불가능합니다.");
      return;
    }

    if (!paymentId || !beansPayNo) {
      alert("취소할 결제 정보를 찾을 수 없습니다.");
      return;
    }

    submitCancelBtn.disabled = true;
    submitCancelBtn.innerText = "취소 요청 중...";

    try {
      const response = await fetch("/payment/cancel", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          paymentId: paymentId, // 포트원 결제 번호
          beansPayNo: beansPayNo, //  DB 결제 번호
          reason: "고객 요청에 의한 환불", // 사유
        }),
      });

      if (response.ok) {
        // 성공 시: 성공 모달 띄우기
        cancelModal.style.display = "none";
        document.getElementById("cancel-success-modal").style.display = "flex";

        // '확인' 누르면 새로고침해서 잔액 갱신
        document.getElementById("cancel-success-confirm").onclick = () => {
          location.reload();
        };
      } else {
        // 실패 시: 서버에서 보낸 에러 메시지 출력
        const errorText = await response.text();
        alert("취소 실패: " + errorText);
        cancelModal.style.display = "none";
      }
    } catch (error) {
      console.error("통신 에러:", error);
      alert("서버와 통신 중 오류가 발생했습니다.");
    } finally {
      submitCancelBtn.disabled = false;
      submitCancelBtn.innerText = "결제 취소하기";
    }
  });

  // 은행 목록 가져오기 함수
  function fetchBankList() {
    fetch("/payment/bankList")
      .then((resp) => resp.json())
      .then((bankList) => {
        const select = document.querySelector("#returnBank");

        bankList.forEach((bank) => {
          const opt = document.createElement("option");
          opt.value = bank.bankCode; // 서버로 보낼 값 (예: 088)
          opt.innerText = bank.bankName; // 화면에 보일 이름 (예: 신한은행)
          select.appendChild(opt);
        });
      })
      .catch((err) => console.error("은행 목록 로드 실패:", err));
  }
});
