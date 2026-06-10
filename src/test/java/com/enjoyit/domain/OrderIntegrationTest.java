package com.enjoyit.domain;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderIntegrationTest {

    @Test
    public void testMenuItemToOrderItemConversionAndPriceCalculation() {
        // --- 1. 模擬主揪建立菜單 (UC-03 範圍) ---
        Vendor vendor = new Vendor("美味便當店", "02-11112222", "台北市信義路二段2號");
        Menu menu = vendor.getMenu();

        MenuCategory category = new MenuCategory("主食區");
        menu.addCategory(category);

        MenuItem porkRice = new MenuItem("排骨飯", 100);
        category.addItem(porkRice);

        ModifierGroup extras = new ModifierGroup("加料");
        ModifierOption extraRice = new ModifierOption("加飯", 15);
        ModifierOption extraEgg = new ModifierOption("加蛋", 15);
        extras.addOption(extraRice);
        extras.addOption(extraEgg);

        porkRice.addModifierGroup(extras);

        // --- 2. 模擬參與者點餐 (UC-05 範圍) ---
        // 參與者選了排骨飯，並且選了加飯跟加蛋
        String participantId = "user_123";
        String orderFor = "方晉維"; // 自己吃
        int quantity = 1;

        // 模擬前端傳來的客製化選項字串
        List<String> selectedCustomizations = new ArrayList<>();
        selectedCustomizations.add(extraRice.getName() + "(+" + extraRice.getExtraPrice() + ")");
        selectedCustomizations.add(extraEgg.getName() + "(+" + extraEgg.getExtraPrice() + ")");

        // 計算小計：基礎單價 + 所有客製化加價
        int itemSubtotal = porkRice.getUnitPrice();
        itemSubtotal += extraRice.getExtraPrice();
        itemSubtotal += extraEgg.getExtraPrice();
        int totalOrderPrice = itemSubtotal * quantity;

        // 建立 OrderItem
        OrderItem orderItem = new OrderItem(
                participantId,
                orderFor,
                porkRice.getId(),
                porkRice.getName(),
                porkRice.getUnitPrice(),
                selectedCustomizations,
                quantity,
                totalOrderPrice
        );

        // --- 3. 驗證結果 ---
        // 100 (基礎) + 15 (加飯) + 15 (加蛋) = 130
        assertEquals(130, orderItem.getOrderTotalPrice(), "排骨飯加飯加蛋的總價應為 130 元");
        assertEquals("排骨飯", orderItem.getItemName());
        assertEquals(2, orderItem.getCustomizations().size());

        System.out.println("測試成功！餐點名稱: " + orderItem.getItemName());
        System.out.println("客製化選項: " + orderItem.getCustomizations());
        System.out.println("總價: " + orderItem.getOrderTotalPrice());
    }
}