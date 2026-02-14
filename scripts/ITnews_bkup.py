import requests
from bs4 import BeautifulSoup
import json
import time
import oracledb

CATEGORIES = {
    "AI": {"code": 21, "url": "https://news.daum.net/ai-tech"},
    "테크": {"code": 22, "url": "https://news.daum.net/technology"},
    "IT기업": {"code": 23, "url": "https://news.daum.net/it-tech"},
    "게임": {"code": 24, "url": "https://news.daum.net/games"},
    "과학": {"code": 25, "url": "https://news.daum.net/science"},
    "우주": {"code": 26, "url": "https://news.daum.net/universe"}
}

# 전체 중복 체크를 위한 세트 (제목 기준)
seen_titles = set()

def get_article_details(url, board_code):
    try:
        header = {"User-Agent":"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"}
        response = requests.get(url, headers=header, timeout=10)
        soup = BeautifulSoup(response.text, "html.parser")
        
        title_tag = soup.select_one('h3.tit_view')
        if not title_tag: return None
        title = title_tag.get_text(strip=True)

        # 이미 수집된 제목이면 스킵 (중복 방지 핵심)
        if title in seen_titles:
            return "duplicate"

        reporter = soup.find("span", class_="txt_info").get_text(strip=True) if soup.find("span", class_="txt_info") else "IT뉴스"
        num_date = soup.find("span", class_="num_date").get_text(strip=True) if soup.find("span", class_="num_date") else ""
        
        figure = soup.find("figure", class_="origin_fig")
        image_url = figure.find("img")["src"] if figure and figure.find("img") else "이미지 없음"

        content_view = soup.select_one('.article_view')
        if not content_view: return None
        paragraphs = [p.get_text(strip=True) for p in content_view.select('p') if p.get_text(strip=True)]

        seen_titles.add(title) # 새로운 제목 저장
        return {
            "BOARD_CODE": board_code,
            "BOARD_TITLE": title,
            "NEWS_REPORTER": reporter,
            "B_CREATE_DATE": num_date,
            "BOARD_CONTENT": "\n".join(paragraphs),
            "IMAGE_URL": image_url,
            "BOARD_DEL_FL": 'N',
            "MEMBER_NO": 1
        }
    except Exception:
        return None

final_list = []

for name, info in CATEGORIES.items():
    print(f"--- {name} 분석 시작 ---")
    header = {"User-Agent":"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"}
    
    try:
        res = requests.get(info["url"], headers=header, timeout=10)
        soup = BeautifulSoup(res.text, "html.parser")
        
        main_sections = soup.select('.item_newsthumb2, .item_mainnews, .cont_thumb')
        category_urls = []
        
        # 링크 추출 (URL 중복 방지)
        all_links = []
        for section in main_sections:
            all_links.extend(section.find_all('a', href=True))
        all_links.extend(soup.find_all('a', href=True))

        for a in all_links:
            href = a['href']
            url = "https://v.daum.net" + href.split('?')[0] if href.startswith("/v/") else (href.split('?')[0] if "v.daum.net/v/" in href else "")
            
            if url and url not in category_urls and any(c.isdigit() for c in url):
                category_urls.append(url)
            
            if len(category_urls) >= 10: # 중복 대비 넉넉히 후보 추출
                break

        # 실제 기사 내용 수집 (내용/제목 중복 체크)
        count = 0
        for url in category_urls:
            data = get_article_details(url, info["code"])
            
            if data == "duplicate": # 제목 중복 시 통과
                continue
            
            if data:
                final_list.append(data)
                print(f"수집 성공: {data['BOARD_TITLE'][:20]}...")
                count += 1
            
            if count == 2: # 카테고리당 4개 채우면 종료
                break
            time.sleep(0.3)
            
    except Exception as e:
        print(f"[!] 에러: {e}")

# ============================================================================

# --- Oracle DB 저장 부분 ---
if final_list:
    conn = None
    try:
        # DB 연결 
        conn = oracledb.connect(user="devlog", password="devlog1234", dsn="localhost:1521/xe")
        
        cursor = conn.cursor() 
        print("\n[DB 처리 시작]")

        for data in final_list:
            #  중복 확인
            cursor.execute("SELECT COUNT(*) FROM BOARD WHERE BOARD_TITLE = :1", [data['BOARD_TITLE']])
            if cursor.fetchone()[0] > 0:
                print(f"   > 중복 스킵: {data['BOARD_TITLE'][:15]}...")
                continue

            #  새 게시글 번호 생성
            cursor.execute("SELECT NVL(MAX(BOARD_NO), 0) + 1 FROM BOARD")
            board_no = cursor.fetchone()[0]

            # BOARD 테이블 저장 
            cursor.setinputsizes(None, None, None, oracledb.CLOB, None, None, None)
            
            sql_board = """
                INSERT INTO BOARD (
                    BOARD_NO, BOARD_TITLE, BOARD_CODE, BOARD_CONTENT, 
                    NEWS_REPORTER, B_CREATE_DATE, BOARD_DEL_FL, MEMBER_NO
                ) VALUES (:1, :2, :3, :4, :5, SYSDATE, :6, :7)
            """
            
            # 데이터 리스트 삽입/ 날자 제외
            cursor.execute(sql_board, [
                board_no,              # :1
                data['BOARD_TITLE'],    # :2
                data['BOARD_CODE'],     # :3
                data['BOARD_CONTENT'],  # :4
                data['NEWS_REPORTER'],  # :5
                data['BOARD_DEL_FL'],   # :6
                2    # :7
            ])

            # 이미지 테이블 저장
            if data['IMAGE_URL'] and data['IMAGE_URL'] != "이미지 없음":
                cursor.execute("SELECT NVL(MAX(IMG_NO), 0) + 1 FROM BOARD_IMG")
                img_no = cursor.fetchone()[0]

                sql_img = """
                    INSERT INTO BOARD_IMG (
                        IMG_NO, IMG_PATH, IMG_ORIG, IMG_RENAME, IMG_ORDER, BOARD_NO
                    ) VALUES (:1, :2, :3, :4, :5, :6)
                """
                cursor.execute(sql_img, [
                    img_no, data['IMAGE_URL'], "daum_news", 
                    f"news_{img_no}.jpg", 0, board_no
                ])
                print(f"저장 완료: {board_no} (이미지 포함)")
            else:
                print(f"저장 완료: {board_no} (이미지 없음)")

        # 모든 작업 성공 시 커밋
        conn.commit()
        print(f"\n[최종 완료] 총 {len(final_list)}건 저장 성공")

    except Exception as e:
        print(f"\n[DB 오류] 상세 내용: {e}")
        if conn:
            conn.rollback()
    finally:
        if 'cursor' in locals() and cursor:
            cursor.close()
        if conn:
            conn.close()