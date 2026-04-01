use case 1
Scope 
揪團小幫手 Web 應用程式 (Web Application)
Level
User goal
Primary Actor
主揪
Stakeholders and Interest 
主揪: 能夠快速建立一組專屬群組的帳號與密碼，方便後續分享給群組內的成員
參與者: 能夠使用同一組帳號密碼輕鬆登入，無須個人註冊即可參與點餐
Preconditions
系統正常運作，主揪處於系統首頁或註冊頁面
Success Guarantee
成功註冊一組新的群組帳號與密碼，並寫入資料庫
Main Success Scenario
主揪新增/註冊團購群組表單，系統顯示輸入「群組帳號」與「群組密碼」
主揪輸入想要的帳號與密碼，進行提交
系統驗證帳號名稱未被他人使用
系統成功建立該帳號，顯示註冊成功訊息，並提供專屬的登入連結供主揪複製分享
Extensions
4a. 帳號名稱已被使用：
系統提示「此帳號已存在，請使用其他名稱」，主揪重新輸入
4b. 密碼格式不符 (如過短)：
系統提示「密碼長度需至少 8 碼」，主揪重新輸入
*a. 系統連線異常：
系統提示「發佈失敗，請檢查網路連線」
Special Requirements
系統在處理群組帳號的註冊與後續登入時，應自動過濾字串前後的空白字元，以降低多名成員登入時的錯誤率
Technology and Data Variations List
4a. 密碼必須經過加密 (如 bcrypt) 處理後才能儲存於資料庫中
Frequency of Occurrence
普通，通常一個實體群組 (如辦公室某部門) 只需要註冊一次
Open Issues
共用帳號若遇到密碼遺失該如何處理？是否需要綁定主揪的 Email 或 Line 以提供忘記密碼的找回機制？

