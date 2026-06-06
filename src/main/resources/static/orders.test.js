const {
    submitOrderItem,
    submitDraftOrders,
    clearDraftOrders,
    adminAction,
    goToItemView,
    deleteOrderItem,
    startEditDraftItem,
    startEditSubmittedItem,
    buildOrderPayload,
    loadLocalOrderRecords,
    loadDraftOrderRecords
} = require('./orders.js');

describe('orders.js 參與者訂單管理測試', () => {
    beforeEach(async () => {
        document.body.innerHTML = `
            <div id="activeOrdersList"></div>
            <div id="ordersEmpty"></div>
            <div id="orderDetailSection" style="display:none;">
                <h2 id="viewingOrderName"></h2>
                <p id="orderAnnouncementDisplay"></p>
                <p id="deadlineDisplay"></p>
                <button id="closeOrderBtn"></button>
            </div>
            <div id="dynamicOrderMenuContainer"></div>
            <div id="draftOrdersList"></div>
            <button id="submitDraftOrdersBtn"></button>
            <div id="participantOrdersMessage"></div>
            <div id="participantOrdersList"></div>
            <div id="summaryResult"></div>
            <input id="updateDeadlineInput" value="2026-06-06T12:30">
        `;

        localStorage.clear();
        global.currentGroupId = 'lab_group';
        global.alert = jest.fn();
        global.prompt = jest.fn();
        global.confirm = jest.fn(() => true);

        global.fetch = jest.fn().mockImplementation((url, options = {}) => {
            if (url.includes('/api/group-orders/order_999/items/batch') && options.method === 'POST') {
                const payload = JSON.parse(options.body);
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve(payload.items.map((item, index) => ({ ...item, itemID: `saved_${index + 1}` }))),
                    text: () => Promise.resolve('success')
                });
            }

            if (url.includes('/api/group-orders/order_999/items/saved_1') && options.method === 'PUT') {
                const payload = JSON.parse(options.body);
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve({ ...payload, itemID: 'saved_1' }),
                    text: () => Promise.resolve('success')
                });
            }

            if (url.includes('/api/group-orders/order_999/items/saved_1') && options.method === 'DELETE') {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve({}),
                    text: () => Promise.resolve('餐點已取消')
                });
            }

            if (url.includes('/api/group-orders/close/order_999') && options.method === 'POST') {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve({}),
                    text: () => Promise.resolve('關閉成功')
                });
            }

            if (url.includes('/api/group-orders/order_999?groupId=')) {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve({
                        orderId: 'order_999',
                        orderInfo: '研究室午餐',
                        vendorId: 'vendor_001',
                        status: '進行中',
                        announcement: '快點餐',
                        orderItems: loadLocalOrderRecords('order_999').map(item => ({ ...item }))
                    }),
                    text: () => Promise.resolve('success')
                });
            }

            if (url.includes('/api/vendors/vendor_001/menu')) {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve({
                        categories: [
                            {
                                name: '飲料',
                                items: [
                                    {
                                        id: 'item_123',
                                        name: '珍珠奶茶',
                                        unitPrice: 50,
                                        modifierGroups: [
                                            {
                                                name: '配料',
                                                options: [
                                                    { name: '加珍珠', extraPrice: 10 }
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    }),
                    text: () => Promise.resolve('success')
                });
            }

            if (url.includes('/api/group-orders/all?groupId=')) {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve([]),
                    text: () => Promise.resolve('success')
                });
            }

            return Promise.resolve({
                ok: true,
                json: () => Promise.resolve({}),
                text: () => Promise.resolve('success')
            });
        });

        await goToItemView('order_999');
        jest.clearAllMocks();
    });

    test('buildOrderPayload: 姓名必填且應正確計算總價', () => {
        document.getElementById('orderFor-item_123').value = '王小明';
        document.getElementById('qty-item_123').value = '2';
        document.querySelector('input[name="mod-item_123"]').checked = true;

        const payload = buildOrderPayload('item_123', '珍珠奶茶', 50);

        expect(payload.orderFor).toBe('王小明');
        expect(payload.orderTotalPrice).toBe(120);
        expect(payload.customizations).toEqual(['加珍珠(+$10)']);
    });

    test('submitOrderItem: 應先加入待送出清單而不是直接送 API', async () => {
        document.getElementById('orderFor-item_123').value = '王小明';
        document.getElementById('qty-item_123').value = '2';
        document.querySelector('input[name="mod-item_123"]').checked = true;

        await submitOrderItem('item_123', '珍珠奶茶', 50);

        expect(global.fetch).not.toHaveBeenCalledWith(
            expect.stringContaining('/api/group-orders/order_999/items?'),
            expect.anything()
        );
        expect(loadDraftOrderRecords('order_999')).toEqual([
            expect.objectContaining({
                orderFor: '王小明',
                orderTotalPrice: 120
            })
        ]);
    });

    test('submitDraftOrders: 應批次送出並清空待送出清單', async () => {
        localStorage.setItem(
            'enjoyit-draft-order-items:lab_group:order_999',
            JSON.stringify([
                {
                    draftId: 'draft_1',
                    participantId: 'device_1',
                    orderFor: '王小明',
                    menuItemId: 'item_123',
                    itemName: '珍珠奶茶',
                    unitPrice: 50,
                    customizations: ['加珍珠(+$10)'],
                    quantity: 1,
                    orderTotalPrice: 60
                }
            ])
        );

        await submitDraftOrders();

        expect(global.fetch).toHaveBeenCalledWith(
            expect.stringContaining('/api/group-orders/order_999/items/batch'),
            expect.objectContaining({ method: 'POST' })
        );
        expect(loadDraftOrderRecords('order_999')).toEqual([]);
        expect(loadLocalOrderRecords('order_999')).toEqual([
            expect.objectContaining({ itemID: 'saved_1', orderFor: '王小明' })
        ]);
    });

    test('startEditDraftItem: 應將待送出資料帶回表單', async () => {
        localStorage.setItem(
            'enjoyit-draft-order-items:lab_group:order_999',
            JSON.stringify([
                {
                    draftId: 'draft_1',
                    participantId: 'device_1',
                    orderFor: '王小美',
                    menuItemId: 'item_123',
                    itemName: '珍珠奶茶',
                    unitPrice: 50,
                    customizations: ['加珍珠(+$10)'],
                    quantity: 3,
                    orderTotalPrice: 180
                }
            ])
        );

        await goToItemView('order_999');
        startEditDraftItem('draft_1');

        expect(document.getElementById('orderFor-item_123').value).toBe('王小美');
        expect(document.getElementById('qty-item_123').value).toBe('3');
        expect(document.querySelector('input[name="mod-item_123"]').checked).toBe(true);
    });

    test('startEditSubmittedItem: 應將已送出資料帶回表單', async () => {
        localStorage.setItem(
            'enjoyit-order-items:lab_group:order_999',
            JSON.stringify([
                {
                    itemID: 'saved_1',
                    participantId: 'device_1',
                    orderFor: '王小明',
                    menuItemId: 'item_123',
                    itemName: '珍珠奶茶',
                    unitPrice: 50,
                    customizations: ['加珍珠(+$10)'],
                    quantity: 2,
                    orderTotalPrice: 120
                }
            ])
        );

        await goToItemView('order_999');
        startEditSubmittedItem('saved_1');

        expect(document.getElementById('orderFor-item_123').value).toBe('王小明');
        expect(document.getElementById('qty-item_123').value).toBe('2');
    });

    test('clearDraftOrders: 應清空待送出清單', () => {
        localStorage.setItem(
            'enjoyit-draft-order-items:lab_group:order_999',
            JSON.stringify([{ draftId: 'draft_1', orderFor: '王小明' }])
        );

        clearDraftOrders();

        expect(loadDraftOrderRecords('order_999')).toEqual([]);
    });

    test('deleteOrderItem: 應發送刪除請求並清除 localStorage', async () => {
        localStorage.setItem(
            'enjoyit-order-items:lab_group:order_999',
            JSON.stringify([
                {
                    itemID: 'saved_1',
                    participantId: 'device_1',
                    orderFor: '王小明',
                    menuItemId: 'item_123',
                    itemName: '珍珠奶茶',
                    unitPrice: 50,
                    customizations: [],
                    quantity: 1,
                    orderTotalPrice: 50
                }
            ])
        );

        await deleteOrderItem('saved_1');

        expect(global.fetch).toHaveBeenCalledWith(
            expect.stringContaining('/api/group-orders/order_999/items/saved_1'),
            expect.objectContaining({ method: 'DELETE' })
        );
        expect(loadLocalOrderRecords('order_999')).toEqual([]);
        expect(global.alert).toHaveBeenCalledWith('取消餐點成功');
    });

    test('adminAction: 結束團購應發送正確的 POST 請求', async () => {
        global.prompt.mockReturnValueOnce('secret123');

        await adminAction('close');

        expect(global.fetch).toHaveBeenCalledWith(
            expect.stringContaining('/api/group-orders/close/order_999'),
            expect.objectContaining({
                method: 'POST',
                body: JSON.stringify({ password: 'secret123' })
            })
        );
    });

    test('adminAction: 當不輸入密碼時應直接取消不執行 API', async () => {
        global.prompt.mockReturnValueOnce(null);

        await adminAction('close');

        expect(global.fetch).not.toHaveBeenCalled();
    });
});
