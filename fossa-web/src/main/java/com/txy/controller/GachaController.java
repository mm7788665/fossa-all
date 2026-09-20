package com.txy.controller;

import com.txy.dto.R;
import com.txy.entity.GachaPool;
import com.txy.entity.GachaRarity;
import com.txy.security.RequirePerm;
import com.txy.service.GachaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 后台抽卡配置接口（Web 管理端）
 *
 * 完整路径：/fossa/api/gacha/**
 *
 * 接口清单：
 *   GET    /fossa/api/gacha/pools              奖池列表（含 cards）
 *   GET    /fossa/api/gacha/pools/{id}         单个奖池
 *   POST   /fossa/api/gacha/pools              新建奖池
 *   PUT    /fossa/api/gacha/pools/{id}         更新奖池（整池提交：规则+保底+卡牌）
 *   DELETE /fossa/api/gacha/pools/{id}         删除奖池（含其卡牌）
 *   PUT    /fossa/api/gacha/cards/{id}/weight  单独改某张卡权重
 *   GET    /fossa/api/gacha/rarity             稀有度概率 { SSR:2, SR:12, R:36, N:50 }
 *   GET    /fossa/api/gacha/rarity/list        稀有度完整定义（含名称/颜色）
 */
@RestController
@RequestMapping("/api/gacha")
public class GachaController {

    private final GachaService gachaService;
    public GachaController(GachaService gachaService){ this.gachaService = gachaService; }

    /** 奖池列表（含 cards） */
    @RequirePerm("gacha:view")
    @GetMapping("/pools")
    public R<List<GachaPool>> list(){
        return R.ok(gachaService.listWithCards());
    }

    /** 单个奖池 */
    @RequirePerm("gacha:view")
    @GetMapping("/pools/{id}")
    public R<GachaPool> get(@PathVariable String id){
        GachaPool pool = gachaService.getWithCards(id);
        if (pool == null) return R.fail(400,"奖池不存在");
        return R.ok(pool);
    }

    /** 新建奖池 */
    @RequirePerm(value = "gacha:edit", module = "gacha", desc = "新建奖池")
    @PostMapping("/pools")
    public R<GachaPool> create(@RequestBody GachaPool pool){
        if (pool == null) return R.fail(400,"参数不能为空");
        if (pool.getName() == null || pool.getName().trim().isEmpty()) return R.fail(400,"奖池名称不能为空");
        return R.ok(gachaService.save(pool));
    }

    /** 更新奖池：前端整池提交（规则 + 保底 + 卡牌） */
    @RequirePerm(value = "gacha:edit", module = "gacha", desc = "保存奖池配置")
    @PutMapping("/pools/{id}")
    public R<Boolean> update(@PathVariable String id, @RequestBody GachaPool pool){
        if (pool == null) return R.fail(400,"参数不能为空");
        pool.setId(id);
        boolean ok = gachaService.saveWithCards(pool);
        return ok ? R.ok(true) : R.fail(400,"保存失败");
    }

    /** 删除奖池（含其卡牌） */
    @RequirePerm(value = "gacha:delete", module = "gacha", desc = "删除奖池")
    @DeleteMapping("/pools/{id}")
    public R<Boolean> remove(@PathVariable String id){
        boolean ok = gachaService.removeWithCards(id);
        return ok ? R.ok(true) : R.fail(400,"删除失败");
    }

    /**
     * 单独改某张卡权重
     * 滑块拖动时高频调用，比整池提交轻得多。
     */
    @RequirePerm(value = "gacha:edit", module = "gacha", desc = "调整卡牌权重")
    @PutMapping("/cards/{id}/weight")
    public R<Boolean> setWeight(@PathVariable String id, @RequestParam int value){
        boolean ok = gachaService.updateWeight(id, value);
        return ok ? R.ok(true) : R.fail(400,"权重更新失败，卡牌可能不存在");
    }

    /** 稀有度概率：{ "SSR":2, "SR":12, "R":36, "N":50 } */
    @RequirePerm("gacha:view")
    @GetMapping("/rarity")
    public R<Map<String, Object>> rarity(){
        return R.ok(gachaService.rarityRateMap());
    }

    /** 稀有度完整定义（名称、标签、颜色、排序） */
    @RequirePerm("gacha:view")
    @GetMapping("/rarity/list")
    public R<List<GachaRarity>> rarityList(){
        return R.ok(gachaService.rarityList());
    }
}
