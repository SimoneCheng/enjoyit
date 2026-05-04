let currentGroupId = (() => {
    const params = new URLSearchParams(window.location.search);
    const groupId = params.get('groupId') || localStorage.getItem('groupId');
    if (groupId) {
        localStorage.setItem('groupId', groupId);
    }
    return groupId;
})();

function ensureLoggedIn() {
    if (!currentGroupId) {
        alert('請先登入群組');
        location.href = 'index.html';
        return false;
    }
    return true;
}

// 用來存放各模組的初始化函式
window.dashboardModules = window.dashboardModules || [];

window.addEventListener('load', () => {
    if (!ensureLoggedIn()) return;
    
    // 如果有舊版的單一函式也執行
    if (typeof window.onDashboardLoad === 'function') {
        window.onDashboardLoad();
    }
    
    // 執行所有註冊模組的初始化
    window.dashboardModules.forEach(fn => {
        if (typeof fn === 'function') fn();
    });
});

// 為了讓 Jest 能夠載入並測試這些函式
if (typeof module !== 'undefined' && module.exports) {
    // 依據不同檔案匯出對應的函式
    // 在 publish.js 加上： module.exports = { handlePublish };
    // 在 dashboard-base.js 加上： module.exports = { ensureLoggedIn };
    module.exports = { ensureLoggedIn };
}
