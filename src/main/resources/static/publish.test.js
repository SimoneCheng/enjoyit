const { handlePublish } = require('./publish.js');

describe('publish.js 發起團購測試', () => {

    beforeEach(() => {
        // 1. 準備模擬的 HTML 畫面 (JSDOM)
        document.body.innerHTML = `
            <div>
                <input id="orderName" value="實驗室下午茶" />
                <input id="announcement" value="大家快來點" />
                <select id="vendorSelect"><option value="vendor_001">店家A</option></select>
                <input id="adminPwdSetup" value="secret123" />
                <input id="deadline" value="2026-12-31T12:00" />
            </div>
        `;

        // 2. 模擬全域變數與方法
        global.currentGroupId = 'lab_group';
        global.alert = jest.fn(); // 攔截 alert，不讓它真的彈出視窗
        global.fetch = jest.fn(); // 攔截所有的 fetch API
        window.refreshList = jest.fn(); // 模擬畫面重整函式
    });

    afterEach(() => {
        // 每次測試完清空 mock 紀錄，避免互相干擾
        jest.clearAllMocks();
    });

    test('當缺少名稱或密碼時，應阻擋送出並跳出警告', async () => {
        document.getElementById('orderName').value = ''; // 刻意清空名稱

        await handlePublish();

        expect(global.alert).toHaveBeenCalledWith('請輸入團購名稱與管理者密碼！');
        expect(global.fetch).not.toHaveBeenCalled(); // 確保沒有打 API
    });

    test('當成功發起團購且有截止時間時，應連續呼叫兩次 API 並清空表單', async () => {
        // 模擬第一次 fetch (發起團購) 回傳成功，並獲得 orderId 'order_999'
        global.fetch.mockResolvedValueOnce({
            ok: true,
            text: () => Promise.resolve('order_999')
        });

        // 模擬第二次 fetch (設定截止時間) 回傳成功
        global.fetch.mockResolvedValueOnce({
            ok: true
        });

        await handlePublish();

        // 驗證第一次 API 呼叫是否正確帶上參數
        expect(global.fetch).toHaveBeenNthCalledWith(1, '/api/group-orders/publish', expect.objectContaining({
            method: 'POST',
            body: expect.stringContaining('"orderInfo":"實驗室下午茶"')
        }));

        // 驗證第二次 API 呼叫的網址與參數是否正確組裝
        expect(global.fetch).toHaveBeenNthCalledWith(2,
            '/api/group-orders/order_999/deadline?groupId=lab_group&password=secret123',
            expect.any(Object)
        );

        // 驗證成功後的 UI 反應
        expect(global.alert).toHaveBeenCalledWith('團購發起成功！');
        expect(document.getElementById('orderName').value).toBe(''); // 確認欄位被清空
        expect(window.refreshList).toHaveBeenCalled(); // 確認有呼叫列表重整
    });

    test('當伺服器連線異常時，應捕捉錯誤並警告', async () => {
        // 模擬 fetch 拋出網路異常
        global.fetch.mockRejectedValueOnce(new Error('Network Error'));

        await handlePublish();

        expect(global.alert).toHaveBeenCalledWith('連線異常');
    });
});