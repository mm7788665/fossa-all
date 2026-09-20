package com.txy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.txy.entity.Player;
import com.txy.mapper.PlayerMapper;

import java.util.List;

@Service
public class PlayerService {

    @Autowired
    private PlayerMapper playerMapper;

    public List<Player> list() {
        return playerMapper.selectAll();
    }

    public Player getById(String id) {
        return playerMapper.selectById(id);
    }
    public Player getByOpenId(String openId) {
        return playerMapper.selectByOpenId(openId);
    }
    public void add(Player player){
        playerMapper.insert(player);
    }
    public void update(Player player){
        playerMapper.update(player);
    }
    /** 逻辑删除 */
    public boolean delete(String id) {
        return playerMapper.logicDelete(id) > 0;
    }
}
