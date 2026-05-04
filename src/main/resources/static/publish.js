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