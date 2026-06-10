let currentOrderId = null;
let currentVendorId = null;
let currentOrderDeadline = null;
let deadlineTimer = null;
let editingTarget = null;

const DEVICE_KEY = 'enjoyit-device-id';
const ORDER_CACHE_PREFIX = 'enjoyit-order-items';
const DRAFT_CACHE_PREFIX = 'enjoyit-draft-order-items';

window.dashboardModules = window.dashboardModules || [];
window.dashboardModules.push(() => {
    refreshList();
    if (deadlineTimer) clearInterval(deadlineTimer);
    deadlineTimer = setInterval(checkDeadlineRealTime, 1000);
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

function checkDeadlineRealTime() {
    if (!currentOrderDeadline || !currentOrderId) return;

    const now = new Date();
    const deadline = new Date(currentOrderDeadline);
    if (now <= deadline) return;

    const deadlineDisplay = document.getElementById('deadlineDisplay');
    const closeBtn = document.getElementById('closeOrderBtn');

    if (deadlineDisplay && !deadlineDisplay.textContent.includes('已截止')) {
        deadlineDisplay.textContent = '截止時間：已截止';
        deadlineDisplay.style.color = 'red';
    }

    if (closeBtn && !closeBtn.disabled) {
        closeBtn.textContent = '團購已截止';
        closeBtn.disabled = true;
        closeBtn.classList.add('disabled');
    }

    if (currentVendorId) {
        loadOrderMenu(currentVendorId, true);
    }
}

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
        currentOrderDeadline = order.deadline;

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
