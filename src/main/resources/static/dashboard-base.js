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

window.addEventListener('load', () => {
    if (!ensureLoggedIn()) return;
    if (typeof window.onDashboardLoad === 'function') {
        window.onDashboardLoad();
    }
});
