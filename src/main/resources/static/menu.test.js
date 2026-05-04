const { MenuEditor } = require('./menu.js');

describe('menu.js 菜單編輯器邏輯測試', () => {

    let editor;

    beforeEach(() => {
        // 準備 DOM 容器給 MenuEditor 渲染
        document.body.innerHTML = `
            <div id="menu-container"></div>
            <input id="newCategoryName" value="">
            <button id="add-category-submit"></button>
        `;

        global.alert = jest.fn();
        global.fetch = jest.fn();

        // 模擬 API 回傳一份有「飲料」分類的初始菜單
        global.fetch.mockResolvedValue({
            ok: true,
            json: () => Promise.resolve({
                isActive: true,
                categories: [
                    { name: '飲料', isActive: true, items: [] }
                ]
            })
        });

        // 實例化 MenuEditor
        editor = new MenuEditor('vendor_001');
    });

    afterEach(() => {
        jest.clearAllMocks();
    });

    test('loadMenuData: 應能成功呼叫 API 並將菜單資料載入', async () => {
        // 等待 loadMenuData 執行完畢
        await editor.loadMenuData();

        expect(global.fetch).toHaveBeenCalledWith('/api/vendors/vendor_001/menu', expect.any(Object));
        expect(editor.menuData.categories.length).toBe(1);
        expect(editor.menuData.categories[0].name).toBe('飲料');
    });

    test('addItem: 金額為負數或不是數字時，應阻擋新增', async () => {
        // 先確保菜單已載入，並且渲染出 HTML
        await editor.loadMenuData();

        // 在 DOM 中塞入錯誤的資料
        document.getElementById('item-name-0').value = '珍珠奶茶';
        document.getElementById('item-price-0').value = '-50'; // 負數價格

        editor.addItem(0);

        expect(global.alert).toHaveBeenCalledWith('請輸入正確的金額');
        expect(editor.menuData.categories[0].items.length).toBe(0); // 確保沒有被加進資料庫
    });

    test('addItem: 資料合法時，應成功加入品項並觸發靜默儲存 (silent: true)', async () => {
        await editor.loadMenuData();

        // 為了捕捉 addItem 內部呼叫 submitMenu 的動作，我們監聽這個方法
        jest.spyOn(editor, 'submitMenu');

        document.getElementById('item-name-0').value = '綠茶';
        document.getElementById('item-price-0').value = '30';

        editor.addItem(0);

        const targetCategory = editor.menuData.categories[0];
        expect(targetCategory.items.length).toBe(1);
        expect(targetCategory.items[0].name).toBe('綠茶');
        expect(targetCategory.items[0].unitPrice).toBe(30);

        // 驗證是否有呼叫儲存 API，並且設定 silent = true (不跳出 alert)
        expect(editor.submitMenu).toHaveBeenCalledWith({ silent: true });
    });

    test('toggleMenuStatus: 應能正確切換整張菜單的上下架狀態並觸發重新渲染', async () => {
        await editor.loadMenuData();

        expect(editor.menuData.isActive).toBe(true);

        // 執行切換
        editor.toggleMenuStatus();

        expect(editor.menuData.isActive).toBe(false);
    });
});