window.dashboardModules = window.dashboardModules || [];
window.dashboardModules.push(() => {
    window.menuEditorInstance = new MenuEditor('vendor_001');
});

class MenuEditor {
    constructor(vendorId) {
        window.menuEditorInstance = this;
        this.vendorId = vendorId;
        this.menuData = { isActive: true, categories: [] };
        this.container = document.getElementById('menu-container');
        this.initEventListeners();
        this.loadMenuData();
    }

    async loadMenuData() {
        this.container.innerHTML = '載入店家現有菜單中...';
        try {
            const res = await fetch(`/api/vendors/${this.vendorId}/menu`, { cache: 'no-store' });
            if (res.ok) {
                this.menuData = await res.json();
                if (this.menuData.isActive === undefined) this.menuData.isActive = true;
            } else {
                this.menuData = { isActive: true, categories: [] };
            }
        } catch (e) {
            console.log('連線失敗，使用空白菜單');
            this.menuData = { isActive: true, categories: [] };
        }
        this.render();
    }

    initEventListeners() {
        const addCategory = () => {
            const input = document.getElementById('newCategoryName');
            const catName = input.value.trim();
            if (!catName) return alert('請輸入分類名稱');
            this.menuData.categories.push({ name: catName, isActive: true, items: [] });
            input.value = '';
            this.render();
            this.submitMenu({ silent: false });
        };

        document.getElementById('add-category-submit').addEventListener('click', addCategory);
        document.getElementById('newCategoryName').addEventListener('keydown', (event) => {
            if (event.key === 'Enter') {
                event.preventDefault();
            }
        });
    }

    toggleMenuStatus() { this.menuData.isActive = !this.menuData.isActive; this.render(); }

    editCategory(catIndex) {
        const cat = this.menuData.categories[catIndex];
        const newName = prompt('修改分類名稱:', cat.name);
        if (newName && newName.trim() !== '') { cat.name = newName.trim(); this.render(); }
    }
    toggleCategoryStatus(catIndex) {
        const cat = this.menuData.categories[catIndex];
        cat.isActive = !cat.isActive; this.render();
    }

    editItem(catIndex, itemIndex) {
        const item = this.menuData.categories[catIndex].items[itemIndex];
        const newName = prompt('修改餐點名稱:', item.name);
        if (!newName) return;
        const newPrice = prompt('修改價格:', item.unitPrice);
        if (newPrice !== null && !isNaN(newPrice) && parseInt(newPrice, 10) >= 0) {
            item.name = newName.trim(); item.unitPrice = parseInt(newPrice, 10);
            this.render();
        }
    }
    toggleItemStatus(catIndex, itemIndex) {
        const item = this.menuData.categories[catIndex].items[itemIndex];
        item.isActive = !item.isActive; this.render();
    }

    promptAddModifier(catIndex, itemIndex) {
        const groupName = prompt('請輸入客製化群組名稱 (例如：甜度):');
        if (!groupName || groupName.trim() === '') return;
        const optionName = prompt('請輸入選項名稱 (例如：半糖):');
        if (!optionName || optionName.trim() === '') return;
        const price = prompt(`請輸入「${optionName}」的加價金額:`);
        if (price !== null && !isNaN(price) && parseInt(price, 10) >= 0) {
            const item = this.menuData.categories[catIndex].items[itemIndex];
            if (!item.modifierGroups) item.modifierGroups = [];
            let group = item.modifierGroups.find(g => g.name === groupName.trim());
            if (!group) {
                group = { name: groupName.trim(), isActive: true, options: [] };
                item.modifierGroups.push(group);
            }
            group.options.push({ name: optionName.trim(), extraPrice: parseInt(price, 10) });
            this.render();
        }
    }
    editModifierGroup(catIndex, itemIndex, mgIndex) {
        const mg = this.menuData.categories[catIndex].items[itemIndex].modifierGroups[mgIndex];
        const newName = prompt('修改群組名稱:', mg.name);
        if (newName && newName.trim() !== '') { mg.name = newName.trim(); this.render(); }
    }
    toggleModifierGroupStatus(catIndex, itemIndex, mgIndex) {
        const mg = this.menuData.categories[catIndex].items[itemIndex].modifierGroups[mgIndex];
        mg.isActive = !mg.isActive; this.render();
    }
    editModifierOption(catIndex, itemIndex, mgIndex, optIndex) {
        const opt = this.menuData.categories[catIndex].items[itemIndex].modifierGroups[mgIndex].options[optIndex];
        const newName = prompt('修改選項名稱:', opt.name);
        if (!newName) return;
        const newPrice = prompt('修改加價金額:', opt.extraPrice);
        if (newPrice !== null && !isNaN(newPrice) && parseInt(newPrice, 10) >= 0) {
            opt.name = newName.trim(); opt.extraPrice = parseInt(newPrice, 10);
            this.render();
        }
    }

    render() {
        this.container.innerHTML = '';

        const menuStatusDiv = document.createElement('div');
        menuStatusDiv.style.marginBottom = '15px';
        menuStatusDiv.style.padding = '10px';
        menuStatusDiv.style.backgroundColor = '#fff';
        menuStatusDiv.style.borderLeft = this.menuData.isActive === false ? '4px solid red' : '4px solid green';
        menuStatusDiv.innerHTML = `
            <strong>當前菜單狀態：</strong>
            <span style="color: ${this.menuData.isActive === false ? 'red' : 'green'}; font-weight: bold; margin-right: 10px;">
                ${this.menuData.isActive === false ? '🔴 已下架' : '🟢 營業上架中'}
            </span>
            <button class="action-btn" style="padding: 4px 8px; font-size: 0.8rem; background: #6c757d;" onclick="window.menuEditorInstance.toggleMenuStatus()">切換狀態</button>
        `;
        this.container.appendChild(menuStatusDiv);

        if (this.menuData.categories.length === 0) return;

        this.menuData.categories.forEach((category, catIndex) => {
            const catDiv = document.createElement('div');
            catDiv.className = 'category-card';
            if (category.isActive === false) catDiv.style.opacity = '0.5';

            catDiv.innerHTML = `
                <div class="category-header">
                    <h3 style="${category.isActive === false ? 'text-decoration: line-through; color: red;' : ''}">📂 ${category.name}</h3>
                    <div>
                        <button class="action-btn" style="background:#ffc107; color:black; padding:4px 8px; font-size:0.8rem;" onclick="window.menuEditorInstance.editCategory(${catIndex})">✏️ 修改</button>
                        <button class="action-btn" style="background:${category.isActive !== false ? '#dc3545' : '#28a745'}; padding:4px 8px; font-size:0.8rem;" onclick="window.menuEditorInstance.toggleCategoryStatus(${catIndex})">${category.isActive !== false ? '⬇️ 下架' : '⬆️ 上架'}</button>
                    </div>
                </div>
                <div style="display:flex; gap:10px; align-items:center; margin: 10px 0;">
                    <input type="text" id="item-name-${catIndex}" placeholder="品項名稱" style="flex: 1;">
                    <input type="number" id="item-price-${catIndex}" placeholder="價格" min="0" style="width: 120px;">
                    <button class="action-btn" style="white-space: nowrap;" onclick="window.menuEditorInstance.addItem(${catIndex})">+ 新增品項並發布</button>
                </div>
            `;

            category.items.forEach((item, itemIndex) => {
                const itemDiv = document.createElement('div');
                itemDiv.className = 'item-card';
                if (item.isActive === false) itemDiv.style.opacity = '0.5';

                let modifiersHtml = '';
                if (item.modifierGroups) {
                    item.modifierGroups.forEach((mg, mgIndex) => {
                        modifiersHtml += `<div style="margin-top: 5px; font-size: 0.85rem; padding: 4px; border: 1px dashed #ccc; ${mg.isActive === false ? 'opacity:0.5; text-decoration:line-through; color:red;' : ''}">`;
                        modifiersHtml += `🏷️ <strong>${mg.name}</strong> `;
                        modifiersHtml += `<span style="cursor:pointer;" onclick="window.menuEditorInstance.editModifierGroup(${catIndex}, ${itemIndex}, ${mgIndex})">✏️</span> `;
                        modifiersHtml += `<span style="cursor:pointer; color:${mg.isActive !== false ? 'red' : 'green'};" onclick="window.menuEditorInstance.toggleModifierGroupStatus(${catIndex}, ${itemIndex}, ${mgIndex})">${mg.isActive !== false ? '⬇️下架' : '⬆️上架'}</span><br>`;

                        modifiersHtml += mg.options.map((opt, optIndex) => `
                            <span style="display:inline-block; background:#eee; padding:2px 6px; border-radius:4px; margin:2px;">
                                ${opt.name}(+$${opt.extraPrice})
                                <span style="cursor:pointer; margin-left:4px;" onclick="window.menuEditorInstance.editModifierOption(${catIndex}, ${itemIndex}, ${mgIndex}, ${optIndex})">✏️</span>
                            </span>
                        `).join('');
                        modifiersHtml += `</div>`;
                    });
                }

                itemDiv.innerHTML = `
                    <div style="flex:1;">
                        <div class="item-info">
                            <strong style="${item.isActive === false ? 'text-decoration: line-through;' : ''}">${item.name}</strong> <span>$${item.unitPrice}</span>
                            <span class="item-status" style="margin-left: 10px; background: ${item.isActive === false ? '#ffebe9' : '#e7f3ff'}; color: ${item.isActive === false ? 'red' : 'var(--primary-color)'};">${item.isActive !== false ? '✅ 上架中' : '❌ 已下架'}</span>
                        </div>
                        ${modifiersHtml}
                    </div>
                    <div style="display: flex; gap: 5px; flex-direction: column;">
                        <div style="display: flex; gap: 5px;">
                            <button class="action-btn" style="background:#ffc107; color:black; padding:4px 8px; font-size:0.8rem;" onclick="window.menuEditorInstance.editItem(${catIndex}, ${itemIndex})">✏️ 修改</button>
                            <button class="action-btn" style="background:${item.isActive !== false ? '#dc3545' : '#28a745'}; padding:4px 8px; font-size:0.8rem;" onclick="window.menuEditorInstance.toggleItemStatus(${catIndex}, ${itemIndex})">${item.isActive !== false ? '⬇️ 下架' : '⬆️ 上架'}</button>
                        </div>
                        <button class="action-btn" style="background:#6c757d; padding:4px 8px; font-size:0.8rem; width:100%;" onclick="window.menuEditorInstance.promptAddModifier(${catIndex}, ${itemIndex})">+ 新增客製化</button>
                    </div>
                `;
                catDiv.appendChild(itemDiv);
            });
            this.container.appendChild(catDiv);
        });
    }

    addItem(catIndex) {
        const nameInput = document.getElementById(`item-name-${catIndex}`);
        const priceInput = document.getElementById(`item-price-${catIndex}`);
        const itemName = nameInput.value.trim();
        const priceValue = priceInput.value.trim();
        const price = parseInt(priceValue, 10);
        
        if (!itemName) return alert('請輸入品項名稱');
        if (priceValue === '' || Number.isNaN(price) || price < 0) return alert('請輸入正確的金額');

        this.menuData.categories[catIndex].items.push({
            id: 'item_' + Date.now() + Math.floor(Math.random() * 1000),
            name: itemName,
            unitPrice: price,
            isActive: true,
            modifierGroups: []
        });
        nameInput.value = '';
        priceInput.value = '';
        this.render();
        this.submitMenu({ silent: true });
    }

    async submitMenu({ silent = false } = {}) {
        if (this.menuData.categories.length === 0) return alert('菜單不能為空！');
        try {
            const response = await fetch(`/api/vendors/${this.vendorId}/menu/submit`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(this.menuData)
            });
            if (response.ok) {
                if (!silent) {
                    alert('✅ 菜單發布成功！已同步至所有團購活動。');
                }
                this.loadMenuData();
            } else {
                alert('發布失敗，請確認後端服務。');
            }
        } catch (error) {
            alert('連線異常');
        }
    }
}
// 讓 Node.js (Jest) 可以載入此函式進行測試
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { MenuEditor };
}