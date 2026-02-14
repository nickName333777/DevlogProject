import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
from selenium import webdriver
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import time
import re
import oracledb

# 드라이버 설정
options = webdriver.ChromeOptions()
options.add_argument('--disable-gpu')
driver = webdriver.Chrome(options=options)

try:
    
    driver.get('https://www.jobkorea.co.kr/Theme/it_developer')
    wait = WebDriverWait(driver, 10)
    
    # 수집된 전체 공고를 담을 리스트
    temp_list = []

    # 오늘 등록(rlistTab27), 오늘 마감(rlistTab26) 탭 순회
    btn_ids = ["rlistTab27", "rlistTab26"]

    for btn_id in btn_ids:
        print(f"탭 클릭 중: {btn_id}")
        btn = wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, f"label[for='{btn_id}']")))
        btn.click()
        time.sleep(3) # 리스트 로딩 대기

        # 해당 탭의 목록 수집
        job_elements = driver.find_elements(By.XPATH, "//tr[descendant::div[@class='titBx']]")
        
        for row in job_elements:
            try:
                title_tag = row.find_element(By.CSS_SELECTOR, ".titBx strong a")
                comp_name = row.find_element(By.CSS_SELECTOR, ".tplCo a").text.strip()
                post_title = title_tag.text.strip()
                detail_url = title_tag.get_attribute('href')

                # 중복 수집 방지 (URL 기준)
                if not any(item['DETAIL_URL'] == detail_url for item in temp_list):
                    temp_list.append({
                        'COMPANY_NAME': comp_name,
                        'POSTING_TITLE': post_title,
                        'DETAIL_URL': detail_url
                    })
            except: 
                continue
    final_db_data = []
    print(f"총 {len(temp_list)}개의 공고를 발견했습니다. 상세 수집을 시작합니다.")

   # 상세 요강 내부
    for i, job in enumerate(temp_list, 1):
        print(f"[{i}/{len(temp_list)}] {job['COMPANY_NAME']} 수집 중...")
        driver.get(job['DETAIL_URL'])
        time.sleep(4)

        image_urls = []
         
        #이미지 찾는다.
        def collect_images_current_context():
            imgs = driver.find_elements(By.TAG_NAME, "img")
            for img in imgs:
                try:
                    # src, data-original, data-src 중 하나라도 있으면 가져옴
                    src = img.get_attribute("src") or img.get_attribute("data-original") or img.get_attribute("data-src")
                    if src:
                        if src.startswith("//"): src = "https:" + src
                        
                        #  file 서버 주소이면서 실제 공고 본문 이미지 패턴인 것만!
                        if "file" in src and "jobkorea.co.kr" in src:
                            if any(x in src for x in ["/Mng/", "/GI/", "/Gen/", "/2025/", "jobimg"]):
                                if "Co_Logo" not in src and "icon" not in src.lower() and src not in image_urls:
                                    image_urls.append(src)
                except: continue

        # 메인 페이지에 이미지가 노출되어 있는 경우를 위해 먼저 실행
        collect_images_current_context()

        # 페이지 내의 모든 iframe을 찾아서 리스트로 만듦
        iframes = driver.find_elements(By.TAG_NAME, "iframe")
        
        # 발견된 모든 iframe을 하나씩 순회
        for idx, frame in enumerate(iframes):
            try:
                driver.switch_to.frame(frame) # i번째 프레임으로 진입
                time.sleep(1)                 
                collect_images_current_context() # 프레임 안에서 이미지 수집 함수 실행
                driver.switch_to.default_content() # 작업 후 메인 페이지로 복귀
            except:
                driver.switch_to.default_content() # 에러 나도 메인으로 복귀
                continue
        full_text = driver.find_element(By.TAG_NAME, "body").text
        
        # 이미지 없으면 표시해주는 거
        if not image_urls:
            print("이미지 없음")
            driver.switch_to.default_content()

        def get_val(key, default="정보없음"):
            if key in full_text:
                try: return full_text.split(key)[1].split('\n')[1].strip()
                except: return default
            return default

        dates = re.findall(r'\d{4}\.\d{2}\.\d{2}', full_text)
        
        # 데이터 묶기
        record = {
            'POSTING_TITLE': job['POSTING_TITLE'][:100],
            'POSTING_CONTENT': "\n".join(image_urls),
            'REC_FIELD': get_val("모집분야")[:4000],
            'REC_COUNT': get_val("모집인원")[:30],
            'EMP_TYPE': get_val("고용형태")[:100],
            'SALARY': get_val("급여")[:100],
            'REQ_CAREER': get_val("경력")[:100],
            'REQ_EDUCATION': get_val("학력")[:100],
            'APPLY_START': dates[0] if len(dates) >= 1 else time.strftime('%Y.%m.%d'),
            'APPLY_END': dates[1] if len(dates) >= 2 else "채용시 마감",
            'APPLY_METHOD': get_val("접수방법")[:30],
            'COMPANY_NAME': job['COMPANY_NAME'][:100],
            'WORK_ADDR': get_val("근무지주소")[:100] if "근무지주소" in full_text else get_val("지역")[:100],
            'NEARBY_SUB': get_val("인근지하철")[:500]
        }
        final_db_data.append(record)
        driver.switch_to.default_content()

except Exception as e:
    print(f"크롤링 오류: {e}")
finally:
    driver.quit()

# Oracle DB 저장
if final_db_data:
    try:
        # DB 연결
        conn = oracledb.connect(user="devlog", password="devlog1234", dsn="localhost:1521/xe")
        cursor = conn.cursor()
        print("\n[DB 처리 시작]")

        for data in final_db_data:
            # 회사 존재 여부 확인 및 번호 결정
            cursor.execute("SELECT COMPANY_CODE FROM COMPANY_CODE WHERE COMPANY_NAME = :1", [data['COMPANY_NAME']])
            row = cursor.fetchone()
            
            if row:
                com_code = row[0]
                print(f"   > 기존 회사 발견: {data['COMPANY_NAME']} (ID: {com_code})")
            else:
                # 새 회사 번호 생성
                cursor.execute("SELECT NVL(MAX(COMPANY_CODE), 0) + 1 FROM COMPANY_CODE")
                com_code = cursor.fetchone()[0]
                
                cursor.execute("""
                    INSERT INTO COMPANY_CODE (COMPANY_CODE, COMPANY_NAME, WORK_ADDR, NEARBY_SUB) 
                    VALUES (:1, :2, :3, :4)
                """, [com_code, data['COMPANY_NAME'], data['WORK_ADDR'], data['NEARBY_SUB']])
                print(f"   > 새 회사 등록: {data['COMPANY_NAME']} (ID: {com_code})")
            

            # 중복 공고 스킵
            cursor.execute("""
                SELECT 1 
                FROM JOB_POSTING 
                WHERE COMPANY_CODE = :1 
                AND POSTING_TITLE = :2
            """, [com_code, data['POSTING_TITLE']])

            if cursor.fetchone():
                print(f"     -> 중복 공고 스킵: {data['POSTING_TITLE']}")
                continue

            # 공고 번호 생성
            cursor.execute("SELECT NVL(MAX(POSTING_NO), 0) + 1 FROM JOB_POSTING")
            post_no = cursor.fetchone()[0]

            # 공고 정보 삽입
            # setinputsizes: 3번째 매개변수(:3)인 POSTING_CONTENT를 CLOB으로 지정
            cursor.setinputsizes(None, None, oracledb.CLOB)
            
            sql_post = """
                INSERT INTO JOB_POSTING (
                    POSTING_NO, POSTING_TITLE, POSTING_CONTENT, REC_FIELD, REC_COUNT, 
                    EMP_TYPE, SALARY, REQ_CAREER, REQ_EDUCATION, 
                    APPLY_START, APPLY_END, APPLY_METHOD, COMPANY_CODE
                ) VALUES (:1, :2, :3, :4, :5, :6, :7, :8, :9, :10, :11, :12, :13)
            """
            
            cursor.execute(sql_post, [
                post_no, data['POSTING_TITLE'], data['POSTING_CONTENT'], data['REC_FIELD'],
                data['REC_COUNT'], data['EMP_TYPE'], data['SALARY'], data['REQ_CAREER'],
                data['REQ_EDUCATION'], data['APPLY_START'], data['APPLY_END'],
                data['APPLY_METHOD'], com_code
            ])
            print(f"     -> 공고 저장 완료 (No: {post_no})")
        
        conn.commit()
        print(f"\n[최종 결과] 성공적으로 {len(final_db_data)}건의 데이터가 저장되었습니다.")
        
    except Exception as e:
        print(f"\n[DB 저장 오류] 상세 내용: {e}")
        if 'conn' in locals(): conn.rollback()
    finally:
        if 'conn' in locals(): conn.close()