let currentOrderId = null;
let currentVendorId = null;
let editingTarget = null;

const DEVICE_KEY = 'enjoyit-device-id';
const ORDER_CACHE_PREFIX = 'enjoyit-order-items';
const DRAFT_CACHE_PREFIX = 'enjoyit-draft-order-items';

window.dashboardModules = window.dashboardModules || [];
window.dashboardModules.push(() => {
    refreshList();
});

window.handleOrdersSectionShown = async function handleOrdersSectionShown() {
    await refreshList();
    if (currentOrderId) {
        await goToItemView(currentOrderId);
    }
};

window.onMenuUpdated = async function onMenuUpdated(vendorId) {
    const ordersSection = document.getElementById('ordersSection');
    const isOrdersVisible = ordersSection && ordersSection.classList.contains('active');
    if (isOrdersVisible && currentOrderId && currentVendorId === vendorId) {
        await goToItemView(currentOrderId);
    }
};

function getDeviceId() {
    if (typeof localStorage === 'undefined') {
        return 'test-device';
    }
    let deviceId = localStorage.getItem(DEVICE_KEY);
    if (!deviceId) {
        deviceId = `device_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;
        localStorage.setItem(DEVICE_KEY, deviceId);
    }
    return deviceId;
}

function getStorageKey(prefix, orderId) {
    return `${prefix}:${currentGroupId || 'unknown'}:${orderId}`;
}

function loadRecords(prefix, orderId) {
    if (!orderId || typeof localStorage === 'undefined') {
        return [];
    }

    try {
        const raw = localStorage.getItem(getStorageKey(prefix, orderId));
        if (!raw) return [];
        const parsed = JSON.parse(raw);
        return Array.isArray(parsed) ? parsed : [];
    } catch (e) {
        return [];
    }
}

function saveRecords(prefix, orderId, records) {
    if (!orderId || typeof localStorage === 'undefined') {
        return;
    }
    localStorage.setItem(getStorageKey(prefix, orderId), JSON.stringify(records));
}

function clearRecords(prefix, orderId) {
    if (!orderId || typeof localStorage === 'undefined') {
        return;
    }
    localStorage.removeItem(getStorageKey(prefix, orderId));
}

function loadLocalOrderRecords(orderId) {
    return loadRecords(ORDER_CACHE_PREFIX, orderId);
}

function saveLocalOrderRecords(orderId, records) {
    saveRecords(ORDER_CACHE_PREFIX, orderId, records);
}

function loadDraftOrderRecords(orderId) {
    return loadRecords(DRAFT_CACHE_PREFIX, orderId);
}

function saveDraftOrderRecords(orderId, records) {
    saveRecords(DRAFT_CACHE_PREFIX, orderId, records);
}

function clearLocalOrderRecords(orderId) {
    clearRecords(ORDER_CACHE_PREFIX, orderId);
}

function clearDraftOrderRecords(orderId) {
    clearRecords(DRAFT_CACHE_PREFIX, orderId);
}

function normalizeOrderItem(item) {
    return {
        itemID: item.itemID,
        participantId: item.participantId,
        orderFor: item.orderFor,
        menuItemId: item.menuItemId,
        itemName: item.itemName,
        unitPrice: item.unitPrice,
        customizations: item.customizations || [],
        quantity: item.quantity,
        orderTotalPrice: item.orderTotalPrice
    };
}

function normalizeDraftItem(item) {
    return {
        draftId: item.draftId || `draft_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
        participantId: item.participantId,
        orderFor: item.orderFor,
        menuItemId: item.menuItemId,
        itemName: item.itemName,
        unitPrice: item.unitPrice,
        customizations: item.customizations || [],
        quantity: item.quantity,
        orderTotalPrice: item.orderTotalPrice
    };
}

function upsertLocalOrderRecord(orderId, item) {
    const records = loadLocalOrderRecords(orderId);
    const nextRecord = normalizeOrderItem(item);
    const existingIndex = records.findIndex(record => record.itemID === item.itemID);
    if (existingIndex >= 0) {
        records.splice(existingIndex, 1, nextRecord);
    } else {
        records.push(nextRecord);
    }
    saveLocalOrderRecords(orderId, records);
}

function removeLocalOrderRecord(orderId, itemId) {
    const records = loadLocalOrderRecords(orderId).filter(record => record.itemID !== itemId);
    saveLocalOrderRecords(orderId, records);
}

function upsertDraftOrderRecord(orderId, item) {
    const records = loadDraftOrderRecords(orderId);
    const nextRecord = normalizeDraftItem(item);
    const existingIndex = records.findIndex(record => record.draftId === nextRecord.draftId);
    if (existingIndex >= 0) {
        records.splice(existingIndex, 1, nextRecord);
    } else {
        records.push(nextRecord);
    }
    saveDraftOrderRecords(orderId, records);
}

function removeDraftOrderRecord(orderId, draftId) {
    const records = loadDraftOrderRecords(orderId).filter(record => record.draftId !== draftId);
    saveDraftOrderRecords(orderId, records);
}

function setParticipantOrdersMessage(message) {
    const messageBox = document.getElementById('participantOrdersMessage');
    if (!messageBox) return;

    if (!message) {
        messageBox.style.display = 'none';
        messageBox.textContent = '';
        return;
    }

    messageBox.style.display = 'block';
    messageBox.textContent = message;
}

async function refreshList() {
    const listDiv = document.getElementById('activeOrdersList');
    const emptyState = document.getElementById('ordersEmpty');
    const detailSection = document.getElementById('orderDetailSection');
    if (!listDiv) return;

    try {
        const res = await fetch(`/api/group-orders/all?groupId=${encodeURIComponent(currentGroupId)}`);
        const orders = await res.json();
        listDiv.innerHTML = '';
        if (emptyState) {
            emptyState.style.display = orders.length === 0 ? 'block' : 'none';
        }
        if (detailSection && orders.length === 0) {
            detailSection.style.display = 'none';
        }
        orders.forEach(order => {
            const btn = document.createElement('div');
            btn.className = 'menu-btn';
            btn.dataset.orderId = order.orderId;
            btn.innerHTML = `${order.status === '已結單' ? '🔒' : '🥤'} ${order.orderInfo}`;
            btn.onclick = () => goToItemView(order.orderId);
            if (currentOrderId === order.orderId) {
                btn.classList.add('active');
            }
            listDiv.appendChild(btn);
        });
    } catch (e) {
        console.error('Refresh list failed');
    }
}

async function goToItemView(orderId) {
    const viewingTitle = document.getElementById('viewingOrderName');
    const detailSection = document.getElementById('orderDetailSection');
    if (!viewingTitle) return;

    currentOrderId = orderId;
    editingTarget = null;
    if (detailSection) {
        detailSection.style.display = 'block';
    }
    document.querySelectorAll('#activeOrdersList .menu-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.orderId === orderId);
    });

    try {
        const res = await fetch(`/api/group-orders/${orderId}?groupId=${encodeURIComponent(currentGroupId)}`);
        if (!res.ok) return;
        const order = await res.json();
        currentVendorId = order.vendorId;

        viewingTitle.textContent = '團購： ' + order.orderInfo;
        document.getElementById('orderAnnouncementDisplay').textContent = '📢 公告：' + (order.announcement || '無公告');
        document.getElementById('deadlineDisplay').textContent = order.deadline
            ? '截止時間：' + order.deadline.substring(0, 16).replace('T', ' ')
            : '無截止時間';

        const closeBtn = document.getElementById('closeOrderBtn');
        if (closeBtn) {
            const isClosed = order.status === '已結單';
            closeBtn.textContent = isClosed ? '團購已截止' : '🔒 結束團購';
            closeBtn.disabled = isClosed;
            closeBtn.classList.toggle('disabled', isClosed);
        }

        await loadOrderMenu(currentVendorId, order.status === '已結單');
        renderDraftOrders(order);
        renderParticipantOrders(order);
    } catch (e) {
        console.error(e);
    }
    fetchMyPaymentStatus(orderId);
}

function renderDraftOrders(order) {
    const container = document.getElementById('draftOrdersList');
    const submitBtn = document.getElementById('submitDraftOrdersBtn');
    if (!container) return;

    if (order.status === '已結單') {
        clearDraftOrderRecords(order.orderId);
        container.innerHTML = '<p style="color:#888;">團購已截止，待送出清單已清空。</p>';
        if (submitBtn) submitBtn.disabled = true;
        return;
    }

    const records = loadDraftOrderRecords(order.orderId);
    if (submitBtn) submitBtn.disabled = records.length === 0;

    if (records.length === 0) {
        container.innerHTML = '<p style="color:#888;">目前還沒有待送出的餐點。</p>';
        return;
    }

    container.innerHTML = '';
    records.forEach(record => {
        const card = document.createElement('div');
        card.className = 'item-card';
        const customizationText = record.customizations.length > 0 ? record.customizations.join('、') : '無';
        card.innerHTML = `
            <div style="flex:1;">
                <div><strong>${record.orderFor}</strong> 的 ${record.itemName}</div>
                <div style="font-size: 0.95em; color: #666;">規格：${customizationText}</div>
                <div style="font-size: 0.95em; color: #666;">數量：${record.quantity}，預估小計：$${record.orderTotalPrice}</div>
            </div>
            <div style="display:flex; gap:8px;">
                <button class="action-btn" style="padding: 5px 10px;" onclick="startEditDraftItem('${record.draftId}')">修改</button>
                <button class="action-btn danger-btn" style="padding: 5px 10px;" onclick="removeDraftItem('${record.draftId}')">移除</button>
            </div>
        `;
        container.appendChild(card);
    });
}

function renderParticipantOrders(order) {
    const container = document.getElementById('participantOrdersList');
    if (!container || !Array.isArray(order.orderItems)) return;

    const localRecords = loadLocalOrderRecords(order.orderId);
    if (order.status === '已結單') {
        clearLocalOrderRecords(order.orderId);
        clearDraftOrderRecords(order.orderId);
        setParticipantOrdersMessage('此團購已截止，這台裝置上的暫存與可編輯紀錄都已清除。');
        container.innerHTML = '<p style="color:#888;">團購已截止，無法再修改或取消餐點。</p>';
        return;
    }

    const itemMap = new Map(order.orderItems.map(item => [item.itemID, item]));
    const activeRecords = [];
    const staleRecords = [];

    localRecords.forEach(record => {
        const serverItem = itemMap.get(record.itemID);
        if (serverItem) {
            activeRecords.push(normalizeOrderItem(serverItem));
        } else {
            staleRecords.push(record);
        }
    });

    if (staleRecords.length > 0) {
        saveLocalOrderRecords(order.orderId, activeRecords);
        setParticipantOrdersMessage('有部分舊訂單紀錄已失效，可能是伺服器重設或該餐點已不存在，系統已自動清除。');
    } else {
        setParticipantOrdersMessage('');
    }

    if (activeRecords.length === 0) {
        container.innerHTML = '<p style="color:#888;">這台裝置目前還沒有送出任何餐點。</p>';
        return;
    }

    container.innerHTML = '';
    activeRecords.forEach(record => {
        const card = document.createElement('div');
        card.className = 'item-card';
        const customizationText = record.customizations.length > 0 ? record.customizations.join('、') : '無';
        card.innerHTML = `
            <div style="flex:1;">
                <div><strong>${record.orderFor}</strong> 的 ${record.itemName}</div>
                <div style="font-size: 0.95em; color: #666;">規格：${customizationText}</div>
                <div style="font-size: 0.95em; color: #666;">數量：${record.quantity}，小計：$${record.orderTotalPrice}</div>
            </div>
            <div style="display:flex; gap:8px;">
                <button class="action-btn" style="padding: 5px 10px;" onclick="startEditSubmittedItem('${record.itemID}')">修改</button>
                <button class="action-btn danger-btn" style="padding: 5px 10px;" onclick="deleteOrderItem('${record.itemID}')">取消</button>
            </div>
        `;
        container.appendChild(card);
    });
}

async function loadOrderMenu(vendorId, isReadOnly = false) {
    const container = document.getElementById('dynamicOrderMenuContainer');
    if (!container) return;

    container.innerHTML = '載入中...';
    try {
        const res = await fetch(`/api/vendors/${vendorId}/menu`, { cache: 'no-store' });
        if (!res.ok) {
            container.innerHTML = "<p style='color:red;'>無法取得店家菜單，主揪可能尚未建立。</p>";
            return;
        }
        const menuData = await res.json();

        if (menuData.isActive === false) {
            container.innerHTML = `
                <div style="background: #ffebe9; border: 1px solid red; padding: 15px; border-radius: 8px; text-align: center; color: #d32f2f;">
                    <strong>⛔ 店家菜單目前已下架，暫停開放點餐</strong>
                </div>`;
            return;
        }

        container.innerHTML = '';
        if (!menuData.categories || menuData.categories.length === 0) {
            container.innerHTML = '<p>店家尚未設定任何餐點。</p>';
            return;
        }

        menuData.categories.forEach(cat => {
            if (cat.isActive === false) return;

            const catDiv = document.createElement('div');
            catDiv.innerHTML = `<h4 style="border-bottom: 1px solid #ccc; padding-bottom: 5px;">${cat.name}</h4>`;

            cat.items.forEach(item => {
                if (item.isActive === false) return;

                const itemDiv = document.createElement('div');
                itemDiv.className = 'item-card';
                itemDiv.innerHTML = `
                    <div style="flex:1;"><strong>${item.name}</strong> <span>$${item.unitPrice}</span></div>
                    <button class="action-btn" style="padding: 5px 10px;" ${isReadOnly ? 'disabled' : ''} onclick="openOrderForm('${item.id}')">
                        ${isReadOnly ? '已截止' : '選這個'}
                    </button>
                `;
                catDiv.appendChild(itemDiv);

                const formDiv = document.createElement('div');
                formDiv.className = 'order-form-box';
                formDiv.id = `form-${item.id}`;

                let modifierHtml = '';
                if (item.modifierGroups) {
                    item.modifierGroups.forEach(mg => {
                        if (mg.isActive === false) return;
                        modifierHtml += `<div style="margin-bottom: 5px;"><strong>${mg.name}:</strong><br>`;
                        mg.options.forEach(opt => {
                            modifierHtml += `<label style="display:inline; margin-right:10px; font-weight:normal;">
                                <input type="checkbox" name="mod-${item.id}" value="${opt.name}(+$${opt.extraPrice})" data-price="${opt.extraPrice}">
                                ${opt.name} (+${opt.extraPrice})
                            </label>`;
                        });
                        modifierHtml += `</div>`;
                    });
                }

                formDiv.innerHTML = `
                    <div>
                        <input type="text" id="orderFor-${item.id}" placeholder="訂購人姓名 (必填)" style="margin-bottom: 10px;">
                        ${modifierHtml}
                        <div style="margin: 8px 0; color: #444;">
                            預估小計：<strong id="subtotal-${item.id}" data-base-price="${item.unitPrice}">$${item.unitPrice}</strong>
                        </div>
                        <div style="margin-top: 10px; display: flex; gap: 10px; align-items: center; flex-wrap: wrap;">
                            數量: <input type="number" id="qty-${item.id}" value="1" min="1" style="width: 60px;">
                            <button class="action-btn success-btn" style="padding: 5px 15px;" onclick="submitOrderItem('${item.id}', '${item.name}', ${item.unitPrice})">
                                加入待送出清單
                            </button>
                            <button class="action-btn danger-btn" style="padding: 5px 15px;" onclick="cancelOrderForm('${item.id}', true)">取消</button>
                        </div>
                    </div>
                `;
                catDiv.appendChild(formDiv);
                queueMicrotask(() => bindOrderFormEvents(item.id, item.unitPrice));
            });

            container.appendChild(catDiv);
        });
    } catch (e) {
        container.innerHTML = '載入菜單發生錯誤。';
    }
}

function bindOrderFormEvents(itemId, basePrice) {
    const qtyInput = document.getElementById(`qty-${itemId}`);
    if (qtyInput) {
        qtyInput.addEventListener('input', () => updateSubtotal(itemId, basePrice));
    }
    document.querySelectorAll(`input[name="mod-${itemId}"]`).forEach(cb => {
        cb.addEventListener('change', () => updateSubtotal(itemId, basePrice));
    });
    updateSubtotal(itemId, basePrice);
}

function updateSubtotal(itemId, basePrice) {
    const subtotalEl = document.getElementById(`subtotal-${itemId}`);
    const qtyEl = document.getElementById(`qty-${itemId}`);
    if (!subtotalEl || !qtyEl) return;

    const quantity = Math.max(parseInt(qtyEl.value, 10) || 1, 1);
    let extraTotal = 0;
    document.querySelectorAll(`input[name="mod-${itemId}"]:checked`).forEach(cb => {
        extraTotal += parseInt(cb.getAttribute('data-price'), 10);
    });
    subtotalEl.textContent = `$${(basePrice + extraTotal) * quantity}`;
}

function openOrderForm(itemId) {
    document.querySelectorAll('.order-form-box').forEach(el => {
        el.style.display = 'none';
    });
    const form = document.getElementById(`form-${itemId}`);
    if (form) {
        form.style.display = 'block';
    }
}

function getBasePriceFromForm(itemId) {
    const subtotalEl = document.getElementById(`subtotal-${itemId}`);
    if (!subtotalEl) return 0;
    return Number.parseInt(subtotalEl.dataset.basePrice || '0', 10) || 0;
}

function updateSubmitButtonLabel(itemId, mode) {
    const form = document.getElementById(`form-${itemId}`);
    if (!form) return;
    const submitBtn = form.querySelector('.success-btn');
    if (!submitBtn) return;

    if (mode === 'draft') {
        submitBtn.textContent = '更新待送出清單';
    } else if (mode === 'submitted') {
        submitBtn.textContent = '儲存修改';
    } else {
        submitBtn.textContent = '加入待送出清單';
    }
}

function resetOrderForm(itemId, preserveName = false) {
    const nameInput = document.getElementById(`orderFor-${itemId}`);
    const qtyInput = document.getElementById(`qty-${itemId}`);
    const currentName = nameInput ? nameInput.value : '';

    if (nameInput) {
        nameInput.value = preserveName ? currentName : '';
    }
    if (qtyInput) {
        qtyInput.value = 1;
    }
    document.querySelectorAll(`input[name="mod-${itemId}"]`).forEach(cb => {
        cb.checked = false;
    });

    editingTarget = null;
    updateSubmitButtonLabel(itemId, null);
    updateSubtotal(itemId, getBasePriceFromForm(itemId));
}

function cancelOrderForm(itemId, resetEditingState = false) {
    const form = document.getElementById(`form-${itemId}`);
    if (form) {
        form.style.display = 'none';
    }
    if (resetEditingState) {
        resetOrderForm(itemId);
    }
}

function populateOrderForm(record, mode) {
    if (!record) return;
    const itemId = record.menuItemId;
    openOrderForm(itemId);
    editingTarget = { mode: mode, id: mode === 'draft' ? record.draftId : record.itemID };

    const nameInput = document.getElementById(`orderFor-${itemId}`);
    const qtyInput = document.getElementById(`qty-${itemId}`);
    if (nameInput) {
        nameInput.value = record.orderFor || '';
    }
    if (qtyInput) {
        qtyInput.value = record.quantity || 1;
    }
    document.querySelectorAll(`input[name="mod-${itemId}"]`).forEach(cb => {
        cb.checked = (record.customizations || []).includes(cb.value);
    });

    updateSubmitButtonLabel(itemId, mode);
    updateSubtotal(itemId, record.unitPrice);
}

function startEditDraftItem(draftId) {
    const record = loadDraftOrderRecords(currentOrderId).find(item => item.draftId === draftId);
    if (!record) {
        alert('找不到這筆待送出餐點。');
        return;
    }
    populateOrderForm(record, 'draft');
}

function startEditSubmittedItem(itemId) {
    const record = loadLocalOrderRecords(currentOrderId).find(item => item.itemID === itemId);
    if (!record) {
        alert('找不到這筆已送出的餐點。');
        return;
    }
    populateOrderForm(record, 'submitted');
}

function buildOrderPayload(itemId, itemName, basePrice) {
    const orderFor = document.getElementById(`orderFor-${itemId}`).value.trim();
    const quantity = parseInt(document.getElementById(`qty-${itemId}`).value, 10);
    if (!orderFor) {
        alert('請輸入訂購人姓名');
        return null;
    }
    if (!quantity || quantity <= 0) {
        alert('請輸入正確的數量');
        return null;
    }

    let extraTotal = 0;
    const customizations = [];
    document.querySelectorAll(`input[name="mod-${itemId}"]:checked`).forEach(cb => {
        customizations.push(cb.value);
        extraTotal += parseInt(cb.getAttribute('data-price'), 10);
    });

    return {
        participantId: getDeviceId(),
        orderFor: orderFor,
        menuItemId: itemId,
        itemName: itemName,
        unitPrice: basePrice,
        customizations: customizations,
        quantity: quantity,
        orderTotalPrice: (basePrice + extraTotal) * quantity
    };
}

async function submitOrderItem(itemId, itemName, basePrice) {
    const orderItemJson = buildOrderPayload(itemId, itemName, basePrice);
    if (!orderItemJson) return;

    if (editingTarget && editingTarget.mode === 'submitted') {
        try {
            const res = await fetch(`/api/group-orders/${currentOrderId}/items/${editingTarget.id}?groupId=${encodeURIComponent(currentGroupId)}`, {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(orderItemJson)
            });
            if (res.ok) {
                const savedItem = normalizeOrderItem(await res.json());
                upsertLocalOrderRecord(currentOrderId, savedItem);
                alert(`✅ 修改成功！ ${itemName} 小計: $${savedItem.orderTotalPrice}`);
                resetOrderForm(itemId);
                document.getElementById(`form-${itemId}`).style.display = 'none';
                await goToItemView(currentOrderId);
            } else {
                handleOrderMutationError(await res.text());
            }
        } catch (e) {
            alert('送出失敗');
        }
        return;
    }

    const draftItem = normalizeDraftItem({
        ...orderItemJson,
        draftId: editingTarget && editingTarget.mode === 'draft' ? editingTarget.id : undefined
    });
    upsertDraftOrderRecord(currentOrderId, draftItem);
    renderDraftOrders({ orderId: currentOrderId, status: '進行中' });
    alert(editingTarget && editingTarget.mode === 'draft' ? '✅ 已更新待送出清單' : '✅ 已加入待送出清單');
    resetOrderForm(itemId, true);
    document.getElementById(`form-${itemId}`).style.display = 'none';
}

async function submitDraftOrders() {
    const drafts = loadDraftOrderRecords(currentOrderId);
    if (drafts.length === 0) {
        alert('待送出清單目前是空的');
        return;
    }

    const payload = {
        items: drafts.map(({ draftId, ...item }) => item)
    };

    try {
        const res = await fetch(`/api/group-orders/${currentOrderId}/items/batch?groupId=${encodeURIComponent(currentGroupId)}`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(payload)
        });
        if (res.ok) {
            const savedItems = await res.json();
            savedItems.forEach(item => {
                upsertLocalOrderRecord(currentOrderId, normalizeOrderItem(item));
            });
            clearDraftOrderRecords(currentOrderId);
            alert(`✅ 已一次送出 ${savedItems.length} 筆餐點`);
            await goToItemView(currentOrderId);
        } else {
            handleOrderMutationError(await res.text());
        }
    } catch (e) {
        alert('批次送出失敗');
    }
}

function removeDraftItem(draftId) {
    if (editingTarget && editingTarget.mode === 'draft' && editingTarget.id === draftId) {
        editingTarget = null;
        document.querySelectorAll('.order-form-box').forEach(form => {
            const submitBtn = form.querySelector('.success-btn');
            if (submitBtn) {
                submitBtn.textContent = '加入待送出清單';
            }
        });
    }
    removeDraftOrderRecord(currentOrderId, draftId);
    renderDraftOrders({ orderId: currentOrderId, status: '進行中' });
}

function clearDraftOrders() {
    clearDraftOrderRecords(currentOrderId);
    editingTarget = null;
    document.querySelectorAll('.order-form-box').forEach(form => {
        const submitBtn = form.querySelector('.success-btn');
        if (submitBtn) {
            submitBtn.textContent = '加入待送出清單';
        }
    });
    renderDraftOrders({ orderId: currentOrderId, status: '進行中' });
}

async function deleteOrderItem(itemId) {
    const confirmed = typeof confirm === 'function' ? confirm('確定要取消這筆餐點嗎？') : true;
    if (!confirmed) return;

    try {
        const res = await fetch(`/api/group-orders/${currentOrderId}/items/${itemId}?groupId=${encodeURIComponent(currentGroupId)}`, {
            method: 'DELETE'
        });
        if (res.ok) {
            removeLocalOrderRecord(currentOrderId, itemId);
            alert('取消餐點成功');
            await goToItemView(currentOrderId);
        } else {
            handleOrderMutationError(await res.text());
        }
    } catch (e) {
        alert('取消失敗');
    }
}

async function handleOrderMutationError(errorText) {
    if (errorText.includes('已截止')) {
        clearLocalOrderRecords(currentOrderId);
        clearDraftOrderRecords(currentOrderId);
        await goToItemView(currentOrderId);
    }
    alert(errorText);
}

async function adminAction(type) {
    if (!currentOrderId) return alert('請先選擇團購');
    const password = prompt('請輸入發起時設定的管理者密碼：');
    if (!password) return;

    if (type === 'close') {
        const res = await fetch(`/api/group-orders/close/${currentOrderId}?groupId=${encodeURIComponent(currentGroupId)}`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ password: password })
        });
        if (res.ok) {
            alert('團購已結單！現在可以產出明細總表了。');
            clearLocalOrderRecords(currentOrderId);
            clearDraftOrderRecords(currentOrderId);
            refreshList();
            goToItemView(currentOrderId);
        } else {
            alert(await res.text());
        }
    } else if (type === 'summary') {
        const res = await fetch(`/api/group-orders/${currentOrderId}/summary?groupId=${encodeURIComponent(currentGroupId)}&password=${encodeURIComponent(password)}`);
        if (res.ok) {
            const data = await res.json();
            document.getElementById('summaryResult').innerHTML = `
                <div style="background:#e3f2fd; padding:15px; border-radius:5px; border:1px solid #2196f3;">
                    <strong>統計總表：</strong><br>
                    <pre>${JSON.stringify(data, null, 2)}</pre>
                </div>`;
        } else {
            alert(await res.text());
        }
    } else if (type === 'deadline') {
        const newDeadline = document.getElementById('updateDeadlineInput').value;
        if (!newDeadline) return alert('請先選擇時間');

        const res = await fetch(`/api/group-orders/${currentOrderId}/deadline?groupId=${encodeURIComponent(currentGroupId)}&password=${encodeURIComponent(password)}`, {
            method: 'PUT',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ deadline: newDeadline })
        });
        if (res.ok) {
            alert('時間更新成功！');
            goToItemView(currentOrderId);
        } else {
            alert(await res.text());
        }
    }
}

if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
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
    };
}

// UC-07: 獲取個人帳單明細
async function viewMyBill() {
    if (!currentOrderId) return alert('請先選擇一個團購');
    const deviceId = getDeviceId();

    try {
        const res = await fetch(`/api/group-orders/${currentOrderId}/payments/bill?participantId=${deviceId}`);
        if (!res.ok) throw new Error(await res.text());
        const bill = await res.json();

        const financeDiv = document.getElementById('financeSection');
        financeDiv.style.display = 'block';
        financeDiv.innerHTML = `
            <h3 style="color: #ff9800;">💰 個人帳單</h3>
            <p><strong>應付總額：</strong> $${bill.amountDue}</p>
            <p><strong>目前狀態：</strong> ${bill.status}</p>
            <hr>
            <h4>回報付款狀態</h4>
            <select id="payMethod"><option value="現金">現金</option><option value="轉帳">轉帳</option></select>
            <input type="text" id="payDetails" placeholder="備註 (例如轉帳末五碼)">
            <button onclick="reportPayment('${deviceId}')" style="margin-top: 10px;">送出回報</button>
        `;
    } catch (e) {
        alert('無法取得帳單：可能尚未結單產生財務紀錄');
    }
}

// UC-07: 參與者回報付款
async function reportPayment(participantId) {
    const method = document.getElementById('payMethod').value;
    const details = document.getElementById('payDetails').value;

    const res = await fetch(`/api/group-orders/${currentOrderId}/payments/report`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ participantId, method, details })
    });

    if (res.ok) {
        alert('付款回報成功！等待主揪確認。');
        viewMyBill(); // 重新整理畫面
    } else {
        alert(await res.text());
    }
}

// UC-07: 主揪查看所有財務紀錄與核帳
async function manageFinances() {
    if (!currentOrderId) return;
    const pwd = prompt('請輸入管理者密碼以進行財務管理：');
    if (!pwd) return;

    try {
        const res = await fetch(`/api/group-orders/${currentOrderId}/payments`);
        if (!res.ok) throw new Error('無法取得財務明細');
        const records = await res.json();

        let html = `<h3 style="color: #9c27b0;">📋 財務核帳管理</h3>`;
        html += `<table style="width: 100%; text-align: left; border-collapse: collapse;">
                    <tr style="border-bottom: 2px solid #ddd;">
                        <th>參與者</th><th>應付</th><th>狀態</th><th>方式</th><th>明細</th><th>操作</th>
                    </tr>`;

        records.forEach(r => {
            html += `<tr style="border-bottom: 1px solid #eee;">
                        <td>${r.participantId.substring(0, 8)}...</td>
                        <td>$${r.amountDue}</td>
                        <td style="color: ${r.status==='已收款'?'green':'red'};">${r.status}</td>
                        <td>${r.method || '-'}</td>
                        <td>${r.details || '-'}</td>
                        <td>
                            ${r.status !== '已收款' ? `<button onclick="confirmPayment('${r.participantId}', ${r.amountDue})">確認收款</button>` : '已核帳'}
                        </td>
                     </tr>`;
        });
        html += `</table>`;
        html += `<button onclick="finalizeFinances()" style="margin-top:15px; background: #f44336; color: white;">🔒 完成結算鎖定帳本</button>`;

        const financeDiv = document.getElementById('financeSection');
        financeDiv.style.display = 'block';
        financeDiv.innerHTML = html;
    } catch (e) {
        alert(e.message);
    }
}

// UC-07: 主揪確認單筆收款
async function confirmPayment(participantId, amount) {
    const res = await fetch(`/api/group-orders/${currentOrderId}/payments/confirm`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ participantId, amount })
    });
    if (res.ok) {
        alert('已確認收款！');
        manageFinances(); // 重新整理
    } else {
        alert(await res.text());
    }
}

// UC-07: 最終結算鎖定
async function finalizeFinances() {
    if (!confirm('結算後將鎖定所有財務紀錄，無法再修改，確定嗎？')) return;
    const res = await fetch(`/api/group-orders/${currentOrderId}/payments/finalize`, { method: 'POST' });
    if (res.ok) {
        alert('財務結算完成！');
        document.getElementById('financeSection').style.display = 'none';
    } else {
        alert('結算失敗');
    }
}
/**
 * UC-07: 個人裝置自動向後端查詢並顯示自己的付款狀態
 */
async function fetchMyPaymentStatus(orderId) {
    const participantId = getDeviceId();
    try {
        const res = await fetch(`/api/group-orders/${orderId}/payments/my-status?participantId=${participantId}`);
        if (res.ok) {
            const data = await res.json();
            document.getElementById('myPaymentStatusCard').style.display = 'block';
            document.getElementById('myDueAmount').textContent = `$${data.amountDue}`;

            const statusLabel = document.getElementById('myPayStatus');
            statusLabel.textContent = data.status;
            if (data.status === '已付款') {
                statusLabel.style.background = '#27ae60';
            } else {
                statusLabel.style.background = '#ff9800';
            }
        }
    } catch (e) {
        console.error("無法載入個人財務狀態", e);
    }
}

/**
 * UC-07: 主揪切換顯示/隱藏財務對帳表格
 */
function toggleFinanceTable() {
    const section = document.getElementById('hostFinanceSection');
    if (section.style.display === 'none') {
        loadHostFinanceTable();
    } else {
        section.style.display = 'none';
    }
}

/**
 * UC-07: 主揪向後端獲取對帳表格數據並渲染 (包含總額計算與備註功能)
 */
async function loadHostFinanceTable() {
    if (!currentOrderId) return alert('請先選擇一個團購');
    const password = prompt('請輸入管理者密碼以開啟財務對帳表：');
    if (!password) return;

    try {
        const res = await fetch(`/api/group-orders/${currentOrderId}/payments/summary?password=${encodeURIComponent(password)}`);
        if (!res.ok) throw new Error(await res.text());
        const summaryData = await res.json();

        // 1. 動態計算總實收與剩餘待繳金額
        let totalReceived = 0;
        let totalRemaining = 0;
        summaryData.forEach(r => {
            if (r.status === '已付款') {
                totalReceived += r.amountDue;
            } else {
                totalRemaining += r.amountDue;
            }
        });

        // 2. 建立畫面上方的總額統計區塊
        let html = `
            <div style="display: flex; gap: 20px; background: #e8f5e9; padding: 15px; border-radius: 8px; margin-bottom: 15px; border: 1px solid #c8e6c9;">
                <div style="font-size: 1.1rem; color: #2e7d32;">💰 總實收金額：<strong>$${totalReceived}</strong></div>
                <div style="font-size: 1.1rem; color: #d32f2f;">⚠️ 剩餘待繳金額：<strong>$${totalRemaining}</strong></div>
            </div>
            <table style="width: 100%; border-collapse: collapse; text-align: left; margin-top: 10px;">
                <thead>
                    <tr style="background: #f5f5f5; border-bottom: 2px solid #ccc;">
                        <th style="padding: 10px;">裝置 ID 與餐點內容</th>
                        <th style="padding: 10px;">應付金額</th>
                        <th style="padding: 10px;">目前狀態</th>
                        <th style="padding: 10px;">備註 (多收/尚欠)</th>
                        <th style="padding: 10px;">操作動作</th>
                    </tr>
                </thead>
                <tbody>
        `;

        if (summaryData.length === 0) {
            html += `<tr><td colspan="5" style="padding:15px; color:#666;">目前尚無任何人點餐，無法對帳。</td></tr>`;
        }

        summaryData.forEach(record => {
            const itemsHtml = record.details.map(item => `<div style="font-size:0.9rem; color:#555;">• ${item}</div>`).join('');
            const isPaid = record.status === '已付款';
            const statusColor = isPaid ? '#27ae60' : '#ff9800';

            html += `
                <tr style="border-bottom: 1px solid #eee;">
                    <td style="padding: 12px; max-width: 250px; word-wrap: break-word;">
                        <div style="font-weight:bold; color:#1877f2; margin-bottom: 4px;">ID: ${record.participantId}</div>
                        ${itemsHtml}
                    </td>
                    <td style="padding: 12px; font-weight:bold; color:#d32f2f;">$${record.amountDue}</td>
                    <td style="padding: 12px;"><span style="color:white; background:${statusColor}; padding:3px 6px; border-radius:4px; font-size:0.85rem;">${record.status}</span></td>

                    <td style="padding: 12px;">
                        <input type="text" id="remark-${record.participantId}" value="${record.remarks || ''}" placeholder="例如：找10元" style="width: 100px; padding: 5px;">
                    </td>

                    <td style="padding: 12px; display: flex; flex-direction: column; gap: 5px;">
                        ${isPaid ?
                            `<button class="action-btn" style="background:#e0e0e0; color:#333; padding:5px 10px; font-size:0.85rem;" onclick="updateUserPayStatus('${record.participantId}', '未付款')">取消核帳</button>` :
                            `<button class="action-btn" style="background:#27ae60; color:white; padding:5px 10px; font-size:0.85rem;" onclick="updateUserPayStatus('${record.participantId}', '已付款')">確認收錢</button>`
                        }
                        <button class="action-btn" style="background:#2196f3; color:white; padding:5px 10px; font-size:0.85rem;" onclick="updateUserPayStatus('${record.participantId}', '${record.status}')">💾 儲存備註</button>
                    </td>
                </tr>
            `;
        });

        html += `</tbody></table>`;
        document.getElementById('financeTableContainer').innerHTML = html;
        document.getElementById('hostFinanceSection').style.display = 'block';
    } catch (e) {
        alert(e.message);
    }
}
//async function loadHostFinanceTable() {
//    if (!currentOrderId) return alert('請先選擇一個團購');
//    const password = prompt('請輸入管理者密碼以開啟財務對帳表：');
//    if (!password) return;
//
//    // 驗證管理者密碼是否正確
//    const verifyRes = await fetch(`/api/group-orders/close/${currentOrderId}`, {
//        method: 'POST',
//        headers: { 'Content-Type': 'application/json' },
//        body: JSON.stringify({ password: password })
//    });
//
//    // 注意：因密碼驗證在原架構會順便把狀態改為「已結單」，此處僅借用驗證邏輯，若密碼錯誤會回傳 400
//    if (!verifyRes.ok && verifyRes.status === 400) {
//        return alert('密碼錯誤，拒絕存取財務資料！');
//    }
//
//    try {
//        const res = await fetch(`/api/group-orders/${currentOrderId}/payments/summary`);
//        if (!res.ok) throw new Error("載入失敗");
//        const summaryData = await res.json();
//
//        let html = `
//            <table style="width: 100%; border-collapse: collapse; text-align: left; margin-top: 10px;">
//                <thead>
//                    <tr style="background: #f5f5f5; border-bottom: 2px solid #ccc;">
//                        <th style="padding: 10px;">訂購者與餐點內容</th>
//                        <th style="padding: 10px;">應付金額</th>
//                        <th style="padding: 10px;">目前狀態</th>
//                        <th style="padding: 10px;">操作動作</th>
//                    </tr>
//                </thead>
//                <tbody>
//        `;
//
//        if (summaryData.length === 0) {
//            html += `<tr><td colspan="4" style="padding:15px; text-color:#666;">目前尚無任何人點餐，無法對帳。</td></tr>`;
//        }
//
//        summaryData.forEach(record => {
//            const itemsHtml = record.details.map(item => `<div style="font-size:0.9rem; color:#555;">• ${item}</div>`).join('');
//            const isPaid = record.status === '已付款';
//            const statusColor = isPaid ? '#27ae60' : '#ff9800';
//
//            html += `
//                <tr style="border-bottom: 1px solid #eee;">
//                    <td style="padding: 12px;">
//                        <span style="font-weight:bold; color:#1877f2;">裝置 ID: ${record.participantId.substring(0,8)}...</span>
//                        ${itemsHtml}
//                    </td>
//                    <td style="padding: 12px; font-weight:bold; color:#d32f2f;">$${record.amountDue}</td>
//                    <td style="padding: 12px;"><span style="color:white; background:${statusColor}; padding:3px 6px; border-radius:4px; font-size:0.85rem;">${record.status}</span></td>
//                    <td style="padding: 12px;">
//                        ${isPaid ?
//                            `<button class="action-btn" style="background:#e0e0e0; color:#333; padding:5px 10px; font-size:0.85rem;" onclick="updateUserPayStatus('${record.participantId}', '未付款')">設為未付</button>` :
//                            `<button class="action-btn" style="background:#27ae60; color:white; padding:5px 10px; font-size:0.85rem;" onclick="updateUserPayStatus('${record.participantId}', '已付款')">確認收錢</button>`
//                        }
//                    </td>
//                </tr>
//            `;
//        });
//
//        html += `</tbody></table>`;
//        document.getElementById('financeTableContainer').innerHTML = html;
//        document.getElementById('hostFinanceSection').style.display = 'block';
//    } catch (e) {
//        alert("無法讀取對帳表資料");
//    }
//}

/**
 * UC-07: 主揪點擊更改某人的付款狀態或儲存備註
 */
async function updateUserPayStatus(participantId, newStatus) {
    // 抓取當下輸入框內的備註文字
    const remarkInput = document.getElementById(`remark-${participantId}`);
    const remarks = remarkInput ? remarkInput.value : '';

    try {
        const res = await fetch(`/api/group-orders/${currentOrderId}/payments/status?participantId=${encodeURIComponent(participantId)}&status=${encodeURIComponent(newStatus)}&remarks=${encodeURIComponent(remarks)}`, {
            method: 'PUT'
        });
        if (res.ok) {
            // 更新成功後，重新拉取資料刷新表格 (同時會重新計算上方的總金額)
            // 這裡不再需要重問密碼，因為資料重新渲染速度很快，可以達到類似即時更新的效果
            // *注意：由於 loadHostFinanceTable 內有密碼 prompt，為避免無限彈窗，我們可以暫時將就，
            // 或是請主揪再次輸入密碼。為求體驗，我們這裡簡單 alert 提示即可，或需要主揪手動再點開對帳表。
            alert("狀態與備註更新成功！");
            document.getElementById('hostFinanceSection').style.display = 'none'; // 先收起，逼迫重刷，或者你有暫存密碼機制就更好
            fetchMyPaymentStatus(currentOrderId);
        } else {
            alert("更新失敗");
        }
    } catch (e) {
        alert("連線異常");
    }
}