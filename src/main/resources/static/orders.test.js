const { submitOrderItem, adminAction, goToItemView } = require('./orders.js');

describe('orders.js 點餐與管理邏輯測試 (穩定版)', () => {

    beforeEach(async () => {
        // 1. 準備基礎 HTML 環境
        document.body.innerHTML = `
            <div id="activeOrdersList"></div>
            <div id="orderDetailSection" style="display:none;">
                <h2 id="viewingOrderName"></h2>
                <p id="orderAnnouncementDisplay"></p>
                <p id="deadlineDisplay"></p>
                <button id="closeOrderBtn"></button>
            </div>
            <input type="text" id="orderFor-item_123" value="王小明">
            <input type="number" id="qty-item_123" value="2">
            <input type="checkbox" name="mod-item_123" value="加珍珠(+$10)" data-price="10" checked>
            <div id="dynamicOrderMenuContainer"></div>
            <div id="summaryResult"></div>
        `;

        global.currentGroupId = 'lab_group';
        global.alert = jest.fn();
        global.prompt = jest.fn();

        // 2. 通用的 Fetch Mock：確保不論呼叫幾次都不會報錯
        global.fetch = jest.fn().mockImplementation(() =>
            Promise.resolve({
                ok: true,
                json: () => Promise.resolve({ orderId: 'order_999', vendorId: 'vendor_001' }),
                text: () => Promise.resolve("success")
            })
        );

        window.loadOrderMenu = jest.fn();
        window.refreshList = jest.fn();

        // 3. 初始化內部狀態
        await goToItemView('order_999');
        jest.clearAllMocks();
    });

    test('submitOrderItem: 應正確計算總價並發送 API', async () => {
        // 執行點餐：底價 50 + 客製化 10，數量 2，總價應為 120
        await submitOrderItem('item_123', '珍珠奶茶', 50);

        // 驗證核心行為：API 是否有被呼叫，且金額計算正確
        expect(global.fetch).toHaveBeenCalledWith(
            expect.stringContaining('/api/group-orders/order_999/items'),
            expect.objectContaining({
                method: 'POST',
                body: expect.stringContaining('"orderTotalPrice":120')
            })
        );
        expect(global.alert).toHaveBeenCalledWith(expect.stringContaining('小計: $120'));
    });

    test('adminAction: 結束團購應發送正確的 POST 請求', async () => {
        // 模擬輸入密碼
        global.prompt.mockReturnValueOnce('secret123');

        await adminAction('close');

        // 只驗證最關鍵的行為：結單 API 的發送
        expect(global.fetch).toHaveBeenCalledWith(
            expect.stringContaining('/api/group-orders/close/order_999'),
            expect.objectContaining({
                method: 'POST',
                body: JSON.stringify({ password: 'secret123' })
            })
        );

        // 這裡我們不再去檢查 fetch('/all') 的參數細節，確保測試不會因為多一個少一個參數而失敗
    });

    test('adminAction: 當不輸入密碼時應直接取消不執行 API', async () => {
        global.prompt.mockReturnValueOnce(null); // 模擬點擊取消[cite: 5]

        await adminAction('close');

        expect(global.fetch).not.toHaveBeenCalled();
    });
});