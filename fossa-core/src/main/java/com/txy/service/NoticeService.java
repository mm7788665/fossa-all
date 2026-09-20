package com.txy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.txy.entity.Notice;
import com.txy.mapper.NoticeMapper;

import java.util.List;

@Service
public class NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    public List<Notice> list() {
        return noticeMapper.selectAll();
    }

    public Notice getById(String id) {
        return noticeMapper.selectById(id);
    }

    public boolean save(Notice notice) {
        return noticeMapper.insert(notice) > 0;
    }

    public boolean update(Notice notice) {
        return noticeMapper.update(notice) > 0;
    }

    /** 逻辑删除 */
    public boolean delete(String id) {
        return noticeMapper.logicDelete(id) > 0;
    }

    public int countPublished() {
        return noticeMapper.countPublished();
    }
}
