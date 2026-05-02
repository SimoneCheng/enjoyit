describe('dashboard-base.js 權限防護網測試', () => {

    test('腳本應能正常載入且 ensureLoggedIn 函式存在', () => {
        // 我們只測試這個檔案引入時沒有語法錯誤，且函式有正確匯出
        const { ensureLoggedIn } = require('./dashboard-base.js');
        expect(typeof ensureLoggedIn).toBe('function');
    });

});