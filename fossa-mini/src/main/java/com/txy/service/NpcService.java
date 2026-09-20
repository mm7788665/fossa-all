package com.txy.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.txy.common.R;
import com.txy.dto.NpcQuery;
import com.txy.entity.Npc;
import com.txy.mapper.NpcMapper;
import com.txy.fossa.web.vo.PageData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class NpcService {

    @Autowired
    private NpcMapper npcMapper;

    /** 分页 + 条件查询 */
    public R<PageData<Npc>> page(NpcQuery q) {
        PageHelper.startPage(q.getPageNum(), q.getPageSize());
        List<Npc> list = npcMapper.selectPage(q);
        PageInfo<Npc> page = new PageInfo<>(list);
        return R.ok(PageData.of(page.getList(), page.getTotal(), page.getPageNum(), page.getPageSize()));
    }

    /** 详情 */
    public R<Npc> detail(String id) {
        Npc npc = npcMapper.selectById(id);
        if (npc == null) {
            return R.fail(404, "角色不存在");
        }
        return R.ok(npc);
    }

    /** 下拉列表（启用状态） */
    public R<List<Npc>> simple() {
        return R.ok(npcMapper.selectAllSimple());
    }

    /** 新增 */
    @Transactional(rollbackFor = Exception.class)
    public R<Npc> create(Npc npc) {
        if (!StringUtils.hasText(npc.getName())) {
            return R.fail(400, "请填写角色名称");
        }
        if (!StringUtils.hasText(npc.getRole())) {
            return R.fail(400, "请填写扮演角色");
        }
        if (!StringUtils.hasText(npc.getId())) {
            // 自动生成 ID：npc_ + uuid 前 8 位
            npc.setId("npc_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        }
        if (npc.getSort() == null) {
            npc.setSort(99);
        }
        if (!StringUtils.hasText(npc.getStatus())) {
            npc.setStatus("启用");
        }
        npcMapper.insert(npc);
        return R.ok(npc);
    }

    /** 更新（全量字段，按 id） */
    @Transactional(rollbackFor = Exception.class)
    public R<Npc> update(String id, Npc npc) {
        Npc exist = npcMapper.selectById(id);
        if (exist == null) {
            return R.fail(404, "角色不存在");
        }
        npc.setId(id);
        npcMapper.update(npc);
        return R.ok(npcMapper.selectById(id));
    }

    /** 删除 */
    @Transactional(rollbackFor = Exception.class)
    public R<?> delete(String id) {
        Npc exist = npcMapper.selectById(id);
        if (exist == null) {
            return R.fail(404, "角色不存在");
        }
        npcMapper.deleteById(id);
        return R.ok("已删除");
    }

    /** 切换启用/禁用 */
    @Transactional(rollbackFor = Exception.class)
    public R<Npc> toggleStatus(String id, String status) {
        Npc npc = npcMapper.selectById(id);
        if (npc == null) {
            return R.fail(404, "角色不存在");
        }
        if (!"启用".equals(status) && !"禁用".equals(status)) {
            return R.fail(400, "状态只能是「启用」或「禁用」");
        }
        Npc patch = new Npc();
        patch.setId(id);
        patch.setStatus(status);
        npcMapper.update(patch);
        return R.ok(npcMapper.selectById(id));
    }

    /** 修改排序值 */
    @Transactional(rollbackFor = Exception.class)
    public R<Npc> updateSort(String id, Integer sort) {
        Npc npc = npcMapper.selectById(id);
        if (npc == null) {
            return R.fail(404, "角色不存在");
        }
        Npc patch = new Npc();
        patch.setId(id);
        patch.setSort(sort == null ? 99 : sort);
        npcMapper.update(patch);
        return R.ok(npcMapper.selectById(id));
    }
}
