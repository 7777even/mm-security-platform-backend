package com.sinopec.mmsecurity.common.cache;

import com.sinopec.mmsecurity.entity.SysMenu;
import com.sinopec.mmsecurity.entity.SysUser;
import com.sinopec.mmsecurity.mapper.SysMenuMapper;
import com.sinopec.mmsecurity.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * IdNameCacheService 读穿缓存 + 写时失效（纯 Mockito，不起 Spring 上下文，手动触发 init() 建缓存）。
 */
class IdNameCacheServiceTest {

    private final SysUserMapper userMapper = mock(SysUserMapper.class);
    private final SysMenuMapper menuMapper = mock(SysMenuMapper.class);
    private IdNameCacheService service;

    @BeforeEach
    void setUp() {
        service = new IdNameCacheService(userMapper, menuMapper);
        service.init();
    }

    @Test
    void userName_readThroughCachesAndEvicts() {
        SysUser u = new SysUser();
        u.setRealName("张三");
        when(userMapper.selectById(1L)).thenReturn(u);

        // 第一次命中 DB
        assertEquals("张三", service.userName(1L));
        // 第二次走缓存，不再查 DB
        assertEquals("张三", service.userName(1L));
        verify(userMapper, times(1)).selectById(1L);

        // 失效后再次读取应回源
        service.evictUser(1L);
        SysUser u2 = new SysUser();
        u2.setRealName("李四");
        when(userMapper.selectById(1L)).thenReturn(u2);
        assertEquals("李四", service.userName(1L));
        verify(userMapper, times(2)).selectById(1L);
    }

    @Test
    void userName_nullIdReturnsNull() {
        assertNull(service.userName(null));
    }

    @Test
    void allMenus_cachesUntilReload() {
        SysMenu m = new SysMenu();
        m.setMenuKey("fm-x");
        when(menuMapper.selectList(null)).thenReturn(List.of(m));

        assertEquals(1, service.allMenus().size());
        assertEquals(1, service.allMenus().size());
        verify(menuMapper, times(1)).selectList(null);

        // reload 主动失效 → 下次读取回源
        service.reloadMenus();
        assertEquals(1, service.allMenus().size());
        verify(menuMapper, times(2)).selectList(null);
    }
}
