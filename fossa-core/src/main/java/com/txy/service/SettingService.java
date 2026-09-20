package com.txy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.txy.entity.Setting;
import com.txy.mapper.SettingMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SettingService {

    @Autowired
    private SettingMapper settingMapper;

    /** 返回 { appName, storeName, ... } 扁平结构 */
    public Map<String, String> all() {
        List<Setting> list = settingMapper.selectAll();
        Map<String, String> map = new HashMap<String, String>();
        for (Setting s : list) {
            map.put(s.getK(), s.getV());
        }
        return map;
    }

    public boolean update(Map<String, String> kv) {
        if (kv == null) return false;
        for (Map.Entry<String, String> e : kv.entrySet()) {
            Setting s = settingMapper.selectByKey(e.getKey());
            if (s != null) {
                s.setV(e.getValue());
                settingMapper.update(s);
            } else {
                Setting ns = new Setting();
                ns.setK(e.getKey());
                ns.setV(e.getValue());
                settingMapper.insert(ns);
            }
        }
        return true;
    }
}
