package com.txy.fossa.web.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.txy.fossa.web.common.R;
import com.txy.fossa.web.mapper.BaseMapper;
import com.txy.fossa.web.vo.PageData;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 通用 CRUD Service 模板 —— 供 Show/Order/Player/Intimacy/Notice/Setting 复用
 *
 * @param <E> 实体类型
 * @param <Q> 查询条件类型（需含 keyword/pageNum/pageSize）
 */
public abstract class CrudService<E, Q> {

    protected abstract BaseMapper<E> mapper();

    /** 分页 + 条件查询（子类可重写条件组装） */
    public R<PageData<E>> page(Q query) {
        int pageNum = getPageNum(query);
        int pageSize = getPageSize(query);
        PageHelper.startPage(pageNum, pageSize);
        List<E> list = mapper().selectList(buildParams(query));
        PageInfo<E> page = new PageInfo<>(list);
        return R.ok(PageData.of(page.getList(), page.getTotal(), page.getPageNum(), page.getPageSize()));
    }

    public R<E> detail(String id) {
        E e = mapper().selectById(id);
        if (e == null) return R.fail(404, "记录不存在");
        return R.ok(e);
    }

    @Transactional(rollbackFor = Exception.class)
    public R<E> create(E entity) {
        beforeCreate(entity);
        mapper().insert(entity);
        return R.ok(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public R<E> update(String id, E entity) {
        if (mapper().selectById(id) == null) return R.fail(404, "记录不存在");
        setId(entity, id);
        mapper().update(entity);
        return R.ok(mapper().selectById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public R<?> delete(String id) {
        if (mapper().selectById(id) == null) return R.fail(404, "记录不存在");
        mapper().deleteById(id);
        return R.ok("已删除");
    }

    /** 状态切换（适用于带 status 字段的表） */
    @Transactional(rollbackFor = Exception.class)
    public R<E> toggleStatus(String id, String status) {
        E e = mapper().selectById(id);
        if (e == null) return R.fail(404, "记录不存在");
        mapper().updateField(id, "status", status);
        return R.ok(mapper().selectById(id));
    }

    // ---- 子类可重写 ----
    protected Map<String, Object> buildParams(Q query) { return null; }
    protected void beforeCreate(E entity) {}

    // ---- 反射取值（避免强依赖 PageReq） ----
    private int getPageNum(Q q) {
        try { return (int) q.getClass().getMethod("getPageNum").invoke(q); }
        catch (Exception e) { return 1; }
    }
    private int getPageSize(Q q) {
        try { return (int) q.getClass().getMethod("getPageSize").invoke(q); }
        catch (Exception e) { return 20; }
    }
    @SuppressWarnings("unchecked")
    private void setId(E entity, String id) {
        try { entity.getClass().getMethod("setId", String.class).invoke(entity, id); } catch (Exception ignored) {}
    }
}
