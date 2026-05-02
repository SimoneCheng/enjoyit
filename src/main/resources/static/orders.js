let currentOrderId = null;
let currentVendorId = null;

window.dashboardModules = window.dashboardModules || [];
window.dashboardModules.push(() => {
    refreshList();
});

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

        if (order.deadline) {
            document.getElementById('deadlineDisplay').textContent = '截止時間：' + order.deadline.substring(0, 16).replace('T', ' ');
        } else {
            document.getElementById('deadlineDisplay').textContent = '無截止時間';
        }

        const closeBtn = document.getElementById('closeOrderBtn');
        if (closeBtn) {
            if (order.status === '已結單') {
                closeBtn.textContent = '團購已截止';
                closeBtn.disabled = true;
                closeBtn.classList.add('disabled');
            } else {
                closeBtn.textContent = '🔒 結束團購';
                closeBtn.disabled = false;
                closeBtn.classList.remove('disabled');
            }
        }

        loadOrderMenu(currentVendorId);
    } catch (e) {
        console.error(e);
    }
}

async function loadOrderMenu(vendorId) {
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
                    <button class="action-btn" style="padding: 5px 10px;" onclick="openOrderForm('${item.id}')">我要點這個</button>
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
                        <div style="margin-top: 10px; display: flex; gap: 10px; align-items: center;">
                            數量: <input type="number" id="qty-${item.id}" value="1" min="1" style="width: 60px;">
                            <button class="action-btn success-btn" style="padding: 5px 15px;" onclick="submitOrderItem('${item.id}', '${item.name}', ${item.unitPrice})">送出訂單</button>
                            <button class="action-btn danger-btn" style="padding: 5px 15px;" onclick="document.getElementById('form-${item.id}').style.display='none'">取消</button>
                        </div>
                    </div>
                `;
                catDiv.appendChild(formDiv);
            });
            container.appendChild(catDiv);
        });
    } catch (e) {
        container.innerHTML = '載入菜單發生錯誤。';
    }
}

function openOrderForm(itemId) {
    document.querySelectorAll('.order-form-box').forEach(el => el.style.display = 'none');
    document.getElementById(`form-${itemId}`).style.display = 'block';
}

async function submitOrderItem(itemId, itemName, basePrice) {
    const orderFor = document.getElementById(`orderFor-${itemId}`).value.trim();
    const quantity = parseInt(document.getElementById(`qty-${itemId}`).value, 10);
    if (!orderFor) return alert('請輸入訂購人姓名');

    let extraTotal = 0;
    const customizations = [];
    document.querySelectorAll(`input[name="mod-${itemId}"]:checked`).forEach(cb => {
        customizations.push(cb.value);
        extraTotal += parseInt(cb.getAttribute('data-price'), 10);
    });

    const orderTotalPrice = (basePrice + extraTotal) * quantity;

    const orderItemJson = {
        participantId: 'user_session',
        orderFor: orderFor,
        menuItemId: itemId,
        itemName: itemName,
        unitPrice: basePrice,
        customizations: customizations,
        quantity: quantity,
        orderTotalPrice: orderTotalPrice
    };

    try {
        const res = await fetch(`/api/group-orders/${currentOrderId}/items?groupId=${encodeURIComponent(currentGroupId)}`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(orderItemJson)
        });
        if (res.ok) {
            alert(`✅ 點餐成功！ ${itemName} 小計: $${orderTotalPrice}`);
            document.getElementById(`form-${itemId}`).style.display = 'none';
        } else {
            alert(await res.text());
        }
    } catch (e) {
        alert('送出失敗');
    }
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
