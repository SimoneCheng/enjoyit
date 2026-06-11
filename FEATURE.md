# FEATURE.md - UC-02 店家管理 (TDD 開發計畫)

本計畫採用 TDD (Test-Driven Development) 模式實作店家管理功能，每一項任務完成後請在 `[]` 中填入 `x`。

## 第一階段：模型與儲存層 (Domain & Repository) TDD

- [x] **1.1 更新領域模型 `Vendor.java`**
    - 新增 `phone`, `address`, `businessHours`, `isActive` 欄位。
    - 實作必要的 Getter/Setter。
- [x] **1.2 編寫儲存層測試 `VendorRepositoryTest.java`**
    - 測試儲存與透過 ID 查詢。
    - 測試 `findAllActive()` 只回傳 `isActive = true` 的店家。
    - 測試 `existsByNameAndAddress(name, address)` 用於檢查重複。
- [x] **1.3 實作 `InMemoryVendorRepository.java`**
    - 滿足上述測試案例。

## 第二階段：服務層 (Service) TDD

- [x] **2.1 編寫服務層測試 `VendorServiceTest.java`**
    - **新增測試**：成功案例、必填欄位缺失 (Exception)、名稱地址重複 (Exception)。
    - **修改測試**：成功更新資訊。
    - **刪除測試**：
        - 測試執行後 `isActive` 變為 `false`。
        - 測試若店家有「進行中」團購訂單，刪除時應拋出錯誤。
- [x] **2.2 實作 `VendorService.java`**
    - 實作業務邏輯與驗證規則，確保測試通過。

## 第三階段：控制層 (Controller) TDD

- [x] **3.1 編寫控制層測試 `VendorControllerTest.java`**
    - 測試 `GET /api/vendors` (列表)。
    - 測試 `POST /api/vendors` (新增)。
    - 測試 `PUT /api/vendors/{id}` (更新)。
    - 測試 `DELETE /api/vendors/{id}` (刪除/下架)。
- [x] **3.2 實作 `VendorController.java`**
    - 串接 Service 層提供 RESTful API。

## 第四階段：前端頁面與路由

- [x] **4.1 配置頁面路由 `PageController.java`**
    - 新增 `/vendors` 導向管理頁面。
- [x] **4.2 實作店家管理 HTML 模板**
    - `vendors.html` (列表與操作)。
    - `vendor-form.html` (新增與編輯共用表單)。
- [x] **4.3 實作前端邏輯 `vendor.js`**
    - 串接 API、表單驗證、動態更新介面。

## 驗證與交付

- [x] **5.1 執行全案單元測試**
    - `mvn test` 確保無回歸錯誤。
- [x] **5.2 功能手動測試驗證**
    - 驗證重複店家檢查邏輯。
    - 驗證進行中訂單保護刪除邏輯。

## 第五階段：店家與菜單關聯 (Store-Menu Linkage)

- [x] **5.1 串接店家與菜單跳轉**
    - 在 `vendor.js` 列表新增「菜單」按鈕。
    - 點擊按鈕後帶入 `vendorId` 參數導向至菜單管理頁 (`/dashboard/menu?vendorId=...`)。
- [x] **5.2 實作菜單動態載入**
    - 修改 `menu.js` 邏輯，從 URL 參數獲取 `vendorId`。
    - 根據 `vendorId` 向後端請求該店家的專屬菜單。

