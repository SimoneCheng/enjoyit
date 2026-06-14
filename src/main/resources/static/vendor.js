let allVendors = [];

// 初始化：載入店家列表
document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('vendorTableBody')) {
        fetchVendors();
    }
});

async function fetchVendors() {
    try {
        const response = await fetch('/api/vendors');
        allVendors = await response.json();
        renderVendorTable();
    } catch (error) {
        console.error('無法取得店家列表:', error);
    }
}

function renderVendorTable() {
    const tbody = document.getElementById('vendorTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = allVendors.map(v => `
        <tr style="${v.active ? '' : 'background-color: #f9f9f9; color: #999;'}">
            <td>${v.name}</td>
            <td>${v.phone}</td>
            <td>${v.address}</td>
            <td>
                <span style="color: ${v.active ? 'green' : 'red'}; font-weight: bold;">
                    ${v.active ? '● 營業中' : '○ 已下架'}
                </span>
            </td>
            <td>
                <button class="small-btn" onclick="openVendorForm('${v.id}')">編輯</button>
                ${v.active 
                    ? `<button class="small-btn danger" onclick="toggleVendorStatus('${v.id}', false)">下架</button>`
                    : `<button class="small-btn info" onclick="toggleVendorStatus('${v.id}', true)">上架</button>`
                }
            </td>
        </tr>
    `).join('');
}

async function toggleVendorStatus(id, shouldActive) {
    const action = shouldActive ? '上架' : '下架';
    if (!confirm(`確定要${action}此店家嗎？`)) return;

    try {
        const response = await fetch(`/api/vendors/${id}/status?active=${shouldActive}`, { method: 'POST' });
        
        if (response.ok) {
            alert(`店家已${action}`);
            fetchVendors();
            if (typeof window.fetchVendorsForPublish === 'function') {
                window.fetchVendorsForPublish();
            }
            if (typeof window.fetchVendorsForMenu === 'function') {
                window.fetchVendorsForMenu();
            }
        } else {
            const errorMsg = await response.text();
            alert(`${action}失敗: ${errorMsg}`);
        }
    } catch (error) {
        alert('連線錯誤');
    }
}

function openVendorForm(id = null) {
    const modal = document.getElementById('vendorModal');
    const title = document.getElementById('modalTitle');
    const form = document.getElementById('vendorForm');
    
    form.reset();
    document.getElementById('vendorId').value = id || '';
    
    if (id) {
        title.innerText = '編輯店家';
        const vendor = allVendors.find(v => v.id === id);
        if (vendor) {
            document.getElementById('vendorName').value = vendor.name;
            document.getElementById('vendorPhone').value = vendor.phone;
            document.getElementById('vendorAddress').value = vendor.address;
        }
    } else {
        title.innerText = '新增店家';
    }
    
    modal.style.display = 'block';
}

function closeVendorModal() {
    document.getElementById('vendorModal').style.display = 'none';
}

async function handleVendorSubmit(event) {
    event.preventDefault();
    
    const id = document.getElementById('vendorId').value;
    const name = document.getElementById('vendorName').value;
    const phone = document.getElementById('vendorPhone').value;
    const address = document.getElementById('vendorAddress').value;

    const method = id ? 'PUT' : 'POST';
    const url = id ? `/api/vendors/${id}` : '/api/vendors';

    try {
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, phone, address })
        });

        if (response.ok) {
            alert(id ? '更新成功' : '新增成功');
            closeVendorModal();
            fetchVendors();
            // 如果有 publish 頁面需要更新店家下拉選單，可以在此呼叫
            if (typeof window.fetchVendorsForPublish === 'function') {
                window.fetchVendorsForPublish();
            }
            if (typeof window.fetchVendorsForMenu === 'function') {
                window.fetchVendorsForMenu();
            }
        } else {
            const errorMsg = await response.text();
            alert('操作失敗: ' + errorMsg);
        }
    } catch (error) {
        alert('連線錯誤');
    }
}
