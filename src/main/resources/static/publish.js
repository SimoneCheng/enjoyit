window.dashboardModules = window.dashboardModules || [];
window.dashboardModules.push(() => {
    // publish initialization if any
});

async function handlePublish() {
    const orderInfo = document.getElementById('orderName')?.value.trim();
    const announcement = document.getElementById('announcement')?.value.trim();
    const vendorId = document.getElementById('vendorSelect')?.value;
    const adminPassword = document.getElementById('adminPwdSetup')?.value;
    const deadline = document.getElementById('deadline')?.value;

    if (!orderInfo || !adminPassword) return alert('請輸入團購名稱與管理者密碼！');
    if (!currentGroupId) return alert('缺少群組資訊，請重新登入');

    try {
        const res = await fetch('/api/group-orders/publish', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ orderInfo, announcement, vendorId, adminPassword, groupId: currentGroupId })
        });

        if (res.ok) {
            const orderId = await res.text();

            if (deadline) {
                await fetch(`/api/group-orders/${orderId}/deadline?groupId=${encodeURIComponent(currentGroupId)}&password=${encodeURIComponent(adminPassword)}`, {
                    method: 'PUT',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({ deadline })
                });
            }

            alert('團購發起成功！');
            document.getElementById('orderName').value = '';
            document.getElementById('announcement').value = '';
            document.getElementById('adminPwdSetup').value = '';
            document.getElementById('deadline').value = '';
            if (typeof window.refreshList === 'function') {
                window.refreshList();
            }
        }
    } catch (e) {
        alert('連線異常');
    }
}

// 為了讓 Jest 能夠載入並測試這些函式
if (typeof module !== 'undefined' && module.exports) {
    // 依據不同檔案匯出對應的函式
    // 在 publish.js 加上： module.exports = { handlePublish };
    // 在 dashboard-base.js 加上： module.exports = { ensureLoggedIn };
    module.exports = { handlePublish };
}
async function fetchVendorsForPublish() {
    const select = document.getElementById('vendorSelect');
    if (!select) return;

    try {
        const response = await fetch('/api/vendors?activeOnly=true');
        const vendors = await response.json();
        
        const currentVal = select.value;
        if (vendors && vendors.length > 0) {
            select.innerHTML = '<option value="">-- 請選擇店家 --</option>' + 
                vendors.map(v => `<option value="${v.id}">${v.name}</option>`).join('');
            
            if (currentVal) select.value = currentVal;
        } else {
            select.innerHTML = '<option value="">-- 暫無上架店家 --</option>';
        }
    } catch (e) {
        console.error('無法載入店家清單:', e);
    }
}

// 註冊到初始化
window.dashboardModules.push(fetchVendorsForPublish);
window.fetchVendorsForPublish = fetchVendorsForPublish;
