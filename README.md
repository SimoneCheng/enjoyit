# Enjoyit - 揪團小幫手 Web 應用程式

這是一個基於 OOAD (物件導向分析與設計) 作業所開發的網頁應用程式，旨在幫助主揪快速建立團購群組，並方便成員參與點餐。

## 開發環境需求

- **Java**: JDK 21 或更高版本
- **Build Tool**: Maven (專案內含 `mvnw` Wrapper，不須額外安裝)
- **IDE**: 推薦使用 IntelliJ IDEA (已包含一鍵啟動設定檔)

## 專案架構說明

本專案採用典型的 Spring Boot 分層架構，適合進行 OOAD 類別圖 (Class Diagram) 與循序圖 (Sequence Diagram) 的實作：

- `com.enjoyit.controller`: 負責處理 HTTP 請求與 API 端點。
- `com.enjoyit.service`: 負責核心業務邏輯。
- `com.enjoyit.repository`: 負責資料存取（目前使用記憶體儲存 `InMemoryGroupRepository`）。
- `com.enjoyit.domain`: 核心領域模型（Domain Model）。
- `com.enjoyit.dto`: 資料傳輸物件，用於請求校驗。
- `src/main/resources/static`: 前端網頁檔案（Vanilla JS + CSS）。

## 如何啟動專案

### 1. 使用 IntelliJ IDEA (推薦)

1. 使用 IntelliJ IDEA 開啟此專案目錄。
2. 等待 Maven 載入相依套件。
3. 在右上角的執行設定下拉選單中，選擇 **`EnjoyitApplication`** 並點選綠色的執行按鈕 (Run)。
4. 啟動後，開啟瀏覽器並訪問：`http://localhost:8080`

### 2. 使用終端機 (Terminal)

在專案根目錄下輸入以下指令：

```bash
# MacOS / Linux
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

## 目前實作進度

UC-01. **主揪註冊群組**:
   - 帳號密碼輸入。
   - 自動過濾字串前後空白。
   - 密碼長度校驗（至少 8 碼）。
   - 帳號重複檢查。
   - 密碼加密存儲。
   - 顯示註冊成功訊息與專屬連結。

UC-04. **主揪團購訂單管理**:
   已完成
   - 任何人皆可輸入「團購名稱」、「群組公告（選填）」及「截止時間（選填）」。
   - 左側列表動態顯示所有進行中的團購名稱。
   - 點擊團購顯示團購的"團購名稱"、"公告"、"團購時限"。
   - 點擊"結束團購"按鈕 -> 要求輸入管理者密碼(目前預設為 2026)
   - 點擊"輸出訂單明細總表"按鈕 -> 要求輸入管理者密碼(目前預設為 2026) -> 顯示訂單明細
   - 在還沒結束團購前，點擊"輸出訂單明細總表"按鈕會顯示團購尚未結束
   - 預設每筆訂單接有三筆商品(等做UC-05 記得去 GroupOrder 的建構子移除)

   待完成
   - 編輯公告
   - 團購時限結束自動結束團購
   - 結束團購後將該團至"已結單的團購"
   - 新增共同管理人
   - 將不同功能分多個html (有時間再說)


## 資料儲存說明

- **後端資料**: 儲存於記憶體中 (In-Memory)。**注意：重啟專案後資料會消失**。
- **前端資料**: 視功能需求而定，部分非敏感資訊（如使用者偏好、未結帳緩存等）可能會暫存於瀏覽器的 `localStorage` 中。

