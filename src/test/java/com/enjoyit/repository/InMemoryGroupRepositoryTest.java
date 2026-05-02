package com.enjoyit.repository;

import com.enjoyit.domain.Group;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryGroupRepositoryTest {

    private InMemoryGroupRepository repository;

    @BeforeEach
    void setUp() {
        // 每次測試前都建立一個全新的 Repository，確保測試之間互不干擾
        repository = new InMemoryGroupRepository();
    }

    @Test
    @DisplayName("測試儲存與查詢：儲存的群組應能透過 ID 成功找回")
    void testSaveAndFindById() {
        Group group = new Group("lab_group", "secret123");

        // 執行儲存
        Group savedGroup = repository.save(group);

        // 驗證回傳的物件與原物件相同
        assertEquals(group, savedGroup);

        // 執行查詢並驗證
        Optional<Group> foundGroup = repository.findById("lab_group");
        assertTrue(foundGroup.isPresent(), "應該要能在資料庫中找到剛剛儲存的群組");
        assertEquals("lab_group", foundGroup.get().getId());
        assertEquals("secret123", foundGroup.get().getPassword());
    }

    @Test
    @DisplayName("測試查詢不存在的群組：應安全地回傳空的 Optional")
    void testFindById_NotFound() {
        // 查詢一個從未存進去的 ID
        Optional<Group> foundGroup = repository.findById("ghost_group");

        // 驗證是否正確回傳 empty 而不是拋出 NullPointerException
        assertFalse(foundGroup.isPresent(), "查詢不存在的 ID 應該回傳空的 Optional");
    }

    @Test
    @DisplayName("測試覆寫機制：若儲存相同 ID 的群組，舊資料應被正確覆寫")
    void testSave_OverwriteExisting() {
        // 儲存第一筆資料
        Group group1 = new Group("my_group", "old_password");
        repository.save(group1);

        // 儲存相同 ID 但密碼不同的第二筆資料
        Group group2 = new Group("my_group", "new_password");
        repository.save(group2);

        // 查詢並驗證密碼是否已經變成新的
        Optional<Group> foundGroup = repository.findById("my_group");
        assertTrue(foundGroup.isPresent());
        assertEquals("new_password", foundGroup.get().getPassword(), "密碼應該被覆寫為新密碼");
    }
}